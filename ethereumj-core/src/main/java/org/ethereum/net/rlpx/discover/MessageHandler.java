package org.ethereum.net.rlpx.discover;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.table.KademliaOptions;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;


public class MessageHandler extends SimpleChannelInboundHandler<DiscoveryEvent> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    ECKey key;
    NodeTable table;
    private Map<Node, Node> evictedCandidates = new HashMap<>();
    private Map<Node, Date> expectedPongs = new HashMap<>();
    public Channel channel;

    private Map<Node, NodeHandler> nodeHandlerMap = new WeakHashMap<>();

    public MessageHandler(ECKey key, NodeTable table, NioDatagramChannel ch, List<Node> bootNodes) {
        channel = ch;
        this.key = key;
        this.table = table;
        init(bootNodes);
    }

    private void init(List<Node> bootNodes) {
        for (Node node : bootNodes) {
            sendPing(null, new InetSocketAddress(node.getHost(), node.getPort()));
        }
    }

//    NodeHandler getNodeHandler(Node n) {
//        NodeHandler ret = nodeHandlerMap.get(n);
//        if (ret == null) {
//            ret = new NodeHandler(n ,this);
//            nodeHandlerMap.put(n, ret);
//            logger.info("New node: " + ret);
//        }
//        return ret;
//    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DiscoveryEvent event) throws Exception {
        Message m = event.getMessage();
        InetSocketAddress sender = event.getAddress();

        Node n = new Node(m.getNodeId(), sender.getHostName(), sender.getPort());
