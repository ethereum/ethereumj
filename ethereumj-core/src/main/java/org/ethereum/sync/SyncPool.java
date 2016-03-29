package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.server.Channel;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;
import static org.ethereum.util.BIUtil.isIn20PercentRange;
import static org.ethereum.util.TimeUtils.*;

/**
 * <p>Encapsulates logic which manages peers involved in blockchain sync</p>
 *
 * Holds connections, bans, disconnects and other peers logic<br>
 * The pool is completely threadsafe<br>
 * Implements {@link Iterable} and can be used in "foreach" loop<br>
 * Used by {@link SyncManager}
 *
 * @author Mikhail Kalinin
 * @since 10.08.2015
 */
@Component
public class SyncPool implements Iterable<Channel> {

    public static final Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = 3; // 3 seconds

    private static final long CONNECTION_TIMEOUT = secondsToMillis(30);

    private final Map<ByteArrayWrapper, Channel> peers = new HashMap<>();
    private final List<Channel> activePeers = Collections.synchronizedList(new ArrayList<Channel>());
    private final Map<String, Long> pendingConnections = new HashMap<>();

    private BigInteger lowerUsefulDifficulty = BigInteger.ZERO;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private EthereumListener ethereumListener;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private SystemProperties config;

    @Autowired
    private NodeManager nodeManager;

    @PostConstruct
    public void init() {

        if (!config.isSyncEnabled()) return;

        updateLowerUsefulDifficulty();

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            heartBeat();
                            processConnections();
                            updateLowerUsefulDifficulty();
                            fillUp();
                            prepareActive();
                        } catch (Throwable t) {
                            logger.error("Unhandled exception", t);
                        }
                    }
                }, WORKER_TIMEOUT, WORKER_TIMEOUT, TimeUnit.SECONDS
        );
    }

    public synchronized void add(Channel peer) {

        if (!config.isSyncEnabled()) return;

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: adding",
                peer.getPeerIdShort()
        );

        if (!isIn20PercentRange(peer.getTotalDifficulty(), lowerUsefulDifficulty)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: difficulty significantly lower than ours: {} vs {}, skipping",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peer.getTotalDifficulty().toString(),
                    lowerUsefulDifficulty.toString()
            );
            return;
        }

//        synchronized (peers) {
            peers.put(peer.getNodeIdWrapper(), peer);
//        }

//        synchronized (pendingConnections) {
            pendingConnections.remove(peer.getPeerId());
//        }

        ethereumListener.onPeerAddedToSyncPool(peer);

        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));
    }

    public synchronized void remove(Channel peer) {
//        synchronized (peers) {
            peers.values().remove(peer);
//        }
    }

    @Nullable
    public synchronized Channel getMaster() {

//        synchronized (peers) {

            for (Channel peer : peers.values())
                if (peer.isMaster()) {
                    return peer;
                }

            return null;
//        }
    }

    @Nullable
    public synchronized Channel getMasterCandidate() {
//        synchronized (activePeers) {

            if (activePeers.isEmpty()) return null;
            return activePeers.get(0);
//        }
    }

    @Nullable
    public synchronized Channel getBestIdle() {
//        synchronized (activePeers) {

            for (Channel peer : activePeers) {
                if (peer.isIdle())
                    return peer;
            }
//        }

        return null;
    }

    @Nullable
    public synchronized Channel getByNodeId(byte[] nodeId) {
        return peers.get(new ByteArrayWrapper(nodeId));
    }

    public synchronized void onDisconnect(Channel peer) {

        if (peer.getNodeId() == null) return;

        boolean existed;
//        synchronized (peers) {
            existed = peers.values().remove(peer);
//            synchronized (activePeers) {
                activePeers.remove(peer);
//            }
//        }

        // do not count disconnects for nodeId
        // if exact peer is not an active one
        if (!existed) return;

        logger.info("Peer {}: disconnected", peer.getPeerIdShort());
    }

    public synchronized void connect(Node node) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: initiate connection",
                node.getHexIdShort()
        );
        if (isInUse(node.getHexId())) {
            if (logger.isTraceEnabled()) logger.trace(
                    "Peer {}: connection already initiated",
                    node.getHexIdShort()
            );
            return;
        }

