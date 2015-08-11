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

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final int MASTER_STUCK_TIME_THRESHOLD = 60 * 1000; // 60 seconds

    private static final long LARGE_GAP_THRESHOLD = 5;

    private SyncState state = SyncState.INIT;
    private SyncState prevState = SyncState.INIT;
    private EthHandler masterPeer;
    private long lastHashesLoadedCnt = 0;
    private long masterStuckAt = 0;
    private int maxHashesAsk;
    private byte[] bestHash;
    private boolean onSyncDoneTriggered = false;

    private BigInteger lowerUsefulDifficulty = BigInteger.ZERO;
    private BigInteger highestKnownDifficulty = BigInteger.ZERO;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService logWorker = Executors.newSingleThreadScheduledExecutor();

    private DiscoverListener discoverListener;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private EthereumListener ethereumListener;

    @Autowired
    private PeersPool pool;

    public void init() {

        if (!CONFIG.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }

        logger.info("Sync Manager: ON");
        updateLowerUsefulDifficulty();
        updateHighestKnownDifficulty();
        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updateLowerUsefulDifficulty();
                    updateHighestKnownDifficulty();
                    checkGapRecovery();
                    checkMaster();
                    checkPeers();
                    askNewPeers();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in main sync worker", e);
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
        if(logger.isInfoEnabled()) {
            logWorker.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logStats();
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
        discoverListener = new DiscoverListener() {
            @Override
            public void nodeAppeared(NodeHandler handler) {
                String nodeId = Hex.toHexString(handler.getNode().getId());
                if(pool.isInUse(nodeId)) {
                    return;
                }
                if(logger.isTraceEnabled()) logger.trace(
                        "Peer {}: new best chain peer discovered: {} vs {}",
                        Utils.getNodeIdShort(Hex.toHexString(handler.getNode().getId())),
                        handler.getNodeStatistics().getEthTotalDifficulty(),
                        highestKnownDifficulty
                );
                pool.connect(handler.getNode());
            }

            @Override
            public void nodeDisappeared(NodeHandler handler) {
            }
        };
        nodeManager.addDiscoverListener(
                discoverListener,
                new Functional.Predicate<NodeStatistics>() {
                    @Override
                    public boolean test(NodeStatistics nodeStatistics) {
                        if(nodeStatistics.getEthLastInboundStatusMsg() == null) {
                            return false;
                        }
                        BigInteger thatDifficulty = nodeStatistics.getEthLastInboundStatusMsg().getTotalDifficultyAsBigInt();
                        return !isIn20PercentRange(highestKnownDifficulty, thatDifficulty);
                    }
                }
        );
    }

    private void checkMaster() {
        if(masterPeer == null) {
            return;
        }
        if(isHashRetrieving() && masterPeer.isHashRetrievingDone()) {
            changeState(SyncState.BLOCK_RETRIEVING);
        }
        if(isGapRecovery() && masterPeer.isHashRetrievingDone()) {
            masterPeer.changeState(SyncState.BLOCK_RETRIEVING);
        }

        if((isHashRetrieving() || isGapRecovery())) {
            long hashesLoadedCnt = masterPeer.getHashesLoadedCnt();
            if(hashesLoadedCnt > lastHashesLoadedCnt) {
                masterStuckAt = 0;
            } else {
                if(masterStuckAt == 0) {
                    masterStuckAt = System.currentTimeMillis();
                } else {
                    if(timeSinceMasterStuck() > MASTER_STUCK_TIME_THRESHOLD) {
                        masterStuckAt = 0;
                        logger.info(
                                "Master peer {}: banned due to stuck timeout exceeding",
                                Utils.getNodeIdShort(masterPeer.getPeerId())
                        );
                        pool.ban(masterPeer);
                    }
                }
            }
        }
    }

    private long timeSinceMasterStuck() {
        return System.currentTimeMillis() - masterStuckAt;
    }

    private void checkGapRecovery() {
        if(masterPeer == null) {
            return;
        }
        if(isGapRecovery() && !masterPeer.isHashRetrieving() && hashStoreEmpty()) {
            if(prevState == SyncState.BLOCK_RETRIEVING) {
                changeState(SyncState.BLOCK_RETRIEVING);
            } else {
                changeState(SyncState.DONE_GAP_RECOVERY);
            }
        }
    }

    private void checkPeers() {
        pool.remove(new Functional.Predicate<EthHandler>() {
            @Override
            public boolean test(EthHandler peer) {
                if (peer.hasNoMoreBlocks()) {
                    logger.info("Peer {}: has no more blocks, removing", Utils.getNodeIdShort(peer.getPeerId()));
                    updateLowerUsefulDifficulty(peer.getTotalDifficulty());
                    return true;
                } else {
                    return false;
                }
            }
        });

        // checking if master peer is still presented
        if ((isHashRetrieving() || isGapRecovery())) {
            EthHandler master = pool.findOne(new Functional.Predicate<EthHandler>() {
                @Override
                public boolean test(EthHandler peer) {
                    return peer.isHashRetrieving() || peer.isHashRetrievingDone();
                }
            });
            if(master == null) {
                logger.info("Master peer has been lost, find a new one");
                if (isHashRetrieving()) {
                    changeState(SyncState.HASH_RETRIEVING);
                } else if (isGapRecovery()) {
                    changeState(SyncState.GAP_RECOVERY);
                }
            }
        }

        // forcing peers to continue blocks downloading if there are more hashes to process
        // peers becoming idle if meet empty hashstore but it's not the end
        if((isBlockRetrieving() || isSyncDone() || isGapRecoveryDone()) && !hashStoreEmpty()) {
            pool.changeState(SyncState.BLOCK_RETRIEVING, new Functional.Predicate<EthHandler>() {
                @Override
                public boolean test(EthHandler peer) {
                    return peer.isIdle();
                }
            });
        }
    }

    private void askNewPeers() {
        int lackSize = CONFIG.syncPeerCount() - pool.activeCount();
        if(lackSize <= 0) {
            return;
        }

        final Set<String> nodesInUse = pool.nodesInUse();

        List<NodeHandler> newNodes = nodeManager.getNodes(
                new Functional.Predicate<NodeHandler>() {
                    @Override
                    public boolean test(NodeHandler nodeHandler) {
                        if(nodeHandler.getNodeStatistics().getEthLastInboundStatusMsg() == null) {
                            return false;
                        }
                        if(nodesInUse.contains(Hex.toHexString(nodeHandler.getNode().getId()))) {
                            return false;
                        }
                        BigInteger thatDifficulty = nodeHandler
                                .getNodeStatistics()
                                .getEthLastInboundStatusMsg()
                                .getTotalDifficultyAsBigInt();
                        return thatDifficulty.compareTo(lowerUsefulDifficulty) > 0;
                    }
                },
                new Comparator<NodeHandler>() {
                    @Override
                    public int compare(NodeHandler n1, NodeHandler n2) {
                        BigInteger td1 = null;
                        BigInteger td2 = null;
                        if(n1.getNodeStatistics().getEthLastInboundStatusMsg() != null) {
                            td1 = n1.getNodeStatistics().getEthLastInboundStatusMsg().getTotalDifficultyAsBigInt();
                        }
                        if(n2.getNodeStatistics().getEthLastInboundStatusMsg() != null) {
                            td2 = n2.getNodeStatistics().getEthLastInboundStatusMsg().getTotalDifficultyAsBigInt();
                        }
                        if (td1 != null && td2 != null) {
                            return td2.compareTo(td1);
                        } else if (td1 == null && td2 == null) {
                            return 0;
                        } else if (td1 != null) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                },
                lackSize
        );

        if(pool.isEmpty() && newNodes.isEmpty()) {
            newNodes = nodeManager.getNodes(
                    new Functional.Predicate<NodeHandler>() {
                        @Override
                        public boolean test(NodeHandler nodeHandler) {
                            if(nodeHandler.getNodeStatistics().getEthLastInboundStatusMsg() == null) {
                                return false;
                            }
                            if(nodesInUse.contains(Hex.toHexString(nodeHandler.getNode().getId()))) {
                                return false;
                            }
                            return true;
                        }
                    },
                    new Comparator<NodeHandler>() {
                        @Override
                        public int compare(NodeHandler n1, NodeHandler n2) {
                            return Integer.valueOf(n2.getNodeStatistics().getReputation())
                                    .compareTo(n1.getNodeStatistics().getReputation());
                        }
                    },
                    lackSize
            );
        }

        if(logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for(NodeHandler n : newNodes) {
                sb.append(Utils.getNodeIdShort(Hex.toHexString(n.getNode().getId())));
                sb.append(", ");
            }
            if(sb.length() > 0) {
                sb.delete(sb.length() - 2, sb.length());
            }
            logger.trace(
                    "Node list obtained from discovery: {}",
                    newNodes.size() > 0 ? sb.toString() : "empty"
            );
        }

        for(NodeHandler n : newNodes) {
            pool.connect(n.getNode());
        }
    }

    private void logStats() {
        pool.logActive();
        pool.logBans();
        logger.info("State {}", state);
        logger.info("\n");
    }

    public void onDisconnect(EthHandler peer) {
        pool.onDisconnect(peer);
    }

    public synchronized void addPeer(EthHandler peer) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: adding",
                peer.getPeerIdShort()
        );

        BigInteger peerTotalDifficulty = peer.getTotalDifficulty();

        if(lowerUsefulDifficulty.compareTo(peerTotalDifficulty) > 0) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its difficulty lower than ours: {} vs {}, skipping",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    lowerUsefulDifficulty.toString()
            );
            // TODO report about lower total difficulty
            return;
        }

        // prohibit transactions processing until main sync is done
        if(!onSyncDoneTriggered) {
            peer.prohibitTransactions();
        }

        pool.add(peer);

        if(isInit()) {
            if(blockchain.getQueue().hasSolidBlocks()) {
                logger.info("It seems that BLOCK_RETRIEVING was interrupted, starting from this state now");
                changeState(SyncState.BLOCK_RETRIEVING);
            } else if (peerTotalDifficulty.compareTo(highestKnownDifficulty) > 0) {
                if(logger.isInfoEnabled()) logger.info(
                        "Peer {}: its chain is better than previously known: {} vs {}, initiating HASH_RETRIEVING",
                        Utils.getNodeIdShort(peer.getPeerId()),
                        peerTotalDifficulty.toString(),
                        highestKnownDifficulty.toString()
                );
                changeState(SyncState.HASH_RETRIEVING);
            } else if(logger.isTraceEnabled()) {
                logger.trace(
                        "Peer {}: its chain is worse than previously known: {} vs {}",
                        Utils.getNodeIdShort(peer.getPeerId()),
                        peerTotalDifficulty.toString(),
                        highestKnownDifficulty.toString()
                );
            }
        }

        if(isHashRetrieving() && !isIn20PercentRange(highestKnownDifficulty, peerTotalDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}, switching HASH_RETRIEVING",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownDifficulty.toString()
            );
            changeState(SyncState.HASH_RETRIEVING);
        }
    }

    public void recoverGap(BlockWrapper wrapper) {
        if(isGapRecovery()) {
            logger.info("Gap recovery is already in progress, postpone");
            return;
        }

        if(wrapper.isSolidBlock()) {
            if(!allowSolidBlockGapRecovery()) {
                logger.info("We are in {} state, postpone SOLID blocks gap recovery", state, wrapper.getNumber());
                return;
            }
        } else {
            if(!allowNewBlockGapRecovery()) {
                logger.info("We are in {} state, postpone NEW blocks gap recovery", state, wrapper.getNumber());
                return;
            }
        }

        Block bestBlock = blockchain.getBestBlock();
        long gap = wrapper.getNumber() - bestBlock.getNumber();
        if(logger.isInfoEnabled()) {
            logger.info(
                    "Try to recover gap for {} block.number [{}] vs best.number [{}]",
                    wrapper.isNewBlock() ? "NEW" : "",
                    wrapper.getNumber(),
                    bestBlock.getNumber()
            );
        }
        if(gap > LARGE_GAP_THRESHOLD) {
            maxHashesAsk = gap > CONFIG.maxHashesAsk() ? CONFIG.maxHashesAsk() : (int) gap;
            bestHash = wrapper.getHash();
            logger.debug("Recover blocks gap, block.number [{}], block.hash [{}]", wrapper.getNumber(), wrapper.getShortHash());
            changeState(SyncState.GAP_RECOVERY);
        } else {
            logger.info("Forcing parent downloading for block.number [{}]", wrapper.getNumber());
            blockchain.getQueue().getHashStore().addFirst(wrapper.getParentHash());
            blockchain.getQueue().logHashQueueSize();
        }
    }

    private boolean allowSolidBlockGapRecovery() {
        return !(isInit() || isHashRetrieving());
    }

    private boolean allowNewBlockGapRecovery() {
        return (isBlockRetrieving() && hashStoreEmpty()) || isSyncDone() || isGapRecoveryDone();
    }

    public void notifyNewBlockImported(BlockWrapper wrapper) {
        if(isSyncDone() || isGapRecovery() || isGapRecoveryDone()) {
            return;
        }
        if(!wrapper.isSolidBlock()) {
            logger.info("NEW block.number [{}] imported", wrapper.getNumber());
            changeState(SyncState.DONE_SYNC);
        } else if (logger.isInfoEnabled()) {
            logger.info(
                    "NEW block.number [{}] block.minsSinceReceiving [{}] exceeds import time limit, continue sync",
                    wrapper.getNumber(),
                    wrapper.timeSinceReceiving() / 1000 / 60
            );
        }
    }

    public synchronized void changeState(SyncState newState) {
        if(newState == SyncState.HASH_RETRIEVING) {
            masterPeer = pool.findBest();
            if(masterPeer == null) {
                return;
            }
            BlockQueue queue = blockchain.getQueue();
            updateHighestKnownDifficulty(masterPeer.getTotalDifficulty());

            bestHash = masterPeer.getBestHash();
            queue.getHashStore().clear();
            pool.changeState(SyncState.IDLE);
            maxHashesAsk = CONFIG.maxHashesAsk();
            runHashRetrievingOnMaster();
        }
        if(newState == SyncState.GAP_RECOVERY) {
            masterPeer = pool.findBest();
            if(masterPeer == null) {
                return;
            }
            runHashRetrievingOnMaster();
            logger.info("Gap recovery initiated");
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            pool.changeState(SyncState.BLOCK_RETRIEVING);
            logger.info("Block retrieving initiated");
        }
        if(newState == SyncState.DONE_GAP_RECOVERY) {
            pool.changeState(SyncState.BLOCK_RETRIEVING);
            logger.info("Done gap recovery");
        }
        if(newState == SyncState.DONE_SYNC) {
            if(onSyncDoneTriggered) {
                return;
            }
            onSyncDoneTriggered = true;
            pool.changeState(SyncState.DONE_SYNC);
            ethereumListener.onSyncDone();
            logger.info("Main synchronization is finished");
        }
        if(newState != state) {
            this.prevState = this.state;
            this.state = newState;
        }
    }

    private void runHashRetrievingOnMaster() {
        lastHashesLoadedCnt = 0;
        masterStuckAt = 0;
        blockchain.getQueue().setBestHash(bestHash);
        masterPeer.setMaxHashesAsk(maxHashesAsk);
        masterPeer.changeState(SyncState.HASH_RETRIEVING);
        logger.info("Master peer hashes retrieving initiated, best known hash [{}], askLimit [{}]", Hex.toHexString(bestHash), maxHashesAsk);
        logger.debug("Our best block hash [{}]", Hex.toHexString(blockchain.getBestBlockHash()));
    }

    private void updateLowerUsefulDifficulty(BigInteger difficulty) {
        if(difficulty.compareTo(lowerUsefulDifficulty) > 0) {
            lowerUsefulDifficulty = difficulty;
        }
    }

    private void updateLowerUsefulDifficulty() {
        updateLowerUsefulDifficulty(blockchain.getTotalDifficulty());
    }

    private void updateHighestKnownDifficulty(BigInteger difficulty) {
        if(difficulty.compareTo(highestKnownDifficulty) > 0) {
            highestKnownDifficulty = difficulty;
        }
    }

    private void updateHighestKnownDifficulty() {
        updateHighestKnownDifficulty(blockchain.getTotalDifficulty());
    }

    public boolean isHashRetrieving() {
        return state == SyncState.HASH_RETRIEVING;
    }

    public boolean isGapRecovery() {
        return state == SyncState.GAP_RECOVERY;
    }

    public boolean isGapRecoveryDone() {
        return state == SyncState.DONE_GAP_RECOVERY;
    }

    public boolean isBlockRetrieving() {
        return state == SyncState.BLOCK_RETRIEVING;
    }

    public boolean isSyncDone() {
        return state == SyncState.DONE_SYNC;
    }

    public boolean hashStoreEmpty() {
        return blockchain.getQueue().isHashesEmpty();
    }

    public boolean isInit() {
        return state == SyncState.INIT;
    }

}
