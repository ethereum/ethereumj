package org.ethereum.net.eth;

import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
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

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final int PEERS_COUNT = 5;
    private static final int CONNECTION_TIMEOUT = 60 * 1000; // 60 seconds

    private SyncState state = SyncState.INIT;
    private EthHandler masterPeer;
    private List<EthHandler> peers = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private NodeManager nodeManager;

    private byte[] bestHash;
    private BigInteger lowerUsefulDifficulty;

    private final Map<String, Long> connectTimestamps = new HashMap<>();

    @PostConstruct
    public void init() {
        lowerUsefulDifficulty = blockchain.getTotalDifficulty();
        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                checkMaster();
                checkPeers();
                removeOutdatedConnections();
                askNewPeers();
            }
        }, 0, 3, TimeUnit.SECONDS);
        if(logger.isInfoEnabled()) {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logStats();
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
        nodeManager.addDiscoverListener(
                new DiscoverListener() {
                    @Override
                    public void nodeAppeared(NodeHandler handler) {
                        initiateConnection(handler.getNode());
                    }

                    @Override
                    public void nodeDisappeared(NodeHandler handler) {
                    }
                },
                new Functional.Predicate<NodeStatistics>() {
                    @Override
                    public boolean test(NodeStatistics nodeStatistics) {
                        if (nodeStatistics.getEthLastInboundStatusMsg() == null) {
                            return false;
                        }
                        BigInteger knownDifficulty = blockchain.getQueue().getHighestTotalDifficulty();
                        if(knownDifficulty == null) {
                            return true;
                        }
                        BigInteger thatDifficulty = nodeStatistics.getEthLastInboundStatusMsg().getTotalDifficultyAsBigInt();
                        return thatDifficulty.compareTo(knownDifficulty) > 0;
                    }
                }
        );
    }

    private void removeOutdatedConnections() {
        synchronized (connectTimestamps) {
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

        // forcing peers to continue blocks downloading if there are more hashes to process
        // peers becoming idle if meet empty hashstore but it's not the end
        if(state == SyncState.BLOCK_RETRIEVING && !blockchain.getQueue().getHashStore().isEmpty()) {
            for(EthHandler peer : peers) {
                if(peer.isIdle()) {
                    peer.changeState(SyncState.BLOCK_RETRIEVING);
                }
            }
        }
    }

    private void askNewPeers() {
        int peersLackSize = PEERS_COUNT - peers.size();
        if(peersLackSize > 0) {
            final Set<String> nodesInUse = CollectionUtils.collectSet(peers, new Functional.Function<EthHandler, String>() {
                @Override
                public String apply(EthHandler handler) {
                    return handler.getPeerId();
                }
            });
            synchronized (connectTimestamps) {
                nodesInUse.addAll(connectTimestamps.keySet());
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
    }

    private void logStats() {
        for(EthHandler peer : peers) {
            peer.logSyncStats();
        }
    }

    public void removePeer(EthHandler peer) {
        synchronized (connectTimestamps) {
            connectTimestamps.remove(peer.getPeerId());
        }
        peer.changeState(SyncState.IDLE);
        peers.remove(peer);
    }

    public void addPeer(EthHandler peer) {
        synchronized (connectTimestamps) {
            connectTimestamps.remove(peer.getPeerId());
        }

        StatusMessage msg = peer.getHandshakeStatusMessage();

        BigInteger peerTotalDifficulty = msg.getTotalDifficultyAsBigInt();
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

        BlockQueue chainQueue = blockchain.getQueue();
        BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();

        if ((highestKnownTotalDifficulty == null || peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownTotalDifficulty == null ? "0" : highestKnownTotalDifficulty.toString()
            );
            logger.debug(
                    "Peer {}: best hash [{}]",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    Hex.toHexString(msg.getBestHash())
            );

            bestHash = msg.getBestHash();
            masterPeer = peer;
            chainQueue.setHighestTotalDifficulty(peerTotalDifficulty);

            changeState(SyncState.HASH_RETRIEVING);
        }

        if(state == SyncState.BLOCK_RETRIEVING) {
            peer.changeState(SyncState.BLOCK_RETRIEVING);
        }

        logger.info("Peer {}: adding to pool", Utils.getNodeIdShort(peer.getPeerId()));

        peers.add(peer);
    }

    public void changeState(SyncState newState) {
        if(state == SyncState.DONE_SYNC) {
            return;
        }
        if(newState == SyncState.HASH_RETRIEVING) {
            for(EthHandler peer : peers) {
                peer.changeState(SyncState.IDLE);
            }
            blockchain.getQueue().setBestHash(bestHash);
            masterPeer.changeState(SyncState.HASH_RETRIEVING);
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            for(EthHandler peer : peers) {
                peer.changeState(SyncState.BLOCK_RETRIEVING);
            }
        }
        if(newState == SyncState.DONE_SYNC) {
            //TODO handle sync DONE
        }
        this.state = newState;
    }

    private void initiateConnection(Node node) {
        ethereum.connect(node);
        synchronized (connectTimestamps) {
            connectTimestamps.put(Hex.toHexString(node.getId()), System.currentTimeMillis());
        }
    }

    private boolean isHashRetrieving() {
        return state == SyncState.HASH_RETRIEVING;
    }
}
