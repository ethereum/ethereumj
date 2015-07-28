package org.ethereum.net.eth;

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

import javax.annotation.PostConstruct;
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
    private static final long BLOCKS_GAP_THRESHOLD = 20;

    private SyncState state = SyncState.INIT;
    private EthHandler masterPeer;
    private final List<EthHandler> peers = new CopyOnWriteArrayList<>();
    private boolean shouldNotifyDone = true;
    private int maxHashesAsk;
    private byte[] bestHash;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService logWorker = Executors.newSingleThreadScheduledExecutor();

    private BigInteger lowerUsefulDifficulty;

    private final Map<String, Long> connectTimestamps = new HashMap<>();

    private DiscoverListener discoverListener;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private EthereumListener ethereumListener;

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
            if(peer.isSyncDone()) {
                changeState(SyncState.DONE_SYNC);
                return;
            }

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
        if((state == SyncState.BLOCK_RETRIEVING || state == SyncState.DONE_SYNC)
                && !blockchain.getQueue().getHashStore().isEmpty()) {
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
        if(state == SyncState.DONE_SYNC) {
            return;
        }

        synchronized (connectTimestamps) {
            connectTimestamps.remove(peer.getPeerId());
        }
        peer.changeState(SyncState.IDLE);
        peers.remove(peer);
    }

    public void addPeer(EthHandler peer) {
        if(state == SyncState.DONE_SYNC) {
            return;
        }

        synchronized (connectTimestamps) {
            connectTimestamps.remove(peer.getPeerId());
        }

        BigInteger peerTotalDifficulty = peer.getTotalDifficulty();
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
        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));

        BlockQueue chainQueue = blockchain.getQueue();
        BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();

        if ((highestKnownTotalDifficulty == null ||
            !isIn20PercentRange(highestKnownTotalDifficulty, peerTotalDifficulty))) {

            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownTotalDifficulty == null ? "0" : highestKnownTotalDifficulty.toString()
            );
            logger.debug(
                    "Peer {}: best hash [{}]",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    Hex.toHexString(peer.getBestHash())
            );
            runHashRetrieving(CONFIG.maxHashesAsk(), null);
        } else if(state == SyncState.BLOCK_RETRIEVING) {
            peer.changeState(SyncState.BLOCK_RETRIEVING);
        }
    }

    public void recoverGap(BlockWrapper wrapper) {
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
        if(wrapper.isNewBlock() && gap > BLOCKS_GAP_THRESHOLD) {
            if(blockchain.getQueue().isHashesEmpty() &&
                    (state == SyncState.BLOCK_RETRIEVING || state == SyncState.DONE_SYNC)) {
                maxHashesAsk = gap > CONFIG.maxHashesAsk() ? CONFIG.maxHashesAsk() : (int) gap;
                logger.debug("Recover NEW blocks gap, block.number [{}], block.hash [{}]", wrapper.getNumber(), wrapper.getShortHash());
                runHashRetrieving(maxHashesAsk, wrapper.getHash());
            } else if(logger.isInfoEnabled()) {
                logger.info("We are in {} state, postpone NEW blocks gap recovery", state, wrapper.getNumber());
            }
        } else {
            logger.info("Forcing parent downloading for block.number [{}]", wrapper.getNumber());
            blockchain.getQueue().getHashStore().addFirst(wrapper.getParentHash());
        }
    }

    public void runHashRetrieving(int maxHashesAsk, byte[] bestHash) {
        this.maxHashesAsk = maxHashesAsk;
        this.bestHash = bestHash;
        changeState(SyncState.HASH_RETRIEVING);
    }

    public synchronized void changeState(SyncState newState) {
        if(state == SyncState.DONE_SYNC) {
            return;
        }
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
            changePeersState(SyncState.IDLE);
            BlockQueue queue = blockchain.getQueue();
            queue.setHighestTotalDifficulty(masterPeer.getTotalDifficulty());
            if(bestHash == null) {
                bestHash = masterPeer.getBestHash();
            }
            queue.setBestHash(bestHash);
            masterPeer.setMaxHashesAsk(maxHashesAsk);
            masterPeer.changeState(SyncState.HASH_RETRIEVING);
            logger.info("Hashes retrieving initiated, best known hash [{}], askLimit [{}]", Hex.toHexString(bestHash), maxHashesAsk);
            logger.debug("Our best block hash [{}]", Hex.toHexString(blockchain.getBestBlockHash()));
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            changePeersState(SyncState.BLOCK_RETRIEVING);
            logger.info("Blocks retrieving initiated");
        }
        if(newState == SyncState.DONE_SYNC) {
            changePeersState(SyncState.DONE_SYNC);
            if(shouldNotifyDone) {
                shouldNotifyDone = false;
                ethereumListener.onSyncDone();
            }
            logger.info("Main synchronization is finished");
        }
        this.state = newState;
    }

    private void changePeersState(SyncState newState) {
        for (EthHandler peer : peers) {
            peer.changeState(newState);
        }
    }

    private void initiateConnection(Node node) {
        synchronized (connectTimestamps) {
            String nodeId = Hex.toHexString(node.getId());
            if(connectTimestamps.containsKey(nodeId)) {
                return;
            }
            ethereum.connect(node);
            connectTimestamps.put(nodeId, System.currentTimeMillis());
        }
    }

    private boolean isHashRetrieving() {
        return state == SyncState.HASH_RETRIEVING;
    }
}
