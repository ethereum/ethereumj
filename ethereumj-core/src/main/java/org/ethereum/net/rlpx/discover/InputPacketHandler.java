package org.ethereum.net.rlpx.discover;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import javafx.scene.control.Tab;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.*;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.*;

import static org.ethereum.crypto.HashUtil.sha3;


public class InputPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private ECKey key;
    private NodeTable table;
    private Map<Node, Node> evictedCandidates = new HashMap<>();
    private Map<Node, Date> expectedPongs = new HashMap<>();

    InputPacketHandler(ECKey key, NodeTable table) {
        this.key = key;
        this.table = table;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf buf = packet.content();
        byte[] encoded = new byte[buf.readableBytes()];
        buf.readBytes(encoded);
        Message msg = Message.decode(encoded);

        byte msgType = msg.getType()[0];
        switch (msgType) {
            case 1:
                handlePing(ctx, msg, packet.sender());
                break;
            case 2:
                handlePong(ctx, msg, packet.sender());
                break;
            case 3:
                handleFindNode(ctx, msg, packet.sender());
                break;
            case 4:
                handleNeighbours(ctx, msg, packet.sender());
                break;
        }
    }

    private void handlePing(ChannelHandlerContext ctx, Message inputMsg, InetSocketAddress sender) {
        PingMessage ping = (PingMessage) inputMsg;
        logger.info("{}", String.format("PING from %s", sender.toString()));
        Message pong = PongMessage.create(ping.getMdc(), ping.getHost(), ping.getPort(), key);
        DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(pong.getPacket()), sender);
        ctx.write(packet);
    }

    private void handlePong(ChannelHandlerContext ctx, Message inputMsg, InetSocketAddress sender) {
        logger.info("{}", String.format("PONG from %s", sender.toString()));
        PongMessage pong = (PongMessage) inputMsg;
        Node n = new Node(pong.getData());
        if (expectedPongs.containsKey(n) && evictedCandidates.containsKey(n)) {
            if (System.currentTimeMillis() - expectedPongs.get(n).getTime()
                    < KademliaOptions.REQ_TIMEOUT) {
                logger.info("{}","Eviction1");
                table.dropNode(evictedCandidates.get(n));
                table.addNode(n);
                expectedPongs.remove(n);
                evictedCandidates.remove(n);
            } else {
                logger.info("{}","Eviction2");
                expectedPongs.remove(n);
                evictedCandidates.remove(n);
            }
        } else {
            table.addNode(n);
        }
    }

    private void handleNeighbours(ChannelHandlerContext ctx, Message inputMsg, InetSocketAddress sender) {
        NeighborsMessage neighborsMessage = (NeighborsMessage) inputMsg;
        logger.info("{}", String.format("NEIGHBOURS from %s", sender.toString()));
        for (Node n : neighborsMessage.getNodes()) {
            Node contested = table.addNode(n);
            if (contested != null) {
                expectedPongs.put(contested, new Date());
                evictedCandidates.put(contested, n);
                Message ping = PingMessage.create(contested.getHost(), contested.getPort(), key);
                DatagramPacket packet = new DatagramPacket(
                        Unpooled.copiedBuffer(ping.getPacket()), sender);
                ctx.write(packet);
            } else {
                Message ping = PingMessage.create(n.getHost(), n.getPort(), key);
                DatagramPacket packet = new DatagramPacket(
                        Unpooled.copiedBuffer(ping.getPacket()), sender);
                ctx.write(packet);
            }
        }
    }

    private void handleFindNode(ChannelHandlerContext ctx, Message inputMsg, InetSocketAddress sender) {
        FindNodeMessage msg = (FindNodeMessage) inputMsg;
        logger.info("{}", String.format("FIND from %s", sender.toString()));
        List<Node> closest = table.getClosestNodes(msg.getTarget());
        NeighborsMessage neighbors = NeighborsMessage.create(closest, key);
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(neighbors.getPacket()), sender);
        ctx.write(packet);
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }
}