//        synchronized (pendingConnections) {
            ethereum.connect(node);
            pendingConnections.put(node.getHexId(), timeAfterMillis(CONNECTION_TIMEOUT));
//        }
    }

    public synchronized Set<String> nodesInUse() {
        Set<String> ids = new HashSet<>();
//        synchronized (peers) {
            for (Channel peer : peers.values()) {
                ids.add(peer.getPeerId());
            }
//        }
//        synchronized (pendingConnections) {
            ids.addAll(pendingConnections.keySet());
//        }
        return ids;
    }

    public synchronized boolean isInUse(String nodeId) {
        return nodesInUse().contains(nodeId);
    }

    public synchronized boolean isEmpty() {
        return peers.isEmpty();
    }

    @Override
    public synchronized Iterator<Channel> iterator() {
//        synchronized (peers) {
            return new ArrayList<>(peers.values()).iterator();
//        }
    }

    synchronized void logActivePeers() {
//        synchronized (activePeers) {
            if (activePeers.isEmpty()) return;

            logger.info("\n");
            logger.info("Active peers");
            logger.info("============");
            for (Channel peer : activePeers) peer.logSyncStats();
//        }
    }

    private void processConnections() {
//        synchronized (pendingConnections) {
            Set<String> exceeded = getTimeoutExceeded(pendingConnections);
            pendingConnections.keySet().removeAll(exceeded);
//        }
    }

    private Set<String> getTimeoutExceeded(Map<String, Long> map) {
        Set<String> exceeded = new HashSet<>();
        final Long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> e : map.entrySet()) {
            if (now >= e.getValue()) {
                exceeded.add(e.getKey());
            }
        }
        return exceeded;
    }

    private void fillUp() {
        int lackSize = config.maxActivePeers() - peers.size();
        if(lackSize <= 0) return;

        Set<String> nodesInUse = nodesInUse();

        List<NodeHandler> newNodes = nodeManager.getBestEthNodes(nodesInUse, lowerUsefulDifficulty, lackSize);
        if (lackSize > 0 && newNodes.isEmpty()) {
            newNodes = nodeManager.getBestEthNodes(nodesInUse, BigInteger.ZERO, lackSize);
        }

        if (logger.isTraceEnabled()) {
            logDiscoveredNodes(newNodes);
        }

        for(NodeHandler n : newNodes) {
            connect(n.getNode());
        }
    }

    private void prepareActive() {
//        synchronized (peers) {

            List<Channel> active = new ArrayList<>(peers.values());

            if (active.isEmpty()) return;

            // filtering by 20% from top difficulty
            Collections.sort(active, new Comparator<Channel>() {
                @Override
                public int compare(Channel c1, Channel c2) {
                    return c2.getTotalDifficulty().compareTo(c1.getTotalDifficulty());
                }
            });

            BigInteger highestDifficulty = active.get(0).getTotalDifficulty();
            int thresholdIdx = min(config.syncPeerCount(), active.size()) - 1;

            for (int i = thresholdIdx; i >= 0; i--) {
                if (isIn20PercentRange(active.get(i).getTotalDifficulty(), highestDifficulty)) {
                    thresholdIdx = i;
                    break;
                }
            }

            List<Channel> filtered = active.subList(0, thresholdIdx + 1);

            // sorting by latency in asc order
            Collections.sort(filtered, new Comparator<Channel>() {
                @Override
                public int compare(Channel c1, Channel c2) {
                    return Double.valueOf(c1.getPeerStats().getAvgLatency()).compareTo(c2.getPeerStats().getAvgLatency());
                }
            });

            synchronized (activePeers) {
                activePeers.clear();
                activePeers.addAll(filtered);
            }
//        }
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

    private void updateLowerUsefulDifficulty() {
        BigInteger td = blockchain.getTotalDifficulty();
        if (td.compareTo(lowerUsefulDifficulty) > 0) {
            lowerUsefulDifficulty = td;
        }
    }

    private void heartBeat() {
        for (Channel peer : this) {
            if (!peer.isIdle() && peer.getSyncStats().secondsSinceLastUpdate() > config.peerChannelReadTimeout()) {
                logger.info("Peer {}: no response after %d seconds", peer.getPeerIdShort(), config.peerChannelReadTimeout());
                peer.dropConnection();
            }
        }
    }
}
