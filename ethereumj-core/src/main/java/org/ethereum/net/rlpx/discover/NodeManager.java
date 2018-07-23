/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.rlpx.discover;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.PeerSource;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.ethereum.util.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
public class NodeManager implements Consumer<DiscoveryEvent>{
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private final boolean PERSIST;

    private static final long LISTENER_REFRESH_RATE = 1000;
    private static final long DB_COMMIT_RATE = 1 * 60 * 1000;
    static final int MAX_NODES = 2000;
    static final int NODES_TRIM_THRESHOLD = 3000;

    PeerConnectionTester peerConnectionManager;
    PeerSource peerSource;
    EthereumListener ethereumListener;
    SystemProperties config = SystemProperties.getDefault();

    Consumer<DiscoveryEvent> messageSender;

    NodeTable table;
    private Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
    final ECKey key;
    final Node homeNode;
    private List<Node> bootNodes;

    // option to handle inbounds only from known peers (i.e. which were discovered by ourselves)
    boolean inboundOnlyFromKnownNodes = false;

    private boolean discoveryEnabled;

    private Map<DiscoverListener, ListenerHandler> listeners = new IdentityHashMap<>();

    private boolean inited = false;
    private Timer logStatsTimer = new Timer();
    private Timer nodeManagerTasksTimer = new Timer("NodeManagerTasks");;
    private ScheduledExecutorService pongTimer;

    @Autowired
    public NodeManager(SystemProperties config, EthereumListener ethereumListener,
                       ApplicationContext ctx, PeerConnectionTester peerConnectionManager) {
        this.config = config;
        this.ethereumListener = ethereumListener;
        this.peerConnectionManager = peerConnectionManager;

        PERSIST = config.peerDiscoveryPersist();
        if (PERSIST) peerSource = ctx.getBean(PeerSource.class);
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

        this.pongTimer = Executors.newSingleThreadScheduledExecutor();
        for (Node node : config.peerActive()) {
            getNodeHandler(node).getNodeStatistics().setPredefined(true);
        }
    }

    public ScheduledExecutorService getPongTimer() {
        return pongTimer;
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
        logger.info("Reading Node statistics from DB: " + peerSource.getNodes().size() + " nodes.");
        for (Pair<Node, Integer> nodeElement : peerSource.getNodes()) {
            getNodeHandler(nodeElement.getLeft()).getNodeStatistics().setPersistedReputation(nodeElement.getRight());
        }
    }

    private void dbWrite() {
        List<Pair<Node, Integer>> batch = new ArrayList<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                batch.add(Pair.of(handler.getNode(), handler.getNodeStatistics().getPersistedReputation()));
            }
        }
        peerSource.clear();
        for (Pair<Node, Integer> nodeElement : batch) {
            peerSource.getNodes().add(nodeElement);
        }
        peerSource.getNodes().flush();
        logger.info("Write Node statistics to DB: " + peerSource.getNodes().size() + " nodes.");
    }

    public void setMessageSender(Consumer<DiscoveryEvent> messageSender) {
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

    public synchronized NodeHandler getNodeHandler(Node n) {
        String key = getKey(n);
        NodeHandler ret = nodeHandlerMap.get(key);
        if (ret == null) {
            trimTable();
            ret = new NodeHandler(n ,this);
            nodeHandlerMap.put(key, ret);
            logger.debug(" +++ New node: " + ret + " " + n);
            if (!n.isDiscoveryNode() && !n.getHexId().equals(homeNode.getHexId())) {
                ethereumListener.onNodeDiscovered(ret.getNode());
            }
        } else if (ret.getNode().isDiscoveryNode() && !n.isDiscoveryNode()) {
            // we found discovery node with same host:port,
            // replace node with correct nodeId
            ret.node = n;
            if (!n.getHexId().equals(homeNode.getHexId())) {
                ethereumListener.onNodeDiscovered(ret.getNode());
            }
            logger.debug(" +++ Found real nodeId for discovery endpoint {}", n);
        }

        return ret;
    }

    private void trimTable() {
        if (nodeHandlerMap.size() > NODES_TRIM_THRESHOLD) {

            List<NodeHandler> sorted = new ArrayList<>(nodeHandlerMap.values());
            // reverse sort by reputation
            sorted.sort((o1, o2) -> o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation());

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
        return getNodeHandler(n).getNodeStatistics();
    }

    /**
     * Checks whether peers with such InetSocketAddress has penalize disconnect record
     * @param addr  Peer address
     * @return true if penalized, false if not or no records
     */
    public boolean isReputationPenalized(InetSocketAddress addr) {
        return getNodeStatistics(new Node(new byte[0], addr.getHostString(),
                addr.getPort())).isReputationPenalized();
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
     * Returns limited list of nodes matching {@code predicate} criteria<br>
     * The nodes are sorted then by their totalDifficulties
     *
     * @param predicate only those nodes which are satisfied to its condition are included in results
     * @param limit max size of returning list
     *
     * @return list of nodes matching criteria
     */
    public List<NodeHandler> getNodes(
            Predicate<NodeHandler> predicate,
            int limit    ) {
        ArrayList<NodeHandler> filtered = new ArrayList<>();
        synchronized (this) {
            for (NodeHandler handler : nodeHandlerMap.values()) {
                if (predicate.test(handler)) {
                    filtered.add(handler);
                }
            }
        }
        filtered.sort((o1, o2) -> o2.getNodeStatistics().getEthTotalDifficulty().compareTo(
                o1.getNodeStatistics().getEthTotalDifficulty()));
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
    public synchronized void addDiscoverListener(DiscoverListener listener, Predicate<NodeStatistics> filter) {
        listeners.put(listener, new ListenerHandler(listener, filter));
    }

    public synchronized void removeDiscoverListener(DiscoverListener listener) {
        listeners.remove(listener);
    }

    public synchronized String dumpAllStatistics() {
        List<NodeHandler> l = new ArrayList<>(nodeHandlerMap.values());
        l.sort((o1, o2) -> -(o1.getNodeStatistics().getReputation() - o2.getNodeStatistics().getReputation()));

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

    /**
     * @return home node if config defines it as public, otherwise null
     */
    Node getPublicHomeNode() {
        if (config.isPublicHomeNode()) {
            return homeNode;
        }
        return null;
    }

    public void close() {
        peerConnectionManager.close();
        try {
            nodeManagerTasksTimer.cancel();
            if (PERSIST) {
                try {
                    dbWrite();
                } catch (Throwable e) {     // IllegalAccessError is expected
                    // NOTE: logback stops context right after shutdown initiated. It is problematic to see log output
                    // System out could help
                    logger.warn("Problem during NodeManager persist in close: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Problems canceling nodeManagerTasksTimer", e);
        }
        try {
            logger.info("Cancelling pongTimer");
            pongTimer.shutdownNow();
        } catch (Exception e) {
            logger.warn("Problems cancelling pongTimer", e);
        }
        try {
            logStatsTimer.cancel();
        } catch (Exception e) {
            logger.warn("Problems canceling logStatsTimer", e);
        }
    }

    private class ListenerHandler {
        Map<NodeHandler, Object> discoveredNodes = new IdentityHashMap<>();
        DiscoverListener listener;
        Predicate<NodeStatistics> filter;

        ListenerHandler(DiscoverListener listener, Predicate<NodeStatistics> filter) {
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
