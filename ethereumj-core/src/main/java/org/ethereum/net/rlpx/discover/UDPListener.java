package org.ethereum.net.rlpx.discover;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.Message;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.PingMessage;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;


public class UDPListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private final int port;
    private final String address;
    private final ECKey key;
    private NodeTable table;

    public UDPListener(String address, int port) {
        this.address = address;
        this.port = port;
        key = ECKey.fromPrivate(BigInteger.TEN).decompress();
    }

    public void start() throws Exception {

        NioEventLoopGroup group = new NioEventLoopGroup();
        byte[] nodeID = new byte[64];
        System.arraycopy(key.getPubKey(), 1, nodeID, 0, 64);
        Node homeNode = new Node(nodeID, address, port);
        table = new NodeTable(homeNode);

        //add default node on local to connect
//        byte[] peerId = Hex.decode("80bddb71035e60a63a8a711e0e9c1d40af8b82857c49f813f451d4dbf782e553c0fd798d26e0f9f570e42f80b7a6a931aa807920f8983c59e18957977dcdf29e");
//        Node p = new Node(peerId, "127.0.0.1", 30303);
//        table.addNode(p);

        //Persist the list of known nodes with their reputation
        byte[] cppId = Hex.decode("24f904a876975ab5c7acbedc8ec26e6f7559b527c073c6e822049fee4df78f2e9c74840587355a068f2cdb36942679f7a377a6d8c5713ccf40b1d4b99046bba0");
        Node cppBootstrap = new Node(cppId, "5.1.83.226", 30303);
        table.addNode(cppBootstrap);

        byte[] goId = Hex.decode("6cdd090303f394a1cac34ecc9f7cda18127eafa2a3a06de39f6d920b0e583e062a7362097c7c65ee490a758b442acd5c80c6fce4b148c6a391e946b45131365b");
        Node goBootstrap = new Node(goId, "54.169.166.226", 30303);
        table.addNode(goBootstrap);

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
            .option(ChannelOption.SO_TIMEOUT, 1000)
            .channel(NioDatagramChannel.class)
            .handler(new InputPacketHandler(key, table));

            Channel channel = b.bind(address, port).sync().channel();

            DiscoveryExecutor discoveryExecutor = new DiscoveryExecutor(channel, table, key);
            discoveryExecutor.discover();

            channel.closeFuture().sync();

        } catch (Exception e) {
            logger.error("{}", e);
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        String address = "0.0.0.0";
        int port = 30303;
        if (args.length >= 2) {
            address = args[0];
            port = Integer.parseInt(args[1]);
        }
        new UDPListener(address, port).start();
    }
}
