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
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class UDPListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private final int port;
    private String address;
    private String[] bootPeers;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    WorldManager worldManager;

    public UDPListener() {
        this.address = SystemProperties.CONFIG.bindIp();
        port = SystemProperties.CONFIG.listenPort();
        if (SystemProperties.CONFIG.peerDiscovery()) {
            bootPeers = SystemProperties.CONFIG.peerDiscoveryIPList().toArray(new String[0]);
        }
    }

    public UDPListener(String address, int port) {
        this.address = address;
        this.port = port;
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

        logger.info("Discovery UDPListener started");
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        final List<Node> bootNodes = new ArrayList<>();

        // FIXME: setting nodes from ip.list and attaching node nodeId [f35cc8] constantly
        for (String boot: args) {
            bootNodes.add(new Node("enode://f35cc8a29929c7dc36bd46472d6cc68f104ede7fd42f1749c3533eb33a0fb6b45f2182c009271b83ca7f1d08ea5b1329056caf34c61e8f9e06314e39ec6f80b1" +
                    "@" + boot));
        }

        nodeManager.setBootNodes(bootNodes);


        try {
            while(true) {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .handler(new ChannelInitializer<NioDatagramChannel>() {
                            @Override
                            public void initChannel(NioDatagramChannel ch)
                                    throws Exception {
                                ch.pipeline().addLast(new PacketDecoder());
                                MessageHandler messageHandler = new MessageHandler(ch, nodeManager);
                                nodeManager.setMessageSender(messageHandler);
                                ch.pipeline().addLast(messageHandler);
                            }
                        });

                Channel channel = b.bind(address, port).sync().channel();

                DiscoveryExecutor discoveryExecutor = new DiscoveryExecutor(nodeManager);
                discoveryExecutor.discover();

                channel.closeFuture().sync();
                logger.warn("UDP channel closed. Recreating after 5 sec pause...");
                Thread.sleep(5000);
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
