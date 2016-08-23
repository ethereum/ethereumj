package org.ethereum.net.rlpx.discover;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.Functional;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

import static java.lang.Math.min;

/**
 * The central class for Peer Discovery machinery.
 *
 * The NodeManager manages info on all the Nodes discovered by the peer discovery
 * protocol, routes protocol messages to the corresponding NodeHandlers and
 * supplies the info about discovered Nodes and their usage statistics
 *
 * Created by Anton Nashatyrev on 16.07.2015.
 */
@Component
public class NodeManager implements Functional.Consumer<DiscoveryEvent>{
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    // to avoid checking for null
    private static NodeStatistics DUMMY_STAT = new NodeStatistics(new Node(new byte[0], "dummy.node", 0));
    private boolean PERSIST;

    private static final long LISTENER_REFRESH_RATE = 1000;
    private static final long DB_COMMIT_RATE = 1 * 60 * 1000;
    static final int MAX_NODES = 2000;
    static final int NODES_TRIM_THRESHOLD = 3000;

    PeerConnectionTester peerConnectionManager;
    MapDBFactory mapDBFactory;
    EthereumListener ethereumListener;
    SystemProperties config = SystemProperties.getDefault();

    Functional.Consumer<DiscoveryEvent> messageSender;

    NodeTable table;
    private Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
    ECKey key;
    Node homeNode;
    private List<Node> bootNodes;

    // option to handle inbounds only from known peers (i.e. which were discovered by ourselves)
    boolean inboundOnlyFromKnownNodes = false;

    private boolean discoveryEnabled;

    private Map<DiscoverListener, ListenerHandler> listeners = new IdentityHashMap<>();

    private DB db;
    private HTreeMap<Node, NodeStatistics.Persistent> nodeStatsDB;
    private boolean inited = false;
    private Timer logStatsTimer = new Timer();
    private Timer nodeManagerTasksTimer = new Timer("NodeManagerTasks");;

