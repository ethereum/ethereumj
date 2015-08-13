package org.ethereum.net.eth.sync;

import org.ethereum.facade.Ethereum;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.rlpx.Node;
import org.ethereum.util.CollectionUtils;
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
public class PeersPool implements Iterable<EthHandler> {

    public static final Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = 3; // 3 seconds

    private static final int DISCONNECT_HITS_THRESHOLD = 5;
    private static final long DEFAULT_BAN_TIMEOUT = minutesToMillis(30);
    private static final long CONNECTION_TIMEOUT = secondsToMillis(30);

    private final List<EthHandler> peers = new ArrayList<>();
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

    public void add(EthHandler peer) {
        synchronized (peers) {
            peers.add(peer);
        }
        synchronized (pendingConnections) {
            pendingConnections.remove(peer.getPeerId());
        }

        logger.info("Peer {}: added to pool", Utils.getNodeIdShort(peer.getPeerId()));
    }

    public void remove(EthHandler peer) {
        synchronized (peers) {
            peers.remove(peer);
        }
    }

    public void removeAll(Collection<EthHandler> removed) {
        synchronized (peers) {
            peers.removeAll(removed);
        }
    }

    @Nullable
    public EthHandler getBest() {
        synchronized (peers) {
            if (peers.isEmpty()) {
                return null;
            }
            return Collections.max(peers, new Comparator<EthHandler>() {
                @Override
                public int compare(EthHandler p1, EthHandler p2) {
                    return p1.getTotalDifficulty().compareTo(p2.getTotalDifficulty());
                }
            });
        }
    }

    public void onDisconnect(EthHandler peer) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: disconnected",
                peer.getPeerIdShort()
        );

        peer.onDisconnect();

        synchronized (peers) {
            peers.remove(peer);
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

    public void ban(EthHandler peer) {
        synchronized (bans) {
            banInner(peer, DEFAULT_BAN_TIMEOUT);
        }
    }

    private void banInner(EthHandler peer, long millis) {
        bans.put(peer.getPeerId(), timeAfterMillis(millis));
    }

    public Set<String> nodesInUse() {
        Set<String> ids = new HashSet<>();
        synchronized (peers) {
            for (EthHandler peer : peers) {
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

    public void changeState(SyncState newState) {
        synchronized (peers) {
            for (EthHandler peer : peers) {
                peer.changeState(newState);
            }
        }
    }

    public void changeState(SyncState newState, Functional.Predicate<EthHandler> filter) {
        synchronized (peers) {
            for (EthHandler peer : peers) {
                if (filter.test(peer)) {
                    peer.changeState(newState);
                }
            }
        }
    }

    public boolean isEmpty() {
        return peers.isEmpty();
    }

    public int activeCount() {
        return peers.size();
    }

    @Override
    public Iterator<EthHandler> iterator() {
        synchronized (peers) {
            return new ArrayList<>(peers).iterator();
        }
    }

    void logActivePeers() {
        if (peers.size() > 0) {
            logger.info("\n");
            logger.info("Active peers");
            logger.info("============");
            for(EthHandler peer : this) {
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
        synchronized (bans) {
            removeByTimeout(bans);
        }
    }

    private void processConnections() {
        synchronized (pendingConnections) {
            removeByTimeout(pendingConnections);
        }
    }

    private void removeByTimeout(Map<?, Long> map) {
        final Long now = System.currentTimeMillis();
        Set<Long> released = CollectionUtils.selectSet(map.values(), new Functional.Predicate<Long>() {
            @Override
            public boolean test(Long timestamp) {
                return now >= timestamp;
            }
        });
        if(!released.isEmpty()) {
            map.values().removeAll(released);
        }
    }
}
