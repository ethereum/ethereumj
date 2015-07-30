package org.ethereum.net.eth;

import org.apache.commons.collections4.Predicate;
import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.DiscoverListener;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.util.CollectionUtils;
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

    private static final int PEERS_COUNT = 5;

    private static final int CONNECTION_TIMEOUT = 60 * 1000; // 60 seconds
    private static final int BAN_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private static final int DISCONNECT_HITS_THRESHOLD = 5;

    private static final long LARGE_GAP_THRESHOLD = 5;
    private static final long TIME_TO_IMPORT_THRESHOLD = 10 * 60 * 1000; // 10 minutes

    private SyncState state = SyncState.INIT;
    private SyncState prevState = SyncState.INIT;
    private EthHandler masterPeer;
    private final List<EthHandler> peers = new CopyOnWriteArrayList<>();
    private int maxHashesAsk;
    private byte[] bestHash;

    private BigInteger lowerUsefulDifficulty;
    private BigInteger highestKnownDifficulty;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService logWorker = Executors.newSingleThreadScheduledExecutor();

    private final Object connectionsMutex = new Object();
    private Map<String, Long> connectTimestamps = new HashMap<>();
    private Map<String, Integer> disconnectHits = new HashMap<>();
    private Map<String, Long> bannedNodes = new HashMap<>();

    private DiscoverListener discoverListener;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private EthereumListener ethereumListener;

    public void init() {
        highestKnownDifficulty = blockchain.getTotalDifficulty();
        lowerUsefulDifficulty = highestKnownDifficulty;
        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    checkMaster();
                    checkPeers();
                    removeOutdatedConnections();
                    askNewPeers();
                    releaseBans();
                } catch (Exception e) {
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
                initiateConnection(handler.getNode());
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
                        return thatDifficulty.compareTo(highestKnownDifficulty) > 0;
                    }
                }
        );
    }

    private void releaseBans() {
        synchronized (connectionsMutex) {
            Set<String> outdated = new HashSet<>();
            for(Map.Entry<String, Long> e : bannedNodes.entrySet()) {
                if(System.currentTimeMillis() - e.getValue() > BAN_TIMEOUT) {
                    outdated.add(e.getKey());
                    logger.info("Peer {}: releasing ban", Utils.getNodeIdShort(e.getKey()));
                }
            }
            for(String nodeId : outdated) {
                bannedNodes.remove(nodeId);
            }
        }
    }

    private void removeOutdatedConnections() {
        synchronized (connectionsMutex) {
            Set<String> outdated = new HashSet<>();
            for(Map.Entry<String, Long> e : connectTimestamps.entrySet()) {
                if(System.currentTimeMillis() - e.getValue() > CONNECTION_TIMEOUT) {
                    outdated.add(e.getKey());
                }
            }
            for(String nodeId : outdated) {
                connectTimestamps.remove(nodeId);
            }
        }
    }

    private void checkMaster() {
        if(isHashRetrieving() && masterPeer.isHashRetrievingDone()) {
            changeState(SyncState.BLOCK_RETRIEVING);
        }
        if(isGapRecovery() && masterPeer.isHashRetrievingDone()) {
            if(prevState == SyncState.BLOCK_RETRIEVING) {
                changeState(SyncState.BLOCK_RETRIEVING);
            } else {
                changeState(SyncState.DONE_GAP_RECOVERY);
            }
        }
    }

    private void checkPeers() {
        List<EthHandler> removed = new ArrayList<>();
        for(EthHandler peer : peers) {
            if(peer.hasNoMoreBlocks()) {
                logger.info("Peer {}: has no more blocks, removing", Utils.getNodeIdShort(peer.getPeerId()));
                removed.add(peer);
                peer.changeState(SyncState.IDLE);
                BigInteger td = peer.getHandshakeStatusMessage().getTotalDifficultyAsBigInt();
                if(td.compareTo(lowerUsefulDifficulty) > 0) {
                    lowerUsefulDifficulty = td;
                }
            }
        }
        if(blockchain.getTotalDifficulty().compareTo(lowerUsefulDifficulty) > 0) {
            lowerUsefulDifficulty = blockchain.getTotalDifficulty();
        }
        peers.removeAll(removed);

        // checking if master peer is still presented
        if(isHashRetrieving() || isGapRecovery()) {
            EthHandler master = org.apache.commons.collections4.CollectionUtils.find(peers, new Predicate<EthHandler>() {
                @Override
                public boolean evaluate(EthHandler peer) {
                    return peer.isHashRetrieving();
                }
            });
            if(master == null) {
                logger.info("Master peer has been lost, find a new one");
                if(isHashRetrieving()) {
                    changeState(SyncState.HASH_RETRIEVING);
                } else if(isGapRecovery()) {
                    changeState(SyncState.GAP_RECOVERY);
                }
            }
        }

        // forcing peers to continue blocks downloading if there are more hashes to process
        // peers becoming idle if meet empty hashstore but it's not the end
        if((isBlockRetrieving() || isSyncDone() || isGapRecoveryDone()) && !hashStoreEmpty()) {
            for(EthHandler peer : peers) {
                if(peer.isIdle()) {
                    peer.changeState(SyncState.BLOCK_RETRIEVING);
                }
            }
        }
    }

    private void askNewPeers() {
        int peersLackSize = PEERS_COUNT - peers.size();
        if(peersLackSize <= 0) {
            return;
        }

        final Set<String> nodesInUse;
        synchronized (connectionsMutex) {
            nodesInUse = CollectionUtils.collectSet(peers, new Functional.Function<EthHandler, String>() {
                @Override
                public String apply(EthHandler handler) {
                    return handler.getPeerId();
                }
            });
            nodesInUse.addAll(connectTimestamps.keySet());
            nodesInUse.addAll(bannedNodes.keySet());
        }

        List<NodeHandler> newNodes = nodeManager.getNodes(
                new Functional.Predicate<NodeHandler>() {
                    @Override
                    public boolean test(NodeHandler nodeHandler) {
                        if (nodeHandler.getNodeStatistics().getEthLastInboundStatusMsg() == null) {
                            return false;
                        }
                        if (nodesInUse.contains(Hex.toHexString(nodeHandler.getNode().getId()))) {
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
                        if (n1.getNodeStatistics().getEthLastInboundStatusMsg() != null) {
                            td1 = n1.getNodeStatistics().getEthLastInboundStatusMsg().getTotalDifficultyAsBigInt();
                        }
                        if (n2.getNodeStatistics().getEthLastInboundStatusMsg() != null) {
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
                peersLackSize
        );

        for(NodeHandler n : newNodes) {
            initiateConnection(n.getNode());
        }
    }

    private void logStats() {

        if (peers.size() > 0) {
            logger.info("\n");
            logger.info("Active peers");
            logger.info("============");
            for(EthHandler peer : peers) {
                peer.logSyncStats();
            }
            logger.info("\n");
        }

        if (bannedNodes.size() > 0) {
            logger.info("\n");
            logger.info("Banned peers");
            logger.info("============");
            for(Map.Entry<String, Long> e : bannedNodes.entrySet()) {
                logger.info(
                        "Peer {} | {} minutes ago",
                        Utils.getNodeIdShort(e.getKey()),
                        (System.currentTimeMillis() - e.getValue()) / 60 / 1000
                );
            }
            logger.info("\n");
        }

        logger.info("State {}", state);
        logger.info("\n");
    }

    public void onDisconnect(EthHandler peer) {
        peer.onDisconnect();
        peers.remove(peer);

        synchronized (connectionsMutex) {
            connectTimestamps.remove(peer.getPeerId());
            Integer hits = disconnectHits.get(peer.getPeerId());
            if(hits == null) {
                hits = 0;
            }
            if(hits > DISCONNECT_HITS_THRESHOLD) {
                bannedNodes.put(peer.getPeerId(), System.currentTimeMillis());
                logger.info("Peer {}: banned due to disconnects exceeding", Utils.getNodeIdShort(peer.getPeerId()));
                disconnectHits.remove(peer.getPeerId());
            } else {
                disconnectHits.put(peer.getPeerId(), hits + 1);
            }
        }
    }

    public void addPeer(EthHandler peer) {
        BigInteger peerTotalDifficulty = peer.getTotalDifficulty();

        synchronized (connectionsMutex) {
            connectTimestamps.remove(peer.getPeerId());
            if(blockchain.getTotalDifficulty().compareTo(peerTotalDifficulty) > 0) {
                if(logger.isInfoEnabled()) logger.info(
                        "Peer {}: its difficulty lower than ours: {} vs {}, skipping",
                        Utils.getNodeIdShort(peer.getPeerId()),
                        peerTotalDifficulty.toString(),
                        blockchain.getTotalDifficulty().toString()
                );
                // TODO report about lower total difficulty
                return;
            }
            peers.add(peer);
        }
        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));

        if (!isIn20PercentRange(highestKnownDifficulty, peerTotalDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownDifficulty.toString()
            );
            logger.debug(
                    "Peer {}: best hash [{}]",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    Hex.toHexString(peer.getBestHash())
            );
            changeState(SyncState.HASH_RETRIEVING);
        } else if(state == SyncState.BLOCK_RETRIEVING) {
            peer.changeState(SyncState.BLOCK_RETRIEVING);
        }
    }

    public void recoverGap(BlockWrapper wrapper) {
        if(isGapRecovery()) {
            logger.info("Gap recovery is already in progress, postpone");
            return;
        }
        if(wrapper.isNewBlock() && !allowNewBlockGapRecovery()) {
            logger.info("We are in {} state, postpone NEW blocks gap recovery", state, wrapper.getNumber());
            return;
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

    private boolean allowNewBlockGapRecovery() {
        return (isBlockRetrieving() && hashStoreEmpty()) || isSyncDone() || isGapRecoveryDone();
    }

    public void notifyNewBlockImported(BlockWrapper wrapper) {
        if(isSyncDone() || isGapRecovery() || isGapRecoveryDone()) {
            return;
        }
        if(wrapper.timeSinceReceiving() <= TIME_TO_IMPORT_THRESHOLD) {
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
            if(peers.isEmpty()) {
                return;
            }
            masterPeer = Collections.max(peers, new Comparator<EthHandler>() {
                @Override
                public int compare(EthHandler p1, EthHandler p2) {
                    return p1.getTotalDifficulty().compareTo(p2.getTotalDifficulty());
                }
            });
            BlockQueue queue = blockchain.getQueue();
            highestKnownDifficulty = masterPeer.getTotalDifficulty();

            if(state == SyncState.INIT && blockchain.getQueue().syncWasInterrupted()) {
                logger.info("It seems that BLOCK_RETRIEVING was interrupted, run it again");
                changeState(SyncState.BLOCK_RETRIEVING);
                return;
            }

            bestHash = masterPeer.getBestHash();
            queue.getHashStore().clear();
            changePeersState(SyncState.IDLE);
            maxHashesAsk = CONFIG.maxHashesAsk();
            runHashRetrievingOnMaster();
        }
        if(newState == SyncState.GAP_RECOVERY) {
            if(peers.isEmpty()) {
                return;
            }
            masterPeer = Collections.max(peers, new Comparator<EthHandler>() {
                @Override
                public int compare(EthHandler p1, EthHandler p2) {
                    return p1.getTotalDifficulty().compareTo(p2.getTotalDifficulty());
                }
            });
            runHashRetrievingOnMaster();
            logger.info("Gap recovery initiated");
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            changePeersState(SyncState.BLOCK_RETRIEVING);
            logger.info("Block retrieving initiated");
        }
        if(newState == SyncState.DONE_GAP_RECOVERY) {
            changePeersState(SyncState.BLOCK_RETRIEVING);
            logger.info("Done gap recovery");
        }
        if(newState == SyncState.DONE_SYNC) {
            if(state == SyncState.DONE_SYNC) {
                return;
            }
            changePeersState(SyncState.DONE_SYNC);
            ethereumListener.onSyncDone();
            logger.info("Main synchronization is finished");
        }
        if(newState != state) {
            this.prevState = this.state;
            this.state = newState;
        }
    }

    private void runHashRetrievingOnMaster() {
        blockchain.getQueue().setBestHash(bestHash);
        masterPeer.setMaxHashesAsk(maxHashesAsk);
        masterPeer.changeState(SyncState.HASH_RETRIEVING);
        logger.info("Master peer hashes retrieving initiated, best known hash [{}], askLimit [{}]", Hex.toHexString(bestHash), maxHashesAsk);
        logger.debug("Our best block hash [{}]", Hex.toHexString(blockchain.getBestBlockHash()));
    }

    private void changePeersState(SyncState newState) {
        for (EthHandler peer : peers) {
            peer.changeState(newState);
        }
    }

    private void initiateConnection(Node node) {
        synchronized (connectionsMutex) {
            String nodeId = Hex.toHexString(node.getId());
            if(connectTimestamps.containsKey(nodeId)) {
                return;
            }
            Set<String> usedNodes = CollectionUtils.collectSet(peers, new Functional.Function<EthHandler, String>() {
                @Override
                public String apply(EthHandler peer) {
                    return peer.getPeerId();
                }
            });
            if(usedNodes.contains(nodeId)) {
                return;
            }
            ethereum.connect(node);
            connectTimestamps.put(nodeId, System.currentTimeMillis());
        }
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
}
