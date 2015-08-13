package org.ethereum.net.eth;

import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.rlpx.discover.DiscoverListener;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.Functional;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.BIUtil.isIn20PercentRange;
import static org.ethereum.util.TimeUtils.secondsToMillis;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = secondsToMillis(3);
    private static final long MASTER_STUCK_TIMEOUT = secondsToMillis(60);
    private static final long GAP_RECOVERY_TIMEOUT = secondsToMillis(10);

    private static final long LARGE_GAP_SIZE = 5;

    private SyncState state = SyncState.IDLE;

    /**
     * master peer parameters
     */
    private int maxHashesAsk = CONFIG.maxHashesAsk();
    private byte[] bestHash;

    /**
     * true if sync done event was triggered
     */
    private boolean syncDone = false;

    /**
     * indicates if main hash retrieving was done before
     * required for managing master peer behaviour
     */
    private boolean mainHashRetrievingPassed = false;

    private BigInteger lowerUsefulDifficulty = BigInteger.ZERO;
    private BigInteger highestKnownDifficulty = BigInteger.ZERO;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    Blockchain blockchain;

    @Autowired
    BlockQueue queue;

    @Autowired
    NodeManager nodeManager;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    PeersPool pool;

    @Autowired
    ChannelManager channelManager;

    public void init() {

        if (!CONFIG.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }

        logger.info("Sync Manager: ON");

        updateDifficulties();

        SyncState initial = initialState();
        if (initial == SyncState.BLOCK_RETRIEVING) {
            mainHashRetrievingPassed = true;
        }
        changeState(initial);

        addBestKnownNodeListener();

        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updateDifficulties();
                    removeUselessPeers();
                    fillUpPeersPool();
                    processStates();
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

    public void addPeer(EthHandler peer) {
        if (!CONFIG.isSyncEnabled()) {
            return;
        }

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: adding",
                peer.getPeerIdShort()
        );

        BigInteger peerTotalDifficulty = peer.getTotalDifficulty();

        if (lowerUsefulDifficulty.compareTo(peerTotalDifficulty) > 0) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its difficulty lower than ours: {} vs {}, skipping",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    lowerUsefulDifficulty.toString()
            );
            // TODO report about lower total difficulty
            return;
        }

        if (isHashRetrieving() && !isIn20PercentRange(highestKnownDifficulty, peerTotalDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}, rotate master peer",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownDifficulty.toString()
            );

            // should be synchronized with processHashRetrieving
            // to avoid double master peer initializing
            synchronized (this) {
                startMaster(peer);
            }
        }

        updateHighestKnownDifficulty(peerTotalDifficulty);

        pool.add(peer);
    }

    public void onDisconnect(EthHandler peer) {
        pool.onDisconnect(peer);
    }

    public void tryGapRecovery(BlockWrapper wrapper) {
        boolean allowed = isGapRecoveryAllowed(wrapper);

        // in any case we should reset the timeout
        // to try again only after some time passed
        wrapper.resetImportFail();

        if (!allowed) return;

        Block bestBlock = blockchain.getBestBlock();
        long gap = wrapper.getNumber() - bestBlock.getNumber();

        if (logger.isDebugEnabled()) logger.debug(
                "Recovering gap: best.number [{}] vs block.number [{}]",
                bestBlock.getNumber(),
                wrapper.getNumber()
        );

        if (gap >= LARGE_GAP_SIZE) {
            maxHashesAsk = gap > CONFIG.maxHashesAsk() ? CONFIG.maxHashesAsk() : (int) gap;
            bestHash = wrapper.getHash();
            changeState(SyncState.HASH_RETRIEVING);
        } else {
            logger.info("Forcing parent downloading for block.number [{}]", wrapper.getNumber());
            queue.addHash(wrapper.getParentHash());
        }
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

    public boolean isHashRetrieving() {
        return state == SyncState.HASH_RETRIEVING;
    }

    public boolean isIdle() {
        return state == SyncState.IDLE;
    }

    public boolean isSyncDone() {
        return syncDone;
    }

    private void onSyncDone() {
        channelManager.onSyncDone();
        ethereumListener.onSyncDone();
        logger.info("Main synchronization is finished");
    }

    private boolean isGapRecoveryAllowed(BlockWrapper block) {
        // hashes are not downloaded yet, recovery doesn't make sense at all
        if (isHashRetrieving()) {
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
            return isIdle();
        }
    }

    private void changeState(SyncState newState) {
        if (state == newState) {
            return;
        }

        logger.info("Changing state from {} to {}", state, newState);

        if (newState == SyncState.BLOCK_RETRIEVING) {
            pool.changeState(SyncState.BLOCK_RETRIEVING);
        }

        if (newState == SyncState.IDLE) {
            pool.changeState(SyncState.IDLE);
        }

        state = newState;
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

    private void startMaster(EthHandler master) {
        pool.changeState(SyncState.IDLE);
        if (!mainHashRetrievingPassed) {
            bestHash = master.getBestHash();
            queue.clearHashes();
        }
        queue.setBestHash(bestHash);
        master.setMaxHashesAsk(maxHashesAsk);
        master.changeState(SyncState.HASH_RETRIEVING);

        logger.info(
                "Peer {}: {} initiated, best known hash [{}], askLimit [{}]",
                master.getPeerIdShort(),
                state,
                Hex.toHexString(bestHash),
                maxHashesAsk
        );
    }

    private SyncState initialState() {
        if (queue.hasSolidBlocks()) {
            logger.info("It seems that BLOCK_RETRIEVING was interrupted, starting from this state now");
            return SyncState.BLOCK_RETRIEVING;
        } else {
            return SyncState.HASH_RETRIEVING;
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
                pool.logActive();
                pool.logBans();
                logger.info("State {}", state);
                logger.info("\n");
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void removeUselessPeers() {
        List<EthHandler> removed = new ArrayList<>();
        for (EthHandler peer : pool) {
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

    private void processStates() {
        switch (state) {
            case HASH_RETRIEVING:
                processHashRetrieving();
                return;
            case BLOCK_RETRIEVING:
                processBlockRetrieving();
                return;
            case IDLE:
                processIdle();
        }
    }

    private synchronized void processHashRetrieving() {
        EthHandler master = null;
        for (EthHandler peer : pool) {
            // if hash retrieving is done all we need is just change state and quit
            if (peer.isHashRetrievingDone()) {
                mainHashRetrievingPassed = true;
                changeState(SyncState.BLOCK_RETRIEVING);
                return;
            }

            // master is found
            if (peer.isHashRetrieving()) {
                master = peer;
                break;
            }
        }

        if (master != null) {
            // if master is stuck ban it
            if(master.stats.millisSinceLastUpdate() > MASTER_STUCK_TIMEOUT
                    || master.stats.getEmptyResponsesCount() > 0) {
                pool.ban(master);
                pool.remove(master);
                logger.info("Master peer {}: banned due to stuck timeout exceeding", master.getPeerIdShort());
                master = null;
            }
        }

        if (master == null) {
            logger.trace("{} is in progress, starting master peer", state);
            master = pool.getBest();
            if (master == null) {
                return;
            }
            startMaster(master);
        }
    }

    private void processBlockRetrieving() {
        if (queue.isHashesEmpty()) {
            changeState(SyncState.IDLE);
            return;
        }

        pool.changeState(SyncState.BLOCK_RETRIEVING, new Functional.Predicate<EthHandler>() {
            @Override
            public boolean test(EthHandler peer) {
                return peer.isIdle();
            }
        });
    }

    private void processIdle() {
        if (!queue.isHashesEmpty()) {
            changeState(SyncState.BLOCK_RETRIEVING);
        }
    }
}
