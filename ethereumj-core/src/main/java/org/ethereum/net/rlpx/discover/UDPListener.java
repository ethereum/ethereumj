package org.ethereum.net.rlpx.discover;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class UDPListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private final int port;
    private final String address;
    private final ECKey key;
    private NodeTable table;
    private String[] bootPeers;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    WorldManager worldManager;

    public UDPListener() {
        System.out.println("====== UDPListener() ======");
        address = SystemProperties.CONFIG.getConfig().getString("peer.bind.ip");
        port = SystemProperties.CONFIG.listenPort();
        key = ECKey.fromPrivate(BigInteger.TEN).decompress();
        if (SystemProperties.CONFIG.peerDiscovery()) {
            bootPeers = SystemProperties.CONFIG.peerDiscoveryIPList().toArray(new String[0]);
        }
    }

    public UDPListener(String address, int port) {
        this.address = address;
        this.port = port;
        key = ECKey.fromPrivate(BigInteger.TEN).decompress();
    }

    @PostConstruct
    void init() {
        if (SystemProperties.CONFIG.peerDiscovery()) {
            new Thread("UDPListener") {
                @Override
                public void run() {
                    try {
                        UDPListener.this.start(bootPeers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }

    public static Node parseNode(String s) {
        int idx1 = s.indexOf('@');
        int idx2 = s.indexOf(':');
        String id = s.substring(0, idx1);
        String host = s.substring(idx1 + 1, idx2);
        int port = Integer.parseInt(s.substring(idx2+1));
        return new Node(Hex.decode(id), host, port);
    }

    public void start(String[] args) throws Exception {
        System.out.println("====== UDPListener:start ======");

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        byte[] nodeID = new byte[64];
        System.arraycopy(key.getPubKey(), 1, nodeID, 0, 64);
        Node homeNode = new Node(nodeID, address, port);
        table = new NodeTable(homeNode);

        final List<Node> bootNodes = new ArrayList<>();

        for (String boot: args) {
            bootNodes.add(new Node("enode://f35cc8a29929c7dc36bd46472d6cc68f104ede7fd42f1749c3533eb33a0fb6b45f2182c009271b83ca7f1d08ea5b1329056caf34c61e8f9e06314e39ec6f80b1" +
                    "@" + boot));
        }

        //add default node on local to connect

//        {
//            byte[] peerId = Hex.decode("f35cc8a29929c7dc36bd46472d6cc68f104ede7fd42f1749c3533eb33a0fb6b45f2182c009271b83ca7f1d08ea5b1329056caf34c61e8f9e06314e39ec6f80b1");
//            bootNodes.add(new Node(peerId, "127.0.0.1", 30300));
//        }

//          bootNodes.add(new Node("enode://8021cdf53c0ed64a3fdfe9fc922515b79d77c80e89794b19ae02ccfdf912d058fbaa4fd3a0e7b0746d9a68dd3b6157406c0c7c085aa29b9db0e833e28f8d5179" +
//                "@127.0.0.1:30300"));
//          bootNodes.add(new Node("enode://8021cdf53c0ed64a3fdfe9fc922515b79d77c80e89794b19ae02ccfdf912d058fbaa4fd3a0e7b0746d9a68dd3b6157406c0c7c085aa29b9db0e833e28f8d5179" +
//                "@109.120.176.38:30303"));
//        bootNodes.add(new Node("enode://ebe122fcc18aa2f46b0a881780ebed667f55b2d40957f7b82234f1f8492acd2650e6c89a30435d5a84b1fd803102aed7e9e953e6c5aa77e2ab9f3f01db789e51" +
//                "@134.213.132.179:30304"));

//		discover.MustParseNode("enode://a979fb575495b8d6db44f750317d0f4622bf4c2aa3365d6af7c284339968eef29b69ad0dce72a4d8db5ebb4968de0e3bec910127f134779fbcb0cb6d3331163c@52.16.188.185:30303"),
//		discover.MustParseNode("enode://de471bccee3d042261d52e9bff31458daecc406142b401d4cd848f677479f73104b9fdeb090af9583d3391b7f10cb2ba9e26865dd5fca4fcdc0fb1e3b723c786@54.94.239.50:30303"),
        // ETH/DEV cpp-ethereum (poc-9.ethdev.com)
//		discover.MustParseNode("enode://487611428e6c99a11a9795a6abe7b529e81315ca6aad66e2a2fc76e3adf263faba0d35466c2f8f68d561dbefa8878d4df5f1f2ddb1fbeab7f42ffb8cd328bd4a@5.1.83.226:30303"),

//        bootNodes.add(new Node("enode://a979fb575495b8d6db44f750317d0f4622bf4c2aa3365d6af7c284339968eef29b69ad0dce72a4d8db5ebb4968de0e3bec910127f134779fbcb0cb6d3331163c@" +
//                "52.16.188.185:30303"));
//        bootNodes.add(new Node("enode://de471bccee3d042261d52e9bff31458daecc406142b401d4cd848f677479f73104b9fdeb090af9583d3391b7f10cb2ba9e26865dd5fca4fcdc0fb1e3b723c786@" +
//                "54.94.239.50:30303"));
//        bootNodes.add(new Node("enode://487611428e6c99a11a9795a6abe7b529e81315ca6aad66e2a2fc76e3adf263faba0d35466c2f8f68d561dbefa8878d4df5f1f2ddb1fbeab7f42ffb8cd328bd4a@" +
//                "5.1.83.226:30303"));

        //Persist the list of known nodes with their reputation
//        byte[] cppId = Hex.decode("487611428e6c99a11a9795a6abe7b529e81315ca6aad66e2a2fc76e3adf263faba0d35466c2f8f68d561dbefa8878d4df5f1f2ddb1fbeab7f42ffb8cd328bd4a");
//        Node cppBootstrap = new Node(cppId, "5.1.83.226", 30303);
//        table.addNode(cppBootstrap);
//
//        byte[] cpp2Id = Hex.decode("1637a970d987ddb8fd18c5ca01931210cd2ac5d2fe0f42873c0b31f110c5cbedf68589ec608ec5421e1d259b06cba224127c6bbddbb7c26eaaea56423a23bd31");
//        Node cpp2Bootstrap = new Node(cpp2Id, "69.140.163.94", 30320);
//        table.addNode(cpp2Bootstrap);

//        final NodeManager nodeManager = new NodeManager(table, key);

        try {
            while(true) {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        //            .option(ChannelOption.SO_TIMEOUT, 0)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .channel(NioDatagramChannel.class)
                        .handler(new ChannelInitializer<NioDatagramChannel>() {
                            @Override
                            public void initChannel(NioDatagramChannel ch)
                                    throws Exception {
                                ch.pipeline().addLast(new PacketDecoder());
                                MessageHandler messageHandler = new MessageHandler(ch, nodeManager);
                                nodeManager.setMessageSender(messageHandler);
                                ch.pipeline().addLast(messageHandler);
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(1000);
                                            nodeManager.init(bootNodes);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });

                Channel channel = b.bind(address, port).sync().channel();

                DiscoveryExecutor discoveryExecutor = new DiscoveryExecutor(nodeManager);
                discoveryExecutor.discover();

                channel.closeFuture().sync();
                logger.warn("UDP channel closed. Recreating...");
            }
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
        new UDPListener(address, port).start(Arrays.copyOfRange(args, 2, args.length));
    }
}
