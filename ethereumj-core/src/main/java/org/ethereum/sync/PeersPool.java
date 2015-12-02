package org.ethereum.sync;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.client.PeerClientManager;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.Functional;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.sync.SyncStateName.IDLE;
import static org.ethereum.util.BIUtil.isIn20PercentRange;
import static org.ethereum.util.BIUtil.isMoreThan;
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
public class PeersPool implements Iterable<Channel> {

    public static final Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = 3; // 3 seconds

    private static final int DISCONNECT_HITS_THRESHOLD = 5;
    private static final long DEFAULT_BAN_TIMEOUT = minutesToMillis(1);
    private static final long CONNECTION_TIMEOUT = secondsToMillis(30);

    private static final int MIN_PEERS_COUNT = 3;

    private final Map<ByteArrayWrapper, Channel> activePeers = new HashMap<>();
    private final Set<Channel> bannedPeers = new HashSet<>();
    private final Map<String, Integer> disconnectHits = new HashMap<>();
    private final Map<String, Long> bans = new HashMap<>();
    private final Map<String, Long> pendingConnections = new HashMap<>();

    @Autowired
    PeerClientManager peerClientManager;

    @PostConstruct
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        releaseBans();
                        processConnections();
                    }
                }, WORKER_TIMEOUT, WORKER_TIMEOUT, TimeUnit.SECONDS
        );
    }

    public void add(Channel peer) {
        synchronized (activePeers) {
            activePeers.put(peer.getNodeIdWrapper(), peer);
            bannedPeers.remove(peer);
        }
        synchronized (pendingConnections) {
            pendingConnections.remove(peer.getPeerId());
        }
        synchronized (bans) {
            bans.remove(peer.getPeerId());
        }

        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));
    }

    public void remove(Channel peer) {
        synchronized (activePeers) {
            activePeers.values().remove(peer);
        }
    }

    @Nullable
    public Channel getMaster() {

        synchronized (activePeers) {

            if (activePeers.isEmpty()) {
                return null;
            }

            Channel best61 = null;
            Channel best62 = null;
            int count62 = 0;
            int count61 = 0;

            for (Channel peer : activePeers.values()) {

                if (peer.getEthVersion().getCode() >= V62.getCode()) {

                    if (best62 == null || isMoreThan(peer.getTotalDifficulty(), best62.getTotalDifficulty())) {
                        best62 = peer;
                    }
                    count62++;
                } else {

                    if (best61 == null || isMoreThan(peer.getTotalDifficulty(), best61.getTotalDifficulty())) {
                        best61 = peer;
                    }
                    count61++;
                }

            }

            if (best61 == null) return best62;
            if (best62 == null) return best61;

            if (count62 >= MIN_PEERS_COUNT) return best62;
            if (count61 >= MIN_PEERS_COUNT) return best61;

            if (isIn20PercentRange(best62.getTotalDifficulty(), best61.getTotalDifficulty())) {
                return best62;
            } else {
                return best61;
            }
        }
    }

    @Nullable
    public Channel getByNodeId(byte[] nodeId) {
        return activePeers.get(new ByteArrayWrapper(nodeId));
    }

    public void onDisconnect(Channel peer) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: disconnected",
                peer.getPeerIdShort()
        );

        if (peer.getNodeId() == null) {
            return;
        }

        boolean existed;
        synchronized (activePeers) {
            existed = activePeers.values().remove(peer);
            bannedPeers.remove(peer);
        }

        // do not count disconnects for nodeId
        // if exact peer is not an active one
        if (!existed) {
            return;
        }

        synchronized (disconnectHits) {
            Integer hits = disconnectHits.get(peer.getPeerId());
            if (hits == null) {
                hits = 0;
            }
            if (hits > DISCONNECT_HITS_THRESHOLD) {
                ban(peer);
                logger.info("Peer {}: banned due to disconnects exceeding", Utils.getNodeIdShort(peer.getPeerId()));
                disconnectHits.remove(peer.getPeerId());
            } else {
                disconnectHits.put(peer.getPeerId(), hits + 1);
            }
        }
    }

    public void connect(Node node) {
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

        synchronized (pendingConnections) {
            peerClientManager.connect(node);
            pendingConnections.put(node.getHexId(), timeAfterMillis(CONNECTION_TIMEOUT));
        }
    }

    public void ban(Channel peer) {

        peer.changeSyncState(IDLE);

        synchronized (activePeers) {
            if (activePeers.containsKey(peer.getNodeIdWrapper())) {
                activePeers.remove(peer.getNodeIdWrapper());
                bannedPeers.add(peer);
            }
        }

        synchronized (bans) {
            bans.put(peer.getPeerId(), timeAfterMillis(DEFAULT_BAN_TIMEOUT));
        }
    }

    public Set<String> nodesInUse() {
        Set<String> ids = new HashSet<>();
        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                ids.add(peer.getPeerId());
            }
        }
        synchronized (bans) {
            ids.addAll(bans.keySet());
        }
        synchronized (pendingConnections) {
            ids.addAll(pendingConnections.keySet());
        }
        return ids;
    }

    public boolean isInUse(String nodeId) {
        return nodesInUse().contains(nodeId);
    }

    public void changeState(SyncStateName newState) {
        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                peer.changeSyncState(newState);
            }
        }
    }

    public void changeStateForIdles(SyncStateName newState, EthVersion compatibleVersion) {

        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                if (peer.isIdle() && peer.getEthVersion().isCompatible(compatibleVersion))
                    peer.changeSyncState(newState);
            }
        }
    }

    public void changeStateForIdles(SyncStateName newState) {

        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                if (peer.isIdle())
                    peer.changeSyncState(newState);
            }
        }
    }

    public boolean hasCompatible(EthVersion version) {

        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                if (peer.getEthVersion().isCompatible(version))
                    return true;
            }
        }

        return false;
    }

    @Nullable
    public Channel findOne(Functional.Predicate<Channel> filter) {

        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                if (filter.test(peer))
                    return peer;
            }
        }

        return null;
    }

    public boolean isEmpty() {
        return activePeers.isEmpty();
    }

    public int activeCount() {
        return activePeers.size();
    }

    @Override
    public Iterator<Channel> iterator() {
        synchronized (activePeers) {
            return new ArrayList<>(activePeers.values()).iterator();
        }
    }

    void logActivePeers() {
        if (activePeers.size() > 0) {
            logger.info("\n");
            logger.info("Active peers");
            logger.info("============");
            for(Channel peer : this) {
                peer.logSyncStats();
            }
        }
    }

    void logBannedPeers() {
        synchronized (bans) {
            if (bans.size() > 0) {
                logger.info("\n");
                logger.info("Banned peers");
                logger.info("============");
                for (Map.Entry<String, Long> e : bans.entrySet()) {
                    logger.info(
                            "Peer {} | {} seconds ago",
                            Utils.getNodeIdShort(e.getKey()),
                            millisToSeconds(System.currentTimeMillis() - (e.getValue() - DEFAULT_BAN_TIMEOUT))
                    );
                }
            }
        }
    }

    private void releaseBans() {

        Set<String> released;

        synchronized (bans) {
            released = getTimeoutExceeded(bans);

            synchronized (activePeers) {
                for (Channel peer : bannedPeers) {
                    if (released.contains(peer.getPeerId())) {
                        activePeers.put(peer.getNodeIdWrapper(), peer);
                    }
                }
                bannedPeers.removeAll(activePeers.values());
            }

            bans.keySet().removeAll(released);
        }

        synchronized (disconnectHits) {
            disconnectHits.keySet().removeAll(released);
        }
    }

    private void processConnections() {
        synchronized (pendingConnections) {
            Set<String> exceeded = getTimeoutExceeded(pendingConnections);
            pendingConnections.keySet().removeAll(exceeded);
        }
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
}