package org.ethereum.net.rlpx.discover;

import org.apache.commons.codec.binary.Hex;
import org.ethereum.config.SystemProperties;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.rlpx.Node;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * Makes test RLPx connection to the peers to acquire statistics
 *
 * Created by Anton Nashatyrev on 17.07.2015.
 */
@Component
public class PeerConnectionTester {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private static final int ConnectThreads = SystemProperties.CONFIG.peerDiscoveryWorkers();
    private static final long ReconnectPeriod = SystemProperties.CONFIG.peerDiscoveryTouchPeriod() * 1000;
    private static final long ReconnectMaxPeers = SystemProperties.CONFIG.peerDiscoveryTouchMaxNodes();

    @Autowired
    private WorldManager worldManager;

    // NodeHandler instance should be unique per Node instance
    private Map<NodeHandler, ?> connectedCandidates = new IdentityHashMap<>();

    // executor with Queue which picks up the Node with the best reputation
    private ExecutorService peerConnectionPool = new ThreadPoolExecutor(ConnectThreads,
            ConnectThreads, 0L, TimeUnit.SECONDS,
            new MutablePriorityQueue<Runnable, ConnectTask>(new Comparator<ConnectTask>() {
                @Override
                public int compare(ConnectTask h1, ConnectTask h2) {
                    return h2.nodeHandler.getNodeStatistics().getReputation() -
                            h1.nodeHandler.getNodeStatistics().getReputation();
                }
            }));

    private Timer reconnectTimer = new Timer("DiscoveryReconnectTimer");
    private int reconnectPeersCount = 0;


    private class ConnectTask implements Runnable {
        NodeHandler nodeHandler;

        public ConnectTask(NodeHandler nodeHandler) {
            this.nodeHandler = nodeHandler;
        }

        @Override
        public void run() {
            try {
                if (nodeHandler != null) {
                    nodeHandler.getNodeStatistics().rlpxConnectionAttempts.add();
                    logger.debug("Trying node connection: " + nodeHandler);
                    Node node = nodeHandler.getNode();
                    worldManager.getActivePeer().connect(node.getHost(), node.getPort(),
                            Hex.encodeHexString(node.getId()), true);
                    logger.debug("Terminated node connection: " + nodeHandler);
                    nodeHandler.getNodeStatistics().disconnected();
                    if (!nodeHandler.getNodeStatistics().getEthTotalDifficulty().equals(BigInteger.ZERO) &&
                            ReconnectPeriod > 0 && (reconnectPeersCount < ReconnectMaxPeers || ReconnectMaxPeers == -1)) {
                        // trying to keep good peers information up-to-date
                        reconnectPeersCount++;
                        reconnectTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                logger.debug("Trying the node again: " + nodeHandler);
                                peerConnectionPool.execute(new ConnectTask(nodeHandler));
                                reconnectPeersCount--;
                            }
                        }, ReconnectPeriod);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public PeerConnectionTester() {
    }

    public void nodeStatusChanged(final NodeHandler nodeHandler) {
        if (!connectedCandidates.containsKey(nodeHandler)) {
            logger.debug("Submitting node for RLPx connection : " + nodeHandler);
            connectedCandidates.put(nodeHandler, null);
            peerConnectionPool.execute(new ConnectTask(nodeHandler));
        }
    }

    /**
     * The same as PriorityBlockQueue but with assumption that elements are mutable
     * and priority changes after enqueueing, thus the list is sorted by priority
     * each time the head queue element is requested.
     * The class has poor synchronization since the prioritization might be approximate
     * though the implementation should be inheritedly thread-safe
     */
    public static class MutablePriorityQueue<T, C extends T> extends LinkedBlockingQueue<T> {
        Comparator<C> comparator;

        public MutablePriorityQueue(Comparator<C> comparator) {
            this.comparator = comparator;
        }

        @Override
        public T take() throws InterruptedException {
            if (isEmpty()) {
                return super.take();
            } else {
                T ret = Collections.min(this, (Comparator<? super T>) comparator);
                remove(ret);
                return ret;
            }
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            if (isEmpty()) {
                return super.poll(timeout, unit);
            } else {
                T ret = Collections.min(this, (Comparator<? super T>) comparator);
                remove(ret);
                return ret;
            }
        }

        @Override
        public T poll() {
            if (isEmpty()) {
                return super.poll();
            } else {
                T ret = Collections.min(this, (Comparator<? super T>) comparator);
                remove(ret);
                return ret;
            }
        }

        @Override
        public T peek() {
            if (isEmpty()) {
                return super.peek();
            } else {
                T ret = Collections.min(this, (Comparator<? super T>) comparator);
                return ret;
            }
        }
    }

}