    @Autowired
    public NodeManager(SystemProperties config, EthereumListener ethereumListener, MapDBFactory mapDBFactory, PeerConnectionTester peerConnectionManager) {
        this.config = config;
        this.ethereumListener = ethereumListener;
        this.mapDBFactory = mapDBFactory;
        this.peerConnectionManager = peerConnectionManager;

        PERSIST = config.peerDiscoveryPersist();
        discoveryEnabled = config.peerDiscovery();

        key = config.getMyKey();
        homeNode = new Node(config.nodeId(), config.externalIp(), config.listenPort());
        table = new NodeTable(homeNode, config.isPublicHomeNode());

        logStatsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.trace("Statistics:\n {}", dumpAllStatistics());
            }
        }, 1 * 1000, 60 * 1000);

        for (Node node : config.peerActive()) {
            getNodeHandler(node).getNodeStatistics().setPredefined(true);
        }
    }

    void setBootNodes(List<Node> bootNodes) {
        this.bootNodes = bootNodes;
    }

    void channelActivated() {
        // channel activated now can send messages
        if (!inited) {
            // no another init on a new channel activation
            inited = true;

            // this task is done asynchronously with some fixed rate
            // to avoid any overhead in the NodeStatistics classes keeping them lightweight
            // (which might be critical since they might be invoked from time critical sections)
            nodeManagerTasksTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    processListeners();
                }
            }, LISTENER_REFRESH_RATE, LISTENER_REFRESH_RATE);

            if (PERSIST) {
                dbRead();
                nodeManagerTasksTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        dbWrite();
                    }
                }, DB_COMMIT_RATE, DB_COMMIT_RATE);
            }

            for (Node node : bootNodes) {
                getNodeHandler(node);
            }
        }
    }

    private void dbRead() {
        try {
            db = mapDBFactory.createTransactionalDB("network/discovery");
            if (config.databaseReset()) {
                logger.info("Resetting DB Node statistics...");
                db.delete("nodeStats");
            }
            nodeStatsDB = db.hashMapCreate("nodeStats")
                    .keySerializer(Node.MapDBSerializer)
                    .valueSerializer(NodeStatistics.Persistent.MapDBSerializer)
                    .makeOrGet();

            logger.info("Reading Node statistics from DB: " + nodeStatsDB.size() + " nodes.");
            for (Map.Entry<Node, NodeStatistics.Persistent> entry : nodeStatsDB.entrySet()) {
                getNodeHandler(entry.getKey()).getNodeStatistics().setPersistedData(entry.getValue());
            }
        } catch (Exception e) {
            try {
                logger.warn("Error reading db. Recreating from scratch...");
                logger.debug("Error reading db. Recreating from scratch:", e);

                db.delete("nodeStats");
                nodeStatsDB = db.hashMap("nodeStats");
            } catch (Exception e1) {
                logger.error("DB recreation has been failed. Node statistics persistence disabled. The problem needs to be fixed manually.", e1);
            }
        }
    }

    private void dbWrite() {
        Map<Node, NodeStatistics.Persistent> batch = new HashMap<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                batch.put(handler.getNode(), handler.getNodeStatistics().getPersistent());
            }
        }
        nodeStatsDB.clear();
        nodeStatsDB.putAll(batch);
        db.commit();
        logger.info("Write Node statistics to DB: " + nodeStatsDB.size() + " nodes.");
    }

    public void setMessageSender(Functional.Consumer<DiscoveryEvent> messageSender) {
        this.messageSender = messageSender;
    }

    private String getKey(Node n) {
        return getKey(new InetSocketAddress(n.getHost(), n.getPort()));
    }

    private String getKey(InetSocketAddress address) {
        InetAddress addr = address.getAddress();
        // addr == null if the hostname can't be resolved
        return (addr == null ? address.getHostString() : addr.getHostAddress()) + ":" + address.getPort();
    }

    synchronized  NodeHandler getNodeHandler(Node n) {
        String key = getKey(n);
        NodeHandler ret = nodeHandlerMap.get(key);
        if (ret == null) {
            trimTable();
            ret = new NodeHandler(n ,this);
            nodeHandlerMap.put(key, ret);
            logger.debug(" +++ New node: " + ret);
            ethereumListener.onNodeDiscovered(ret.getNode());
        }
        return ret;
    }

    private void trimTable() {
        if (nodeHandlerMap.size() > NODES_TRIM_THRESHOLD) {

            List<NodeHandler> sorted = new ArrayList<>(nodeHandlerMap.values());
            // reverse sort by reputation
            Collections.sort(sorted, new Comparator<NodeHandler>() {
                @Override
                public int compare(NodeHandler o1, NodeHandler o2) {
                    return o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation();
                }
            });

            for (NodeHandler handler : sorted) {
                nodeHandlerMap.remove(getKey(handler.getNode()));
                if (nodeHandlerMap.size() <= MAX_NODES) break;
            }
        }
    }


    boolean hasNodeHandler(Node n) {
        return nodeHandlerMap.containsKey(getKey(n));
    }

    public NodeTable getTable() {
        return table;
    }

    public NodeStatistics getNodeStatistics(Node n) {
        return discoveryEnabled ? getNodeHandler(n).getNodeStatistics() : DUMMY_STAT;
    }

    @Override
    public void accept(DiscoveryEvent discoveryEvent) {
        handleInbound(discoveryEvent);
    }

    public void handleInbound(DiscoveryEvent discoveryEvent) {
        Message m = discoveryEvent.getMessage();
        InetSocketAddress sender = discoveryEvent.getAddress();

        Node n = new Node(m.getNodeId(), sender.getHostString(), sender.getPort());

        if (inboundOnlyFromKnownNodes && !hasNodeHandler(n)) {
            logger.debug("=/=> (" + sender + "): inbound packet from unknown peer rejected due to config option.");
            return;
        }
        NodeHandler nodeHandler = getNodeHandler(n);

        logger.trace("===> ({}) {} [{}] {}", sender, m.getClass().getSimpleName(), nodeHandler, m);

        byte type = m.getType()[0];
        switch (type) {
            case 1:
                nodeHandler.handlePing((PingMessage) m);
                break;
            case 2:
                nodeHandler.handlePong((PongMessage) m);
                break;
            case 3:
                nodeHandler.handleFindNode((FindNodeMessage) m);
                break;
            case 4:
                nodeHandler.handleNeighbours((NeighborsMessage) m);
                break;
        }
    }

    public void sendOutbound(DiscoveryEvent discoveryEvent) {
        if (discoveryEnabled && messageSender != null) {
            logger.trace(" <===({}) {} [{}] {}", discoveryEvent.getAddress(),
                    discoveryEvent.getMessage().getClass().getSimpleName(), this, discoveryEvent.getMessage());
            messageSender.accept(discoveryEvent);
        }
    }

    public void stateChanged(NodeHandler nodeHandler, NodeHandler.State oldState, NodeHandler.State newState) {
        if (discoveryEnabled && peerConnectionManager != null) {  // peerConnectionManager can be null if component not inited yet
            peerConnectionManager.nodeStatusChanged(nodeHandler);
        }
    }

    public synchronized List<NodeHandler> getNodes(int minReputation) {
        List<NodeHandler> ret = new ArrayList<>();
        for (NodeHandler nodeHandler : nodeHandlerMap.values()) {
            if (nodeHandler.getNodeStatistics().getReputation() >= minReputation) {
                ret.add(nodeHandler);
            }
        }
        return ret;
    }

    /**
     * Returns list of unused Eth nodes with highest total difficulty<br>
     *     Search criteria:
     *     <ul>
     *         <li>not presented in {@code usedIds} collection</li>
     *         <li>eth status processing succeeded</li>
     *         <li>difficulty is higher than {@code lowerDifficulty}</li>
     *     </ul>
     *
     *
     * @param usedIds collections of ids which are excluded from results
     * @param lowerDifficulty nodes having TD lower than this value are sorted out
     * @param limit max size of returning list
     *
     * @return list of nodes with highest difficulty, ordered by TD in desc order
     */
    public List<NodeHandler> getBestEthNodes(
            final Set<String> usedIds,
            final BigInteger lowerDifficulty,
            int limit
    ) {
        return getNodes(new Functional.Predicate<NodeHandler>() {
            @Override
            public boolean test(NodeHandler handler) {
                if (usedIds.contains(handler.getNode().getHexId())) {
                    return false;
                }

                if (handler.getNodeStatistics().isPredefined()) return true;

                if (handler.getNodeStatistics().getEthTotalDifficulty() == null) {
                    return false;
                }
                return handler.getNodeStatistics().getEthTotalDifficulty().compareTo(lowerDifficulty) > 0;
            }
        }, limit);
    }

    /**
     * Returns limited list of nodes matching {@code predicate} criteria<br>
     * The nodes are sorted then by their totalDifficulties
     *
     * @param predicate only those nodes which are satisfied to its condition are included in results
     * @param limit max size of returning list
     *
     * @return list of nodes matching criteria
     */
    private List<NodeHandler> getNodes(
            Functional.Predicate<NodeHandler> predicate,
            int limit    ) {
        ArrayList<NodeHandler> filtered = new ArrayList<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                if (predicate.test(handler)) {
                    filtered.add(handler);
                }
            }
        }
        Collections.sort(filtered, new Comparator<NodeHandler>() {
            @Override
            public int compare(NodeHandler o1, NodeHandler o2) {
                return o2.getNodeStatistics().getEthTotalDifficulty().compareTo(
                        o1.getNodeStatistics().getEthTotalDifficulty());
            }
        });
        return CollectionUtils.truncate(filtered, limit);
    }

    private synchronized void processListeners() {
        for (ListenerHandler handler : listeners.values()) {
            try {
                handler.checkAll();
            } catch (Exception e) {
                logger.error("Exception processing listener: " + handler, e);
            }
        }
    }

    /**
     * Add a listener which is notified when the node statistics starts or stops meeting
     * the criteria specified by [filter] param.
     */
    public synchronized void addDiscoverListener(DiscoverListener listener, Functional.Predicate<NodeStatistics> filter) {
        listeners.put(listener, new ListenerHandler(listener, filter));
    }

    public synchronized void removeDiscoverListener(DiscoverListener listener) {
        listeners.remove(listener);
    }

    public synchronized String dumpAllStatistics() {
        List<NodeHandler> l = new ArrayList<>(nodeHandlerMap.values());
        Collections.sort(l, new Comparator<NodeHandler>() {
            public int compare(NodeHandler o1, NodeHandler o2) {
                return -(o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation());
            }
        });

        StringBuilder sb = new StringBuilder();
        int zeroReputCount = 0;
        for (NodeHandler nodeHandler : l) {
            if (nodeHandler.getNodeStatistics().getReputation() > 0) {
                sb.append(nodeHandler).append("\t").append(nodeHandler.getNodeStatistics()).append("\n");
            } else {
                zeroReputCount++;
            }
        }
        sb.append("0 reputation: ").append(zeroReputCount).append(" nodes.\n");
        return sb.toString();
    }

    public void close() {
        peerConnectionManager.close();
        try {
            nodeManagerTasksTimer.cancel();
        } catch (Exception e) {
            logger.warn("Problems canceling nodeManagerTasksTimer", e);
        }
        try {
            logStatsTimer.cancel();
        } catch (Exception e) {
            logger.warn("Problems canceling logStatsTimer", e);
        }
        try {
            logger.info("Closing discovery DB...");
            db.close();
        } catch (Throwable e) {
            logger.warn("Problems closing db", e);
        }
    }

    private class ListenerHandler {
        Map<NodeHandler, Object> discoveredNodes = new IdentityHashMap<>();
        DiscoverListener listener;
        Functional.Predicate<NodeStatistics> filter;

        ListenerHandler(DiscoverListener listener, Functional.Predicate<NodeStatistics> filter) {
            this.listener = listener;
            this.filter = filter;
        }

        void checkAll() {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                boolean has = discoveredNodes.containsKey(handler);
                boolean test = filter.test(handler.getNodeStatistics());
                if (!has && test) {
                    listener.nodeAppeared(handler);
                    discoveredNodes.put(handler, null);
                } else if (has && !test) {
                    listener.nodeDisappeared(handler);
                    discoveredNodes.remove(handler);
                }
            }
        }
    }
}
