package org.ethereum.net.rlpx.discover;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


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
//        byte[] peerId = Hex.decode("621168019b7491921722649cd1aa9608f23f8857d782e7495fb6765b821002c4aac6ba5da28a5c91b432e5fcc078931f802ffb5a3ababa42adee7a0c927ff49e");
//        Node p = new Node(peerId, "127.0.0.1", 30303);
//        table.addNode(p);

        //Persist the list of known nodes with their reputation
        byte[] cppId = Hex.decode("487611428e6c99a11a9795a6abe7b529e81315ca6aad66e2a2fc76e3adf263faba0d35466c2f8f68d561dbefa8878d4df5f1f2ddb1fbeab7f42ffb8cd328bd4a");
        Node cppBootstrap = new Node(cppId, "5.1.83.226", 30303);
        table.addNode(cppBootstrap);

        byte[] cpp2Id = Hex.decode("1637a970d987ddb8fd18c5ca01931210cd2ac5d2fe0f42873c0b31f110c5cbedf68589ec608ec5421e1d259b06cba224127c6bbddbb7c26eaaea56423a23bd31");
        Node cpp2Bootstrap = new Node(cpp2Id, "69.140.163.94", 30320);
        table.addNode(cpp2Bootstrap);

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
            .option(ChannelOption.SO_TIMEOUT, 1000)
            .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new PacketDecoder());
                            ch.pipeline().addLast(new MessageHandler(key, table));
                        }
                    });

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
