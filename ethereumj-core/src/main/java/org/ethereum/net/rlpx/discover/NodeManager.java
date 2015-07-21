package org.ethereum.net.rlpx.discover;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.ethereum.util.Functional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

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

    @Autowired
    PeerConnectionTester peerConnectionManager;

    Functional.Consumer<DiscoveryEvent> messageSender;

    NodeTable table;
    private Map<String, NodeHandler> nodeHandlerMap = new HashMap<String, NodeHandler>();
    ECKey key;
    Node homeNode;

    // option to handle inbounds only from known peers (i.e. which were discovered by ourselves)
    boolean inboundOnlyFromKnownNodes = true;

    private boolean discoveryEnabled = SystemProperties.CONFIG.peerDiscovery();

    private Map<DiscoverListener, ListenerHandler> listeners = new IdentityHashMap<>();

    public NodeManager() {
        // TODO all below take from config ?
        key = ECKey.fromPrivate(BigInteger.TEN).decompress();
        byte[] nodeID = new byte[64];
        System.arraycopy(key.getPubKey(), 1, nodeID, 0, 64);
        int port = 30303;
        String address = "127.0.0.1";
        homeNode = new Node(nodeID, address, port);
        table = new NodeTable(homeNode);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.err.println("Statistics:\n" + dumpAllStatistics());
            }
        }, 1 * 1000, 10 * 1000);
    }

    public NodeManager(NodeTable table, ECKey key) {
        this.table = table;
        homeNode = table.getNode();
        this.key = key;
    }

    synchronized void init(List<Node> bootNodes) {
        for (Node node : bootNodes) {
            getNodeHandler(node);
        }

        for (Node node : SystemProperties.CONFIG.peerActive()) {
            getNodeHandler(node).getNodeStatistics().setPredefined(true);
        }

        // this task is done asynchronously with some fixed rate
        // to avoid any overhead in the NodeStatistics classes keeping them lightweight
        // (which might be critical since they might be invoked from time critical sections)
        Timer timer = new Timer("NodeManagerListenerTask");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processListeners();
            }
        }, 0, 1000);
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
            ret = new NodeHandler(n ,this);
            nodeHandlerMap.put(key, ret);
//            logger.debug("New node: " + ret);
            System.out.println("++++  New node: " + ret);
        }
        return ret;
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

        Node n = new Node(m.getNodeId(), sender.getHostName(), sender.getPort());

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
        if (discoveryEnabled) {
            logger.trace(" <===({}) {} [{}] {}", discoveryEvent.getAddress(),
                    discoveryEvent.getMessage().getClass().getSimpleName(), this, discoveryEvent.getMessage());
            messageSender.accept(discoveryEvent);
        }
    }

    public void stateChanged(NodeHandler nodeHandler, NodeHandler.State oldState, NodeHandler.State newState) {
        peerConnectionManager.nodeStatusChanged(nodeHandler);
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
        sb.append("0 reputation: " + zeroReputCount + " nodes.\n");
        return sb.toString();
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
