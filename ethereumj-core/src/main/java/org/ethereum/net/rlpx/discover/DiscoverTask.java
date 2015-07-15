package org.ethereum.net.rlpx.discover;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.FindNodeMessage;
import org.ethereum.net.rlpx.Message;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.KademliaOptions;
import org.ethereum.net.rlpx.discover.table.NodeEntry;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class DiscoverTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("discover");

    Channel channel;

    NodeTable table;

    ECKey key;

    byte[] nodeId;

    DiscoverTask(byte[] nodeId, Channel channel, ECKey key, NodeTable table) {
        this.nodeId = nodeId;
        this.channel = channel;
        this.key = key;
        this.table = table;
    }

    @Override
    public void run() {
        discover(nodeId, 0, new ArrayList<Node>());
    }

    public synchronized void discover(byte[] nodeId, int round, List<Node> prevTried) {

        try {
//        if (!channel.isOpen() || round == KademliaOptions.MAX_STEPS) {
//            logger.info("{}", String.format("Nodes discovered %d ", table.getAllNodes().size()));
//            return;
//        }

            if (round == KademliaOptions.MAX_STEPS) {
                logger.info("{}", String.format("(KademliaOptions.MAX_STEPS) Terminating discover after %d rounds.", round));
                logger.info("{}\n{}", String.format("Nodes discovered %d ", table.getNodesCount()), dumpNodes());
                return;
            }

            List<Node> closest = table.getClosestNodes(nodeId);
            List<Node> tried = new ArrayList<>();

            for (Node n : closest) {
                if (!tried.contains(n) && !prevTried.contains(n)) {
                    try {
                        Message findNode = FindNodeMessage.create(nodeId, key);
                        DatagramPacket packet = new DatagramPacket(
                                Unpooled.copiedBuffer(findNode.getPacket()),
                                new InetSocketAddress(n.getHost(), n.getPort()));
                        logger.info("<=== (to " + n.getHost() + ":" + n.getPort() + ") " + findNode);
                        channel.write(packet);
                        tried.add(n);
                    } catch (Exception ex) {
                        logger.info("{}", ex);
                    }
                }
                if (tried.size() == KademliaOptions.ALPHA) {
                    break;
                }
            }

            channel.flush();

            if (tried.isEmpty()) {
                logger.info("{}", String.format("(tried.isEmpty()) Terminating discover after %d rounds.", round));
                logger.info("{}\n{}", String.format("Nodes discovered %d ", table.getNodesCount()), dumpNodes());
                return;
            }

            tried.addAll(prevTried);

            discover(nodeId, round + 1, tried);
        } catch (Exception ex) {
            logger.info("{}", ex);
        }
    }

    private String dumpNodes() {
        String ret = "";
        for (NodeEntry entry : table.getAllNodes()) {
            ret += "    " + entry.getNode() + "\n";
        }
        return ret;
    }
}
