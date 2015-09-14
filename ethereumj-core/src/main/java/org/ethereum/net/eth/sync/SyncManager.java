package org.ethereum.net.eth.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.DiscoverListener;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.ByteUtil;
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

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.sync.SyncStateName.*;
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
    private static final long MASTER_STUCK_TIMEOUT = secondsToMillis(60);
    private static final long GAP_RECOVERY_TIMEOUT = secondsToMillis(2);

    private static final long LARGE_GAP_SIZE = 5;

    @Resource
    @Qualifier("syncStates")
    private Map<SyncStateName, SyncState> syncStates;
    private SyncState state;
    private final Object stateMutex = new Object();

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

                if (!CONFIG.isSyncEnabled()) {
                    logger.info("Sync Manager: OFF");
                    return;
                }

                logger.info("Sync Manager: ON");

                // set IDLE state at the beginning
                state = syncStates.get(IDLE);

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

                for (Node node : CONFIG.peerActive()) {
                    pool.connect(node);
                }

                if (logger.isInfoEnabled()) {
                    startLogWorker();
                }

            }
        }).start();
    }

    public void addPeer(Channel peer) {
        if (!CONFIG.isSyncEnabled()) {
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

            // should be synchronized with HASH_RETRIEVING state maintenance
            // to avoid double master peer initializing
            synchronized (stateMutex) {
                startMaster(peer);
            }
        }

        updateHighestKnownDifficulty(peerTotalDifficulty);

        pool.add(peer);
    }

    public void onDisconnect(Channel peer) {
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
        int gap = gapSize(wrapper);

        if (gap >= LARGE_GAP_SIZE) {
            changeState(HASH_RETRIEVING);
        } else {
            logger.info("Forcing parent downloading for block.number [{}]", wrapper.getNumber());
            queue.addHash(wrapper.getParentHash());
        }
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

        peer.changeSyncState(IDLE);
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

        // gap for this block is being recovered
        if (block.equals(gapBlock) && !state.is(IDLE)) {
            logger.trace("Gap recovery is already in progress for block.number [{}]", gapBlock.getNumber());
            return false;
        }

        // ALL blocks are downloaded, we definitely have a gap
        if (queue.isHashesEmpty()) {
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

        return stats.millisSinceLastUpdate() > MASTER_STUCK_TIMEOUT
                || stats.getEmptyResponsesCount() > 0;
    }

    void startMaster(Channel master) {
        pool.changeState(IDLE);

        if (gapBlock != null) {
            int gap = gapSize(gapBlock);
            master.setMaxHashesAsk(gap > CONFIG.maxHashesAsk() ? CONFIG.maxHashesAsk() : gap);
            master.setLastHashToAsk(gapBlock.getParentHash());
        } else {
            master.setMaxHashesAsk(CONFIG.maxHashesAsk());
            master.setLastHashToAsk(master.getBestKnownHash());
            queue.clearHashes();
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
                pool.logActivePeers();
                pool.logBannedPeers();
                logger.info("\n");
                logger.info("State {}\n", state);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void removeUselessPeers() {
        List<Channel> removed = new ArrayList<>();
        for (Channel peer : pool) {
            if (peer.hasBlocksLack()) {
                logger.info("Peer {}: has no more blocks, removing", Utils.getNodeIdShort(peer.getPeerId()));
                removed.add(peer);
                updateLowerUsefulDifficulty(peer.getTotalDifficulty());
            }
        }
        pool.removeAll(removed);
    }

    private void fillUpPeersPool() {
        int lackSize = CONFIG.syncPeerCount() - pool.activeCount();
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