//        NodeHandler nodeHandler = getNodeHandler(n);

        byte type = m.getType()[0];
        switch (type) {
            case 1:
                handlePing(ctx, m, sender);
//                nodeHandler.handlePing((PingMessage) m, sender);
                break;
            case 2:
                handlePong(ctx, m, sender);
//                nodeHandler.handlePong((PongMessage) m, sender);
                break;
            case 3:
                handleFindNode(ctx, m, sender);
//                nodeHandler.handleFindNode((FindNodeMessage) m, sender);
                break;
            case 4:
                handleNeighbours(ctx, m, sender);
//                nodeHandler.handleNeighbours((NeighborsMessage) m, sender);
                break;
        }
    }

    private void handlePing(ChannelHandlerContext ctx, Message m, InetSocketAddress sender) {
        PingMessage ping = (PingMessage) m;
        logger.info("{}", String.format("  ===> PING from %s", sender.toString()));
        Node n = new Node(ping.getNodeId(), sender.getHostName(), sender.getPort());
        if (!table.getNode().equals(n)) {
            update(ctx, n);
            sendPong(ctx, ping.getMdc(), sender);
        }
    }

    private void handlePong(ChannelHandlerContext ctx, Message m, InetSocketAddress sender) {
        logger.info("{}", String.format("  ===> PONG from %s", sender.toString()));
        PongMessage pong = (PongMessage) m;
        Node n = new Node(pong.getNodeId(), sender.getHostName(), sender.getPort());
        update(ctx, n);
    }

    private void handleNeighbours(ChannelHandlerContext ctx, Message m, InetSocketAddress sender) {
        NeighborsMessage neighborsMessage = (NeighborsMessage) m;
        logger.info("{}", String.format("  ===> NEIGHBOURS from %s: %s", sender.toString(), m.toString()));
        update(ctx, new Node(neighborsMessage.getNodeId(), sender.getHostName(), sender.getPort()));
        for (Node n : neighborsMessage.getNodes()) {
            update(ctx, n);
        }
    }

    private void update(ChannelHandlerContext ctx, Node n) {

        if(table.getNode().equals(n)) {
           return;
        }

        if (!table.contains(n)) {
            if (expectedPongs.containsKey(n)) {
                if (System.currentTimeMillis() - expectedPongs.get(n).getTime()
                        < KademliaOptions.REQ_TIMEOUT) {
                    if (evictedCandidates.containsKey(n)) {
                        logger.info("{}", String.format("Evicted node remains %s:%d, remove expected node %s:%d", n, evictedCandidates.get(n)));
                        expectedPongs.remove(n);
                        evictedCandidates.remove(n);
                    } else {
                        addNode(ctx, n);
                    }
                } else {
                    if (evictedCandidates.containsKey(n)) {
                        logger.info("{}", String.format("Drop evicted %s:%d, add node %s:%d", n, evictedCandidates.get(n)));
                        dropNode(n);
                        addNode(ctx, evictedCandidates.get(n));
                    }
                    expectedPongs.remove(n);
                    evictedCandidates.remove(n);
                }
            } else {
                expectedPongs.put(n, new Date());
                sendPing(ctx, new InetSocketAddress(n.getHost(), n.getPort()));
            }
        } else {
            table.touchNode(n);
        }

        Set<Node> expiredExpected = new HashSet<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<Node, Date> e : expectedPongs.entrySet()) {
            if (now - e.getValue().getTime() > KademliaOptions.REQ_TIMEOUT) {
                if (evictedCandidates.containsKey(e.getKey())) {
                    Node evictionCandidate = e.getKey();
                    Node replacement = evictedCandidates.get(evictionCandidate);
                    logger.info("{}", String.format("Drop evicted %s:%d, add node %s:%d",
                            evictionCandidate.getHost(), evictionCandidate.getPort(),
                            replacement.getHost(), replacement.getPort()));
                    dropNode(evictionCandidate);
                    addNode(ctx, replacement);
                }
                expiredExpected.add(e.getKey());
            }
        }

        expectedPongs.keySet().removeAll(expiredExpected);
        evictedCandidates.keySet().removeAll(expiredExpected);
    }

    private void addNode(ChannelHandlerContext ctx, Node n) {
        Node evictedCandidate = table.addNode(n);
        if (evictedCandidate != null) {
            expectedPongs.put(evictedCandidate, new Date());
            evictedCandidates.put(evictedCandidate, n);
            expectedPongs.remove(n);
            sendPing(ctx, new InetSocketAddress(evictedCandidate.getHost(), evictedCandidate.getPort()));
        }
    }

    private void dropNode(Node n) {
        table.dropNode(n);
    }

    private void sendPong(ChannelHandlerContext ctx, byte[] mdc, InetSocketAddress address) {
        logger.debug("<=== [PONG] to " + address);
        Message pong = PongMessage.create(mdc, address.getHostName(), address.getPort(), key);
        sendPacket(ctx, pong.getPacket(), address);
    }

    private void sendPing(ChannelHandlerContext ctx, InetSocketAddress address) {
        logger.debug("<=== [PING] to " + address);
        Message ping = PingMessage.create(table.getNode().getHost(), table.getNode().getPort(), key);
//        logger.info("{}", String.format("PING to %s:%d", address.getHostName(), address.getPort()));
        sendPacket(ctx, ping.getPacket(), address);
    }

    private void handleFindNode(ChannelHandlerContext ctx, Message m, InetSocketAddress sender) {
        logger.info("{}", String.format("  ===> FIND from %s: %s", sender.toString(), m.toString()));
        FindNodeMessage msg = (FindNodeMessage) m;
        List<Node> closest = table.getClosestNodes(msg.getTarget());
        sendNeighbours(ctx, closest, sender);
    }

    private void sendNeighbours(ChannelHandlerContext ctx, List<Node> closest, InetSocketAddress address) {
        NeighborsMessage neighbors = NeighborsMessage.create(closest, key);
        logger.debug("<=== [NEIGHBORS]: to " + address + ": " + neighbors);
        sendPacket(ctx, neighbors.getPacket(), address);
    }

    private void sendPacket(ChannelHandlerContext ctx, byte[] wire, InetSocketAddress address) {
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(wire), address);
        channel.write(packet);
//        ctx.write(packet);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}
