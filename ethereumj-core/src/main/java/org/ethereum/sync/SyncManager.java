package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.DiscoverListener;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.Functional;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

import static org.ethereum.net.eth.EthVersion.*;
import static org.ethereum.sync.SyncStateName.*;
import static org.ethereum.util.BIUtil.isIn20PercentRange;
import static org.ethereum.util.TimeUtils.secondsToMillis;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = secondsToMillis(1);
    private static final long PEER_STUCK_TIMEOUT = secondsToMillis(60);
    private static final long GAP_RECOVERY_TIMEOUT = secondsToMillis(2);

    @Autowired
    SystemProperties config;

    @Resource
    @Qualifier("syncStates")
    private Map<SyncStateName, SyncState> syncStates;
    private SyncState state;
    private final Object stateMutex = new Object();

    /**
     * master peer version
     */
    EthVersion masterVersion = V62;

    /**
     * block which gap recovery is running for
     */
    private BlockWrapper gapBlock;

    /**
     * true if sync done event was triggered
     */
    private boolean syncDone = false;

    private BigInteger lowerUsefulDifficulty = BigInteger.ZERO;
    private BigInteger highestKnownDifficulty = BigInteger.ZERO;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    Blockchain blockchain;

    @Autowired
    SyncQueue queue;

    @Autowired
    NodeManager nodeManager;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    PeersPool pool;

    @Autowired
    ChannelManager channelManager;

    public void init() {

        // make it asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {

                // sync queue
                queue.init();

                if (!config.isSyncEnabled()) {
                    logger.info("Sync Manager: OFF");
                    return;
                }

                logger.info("Sync Manager: ON");

                // set IDLE state at the beginning
                state = syncStates.get(IDLE);

                masterVersion = initialMasterVersion();

                updateDifficulties();

                changeState(initialState());

                addBestKnownNodeListener();

                worker.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateDifficulties();
                            removeUselessPeers();
                            fillUpPeersPool();
                            maintainState();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            logger.error("Exception in main sync worker", t);
                        }
                    }
                }, WORKER_TIMEOUT, WORKER_TIMEOUT, TimeUnit.MILLISECONDS);

                if (logger.isInfoEnabled()) {
                    startLogWorker();
                }

            }
        }).start();
    }

    public void addPeer(Channel peer) {
        if (!config.isSyncEnabled()) {
            return;
        }

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: adding",
                peer.getPeerIdShort()
        );

        BigInteger peerTotalDifficulty = peer.getTotalDifficulty();

        if (!isIn20PercentRange(peerTotalDifficulty, lowerUsefulDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: difficulty significantly lower than ours: {} vs {}, skipping",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    lowerUsefulDifficulty.toString()
            );
            // TODO report low total difficulty
            return;
        }

        if (state.is(HASH_RETRIEVING) && !isIn20PercentRange(highestKnownDifficulty, peerTotalDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}, rotate master peer",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownDifficulty.toString()
            );

            Channel master = pool.findOne(new Functional.Predicate<Channel>() {
                @Override
                public boolean test(Channel peer) {
                    return peer.isHashRetrieving() || peer.isHashRetrievingDone();
                }
            });

            if (master == null || master.isEthCompatible(peer)) {

                // should be synchronized with HASH_RETRIEVING state maintenance
                // to avoid double master peer initializing
                synchronized (stateMutex) {
                    startMaster(peer);
                }
            }
        }

        updateHighestKnownDifficulty(peerTotalDifficulty);

        pool.add(peer);
    }

    public void onDisconnect(Channel peer) {

        // if master peer has been disconnected
        // we need to process data it sent
        if (peer.isHashRetrieving() || peer.isHashRetrievingDone()) {
            changeState(BLOCK_RETRIEVING);
        }

        pool.onDisconnect(peer);
    }

    public void tryGapRecovery(BlockWrapper wrapper) {
        if (!isGapRecoveryAllowed(wrapper)) {
            return;
        }

        if (logger.isDebugEnabled()) logger.debug(
                "Recovering gap: best.number [{}] vs block.number [{}]",
                blockchain.getBestBlock().getNumber(),
                wrapper.getNumber()
        );

        gapBlock = wrapper;

        changeState(HASH_RETRIEVING);
    }

    public BlockWrapper getGapBlock() {
        return gapBlock;
    }

    void resetGapRecovery() {
        this.gapBlock = null;
    }

    public void notifyNewBlockImported(BlockWrapper wrapper) {
        if (syncDone) {
            return;
        }

        if (!wrapper.isSolidBlock()) {
            syncDone = true;
            onSyncDone();

            logger.debug("NEW block.number [{}] imported", wrapper.getNumber());
        } else if (logger.isInfoEnabled()) {
            logger.debug(
                    "NEW block.number [{}] block.minsSinceReceiving [{}] exceeds import time limit, continue sync",
                    wrapper.getNumber(),
                    wrapper.timeSinceReceiving() / 1000 / 60
            );
        }
    }

    public boolean isSyncDone() {
        return syncDone;
    }

    public void reportInvalidBlock(byte[] nodeId) {

        Channel peer = pool.getByNodeId(nodeId);

        if (peer == null) {
            return;
        }

        logger.info("Peer {}: received invalid block, drop it", peer.getPeerIdShort());

        pool.ban(peer);

        // TODO decrease peer's reputation

    }

    private int gapSize(BlockWrapper block) {
        Block bestBlock = blockchain.getBestBlock();
        return (int) (block.getNumber() - bestBlock.getNumber());
    }

    private void onSyncDone() {
        channelManager.onSyncDone();
        ethereumListener.onSyncDone();
        logger.info("Main synchronization is finished");
    }

    private boolean isGapRecoveryAllowed(BlockWrapper block) {
        // hashes are not downloaded yet, recovery doesn't make sense at all
        if (state.is(HASH_RETRIEVING)) {
            return false;
        }

        // no peers compatible with latest master left, we're stuck
        if (!pool.hasCompatible(masterVersion)) {
            logger.trace("No peers compatible with {}, recover the gap", masterVersion);
            return true;
        }

        // gap for this block is being recovered
        if (block.equals(gapBlock) && !state.is(IDLE)) {
            logger.trace("Gap recovery is already in progress for block.number [{}]", gapBlock.getNumber());
            return false;
        }

        // ALL blocks are downloaded, we definitely have a gap
        if (!hasBlockHashes()) {
            logger.trace("No hashes/headers left, recover the gap", masterVersion);
            return true;
        }

        // if blocks downloading is in progress
        // and import fails during some period of time
        // then we assume that faced with a gap
        // but anyway NEW blocks must wait until SyncManager becomes idle
        if (!block.isNewBlock()) {
            return block.timeSinceFail() > GAP_RECOVERY_TIMEOUT;
        } else {
            return state.is(IDLE);
        }
    }

    void changeState(SyncStateName newStateName) {
        SyncState newState = syncStates.get(newStateName);

        if (state == newState) {
            return;
        }

        logger.info("Changing state from {} to {}", state, newState);

        synchronized (stateMutex) {
            newState.doOnTransition();
            state = newState;
        }
    }

    boolean isPeerStuck(Channel peer) {
        SyncStatistics stats = peer.getSyncStats();

        return stats.millisSinceLastUpdate() > PEER_STUCK_TIMEOUT
                || stats.getEmptyResponsesCount() > 0;
    }

    void startMaster(Channel master) {
        pool.changeState(IDLE);

        masterVersion = master.getEthVersion();

        if (gapBlock != null) {
            master.setLastHashToAsk(gapBlock.getHash());
        } else {
            master.setLastHashToAsk(master.getBestKnownHash());
            queue.clearHashes();
            queue.clearHeaders();
        }

        master.changeSyncState(HASH_RETRIEVING);

        if (logger.isInfoEnabled()) logger.info(
                "Peer {}: {} initiated, lastHashToAsk [{}], askLimit [{}]",
                master.getPeerIdShort(),
                state,
                Hex.toHexString(master.getLastHashToAsk()),
                master.getMaxHashesAsk()
        );
    }

    boolean hasBlockHashes() {
        if (masterVersion.isCompatible(V62)) {
            return !queue.isHeadersEmpty();
        } else {
            return !queue.isHashesEmpty();
        }
    }

    private void updateDifficulties() {
        updateLowerUsefulDifficulty(blockchain.getTotalDifficulty());
        updateHighestKnownDifficulty(blockchain.getTotalDifficulty());
    }

    private void updateLowerUsefulDifficulty(BigInteger difficulty) {
        if (difficulty.compareTo(lowerUsefulDifficulty) > 0) {
            lowerUsefulDifficulty = difficulty;
        }
    }

    private void updateHighestKnownDifficulty(BigInteger difficulty) {
        if (difficulty.compareTo(highestKnownDifficulty) > 0) {
            highestKnownDifficulty = difficulty;
        }
    }

    private SyncStateName initialState() {
        if (queue.hasSolidBlocks()) {
            logger.info("It seems that BLOCK_RETRIEVING was interrupted, starting from this state now");
            return BLOCK_RETRIEVING;
        } else {
            return HASH_RETRIEVING;
        }
    }

    private EthVersion initialMasterVersion() {
        if (!queue.isHeadersEmpty() || queue.isHashesEmpty()) {
            return V62;
        } else {
            return V61;
        }
    }

    private void addBestKnownNodeListener() {
        nodeManager.addDiscoverListener(
                new DiscoverListener() {
                    @Override
                    public void nodeAppeared(NodeHandler handler) {
                        if (logger.isTraceEnabled()) logger.trace(
                                "Peer {}: new best chain peer discovered: {} vs {}",
                                handler.getNode().getHexIdShort(),
                                handler.getNodeStatistics().getEthTotalDifficulty(),
                                highestKnownDifficulty
                        );
                        pool.connect(handler.getNode());
                    }

                    @Override
                    public void nodeDisappeared(NodeHandler handler) {
                    }
                },
                new Functional.Predicate<NodeStatistics>() {
                    @Override
                    public boolean test(NodeStatistics nodeStatistics) {
                        if (nodeStatistics.getEthTotalDifficulty() == null) {
                            return false;
                        }
                        return !isIn20PercentRange(highestKnownDifficulty, nodeStatistics.getEthTotalDifficulty());
                    }
                }
        );
    }

    // WORKER

    private void startLogWorker() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    pool.logActivePeers();
                    pool.logBannedPeers();
                    logger.info("\n");
                    logger.info("State {}\n", state);
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Exception in log worker", t);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void removeUselessPeers() {
        List<Channel> removed = new ArrayList<>();
        for (Channel peer : pool) {
            if (peer.hasBlocksLack()) {
                logger.info("Peer {}: has no more blocks, ban", peer.getPeerIdShort());
                removed.add(peer);
                updateLowerUsefulDifficulty(peer.getTotalDifficulty());
            }
        }

        // todo decrease peers' reputation

        for (Channel peer : removed) {
            pool.ban(peer);
        }
    }

    private void fillUpPeersPool() {
        int lackSize = config.syncPeerCount() - pool.activeCount();
        if(lackSize <= 0) {
            return;
        }

        Set<String> nodesInUse = pool.nodesInUse();

        List<NodeHandler> newNodes = nodeManager.getBestEthNodes(nodesInUse, lowerUsefulDifficulty, lackSize);
        if (pool.isEmpty() && newNodes.isEmpty()) {
            newNodes = nodeManager.getBestEthNodes(nodesInUse, BigInteger.ZERO, lackSize);
        }

        if (logger.isTraceEnabled()) {
            logDiscoveredNodes(newNodes);
        }

        for(NodeHandler n : newNodes) {
            pool.connect(n.getNode());
        }
    }

    private void logDiscoveredNodes(List<NodeHandler> nodes) {
        StringBuilder sb = new StringBuilder();
        for(NodeHandler n : nodes) {
            sb.append(Utils.getNodeIdShort(Hex.toHexString(n.getNode().getId())));
            sb.append(", ");
        }
        if(sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        logger.trace(
                "Node list obtained from discovery: {}",
                nodes.size() > 0 ? sb.toString() : "empty"
        );
    }

    private void maintainState() {
        synchronized (stateMutex) {
            state.doMaintain();
        }
    }
}
