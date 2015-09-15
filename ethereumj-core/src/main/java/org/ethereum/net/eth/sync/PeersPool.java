package org.ethereum.net.eth.sync;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
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
    private static final long DEFAULT_BAN_TIMEOUT = minutesToMillis(30);
    private static final long CONNECTION_TIMEOUT = secondsToMillis(30);

    private final Map<ByteArrayWrapper, Channel> activePeers = new HashMap<>();
    private final Set<Channel> bannedPeers = new HashSet<>();
    private final Map<String, Integer> disconnectHits = new HashMap<>();
    private final Map<String, Long> bans = new HashMap<>();
    private final Map<String, Long> pendingConnections = new HashMap<>();

    @Autowired
    private Ethereum ethereum;

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
        }
        synchronized (pendingConnections) {
            pendingConnections.remove(peer.getPeerId());
        }

        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));
    }

    public void remove(Channel peer) {
        synchronized (activePeers) {
            activePeers.remove(peer.getNodeIdWrapper());
        }
    }

    public void removeAll(Collection<Channel> removed) {
        synchronized (activePeers) {
            activePeers.values().removeAll(removed);
        }
    }

    @Nullable
    public Channel getBest() {
        synchronized (activePeers) {
            if (activePeers.isEmpty()) {
                return null;
            }
            return Collections.max(activePeers.values(), new Comparator<Channel>() {
                @Override
                public int compare(Channel p1, Channel p2) {
                    return p1.getTotalDifficulty().compareTo(p2.getTotalDifficulty());
                }
            });
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

        synchronized (activePeers) {
            activePeers.remove(peer.getNodeIdWrapper());
            bannedPeers.remove(peer);
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
            ethereum.connect(node);
            pendingConnections.put(node.getHexId(), timeAfterMillis(CONNECTION_TIMEOUT));
        }
    }

    public void ban(Channel peer) {
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

    public void changeState(SyncStateName newState, Functional.Predicate<Channel> filter) {
        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                if (filter.test(peer)) {
                    peer.changeSyncState(newState);
                }
            }
        }
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
                            "Peer {} | {} minutes ago",
                            Utils.getNodeIdShort(e.getKey()),
                            millisToMinutes(System.currentTimeMillis() - (e.getValue() - DEFAULT_BAN_TIMEOUT))
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
