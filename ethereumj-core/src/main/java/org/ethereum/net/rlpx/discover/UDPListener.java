package org.ethereum.net.rlpx.discover;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.Node;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.ethereum.crypto.HashUtil.sha3;

@Component
public class UDPListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    private int port;
    private String address;
    private String[] bootPeers;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    SystemProperties config = SystemProperties.getDefault();

    private Channel channel;
    private volatile boolean shutdown = false;
    private DiscoveryExecutor discoveryExecutor;

    @Autowired
    public UDPListener(SystemProperties config, NodeManager nodeManager) {
        this.config = config;
        this.nodeManager = nodeManager;

        this.address = config.bindIp();
        port = config.listenPort();
        if (config.peerDiscovery()) {
            bootPeers = config.peerDiscoveryIPList().toArray(new String[0]);
        }
        if (config.peerDiscovery()) {
            if (port == 0) {
                logger.error("Discovery can't be started while listen port == 0");
            } else {
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
    }

    public UDPListener(String address, int port) {
        this.address = address;
        this.port = port;
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

        for (String boot: args) {
            // since discover IP list has no NodeIds we will generate random but persistent
            byte[] nodeId = ECKey.fromPrivate(sha3(boot.getBytes())).getNodeId();
            bootNodes.add(new Node("enode://" + Hex.toHexString(nodeId) + "@" + boot));
        }

        nodeManager.setBootNodes(bootNodes);


        try {
            discoveryExecutor = new DiscoveryExecutor(nodeManager);
            discoveryExecutor.start();

            while (!shutdown) {
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

                channel = b.bind(address, port).sync().channel();

                channel.closeFuture().sync();
                if (shutdown) {
                    logger.info("Shutdown discovery UDPListener");
                    break;
                }
                logger.warn("UDP channel closed. Recreating after 5 sec pause...");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            if (e instanceof BindException && e.getMessage().contains("Address already in use")) {
                logger.error("Port " + port + " is busy. Check if another instance is running with the same port.");
            } else {
                logger.error("Can't start discover: ", e);
            }
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public void close() {
        logger.info("Closing UDPListener...");
        shutdown = true;
        if (channel != null) {
            try {
                channel.close().await(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Problems closing UDPListener", e);
            }
        }

        if (discoveryExecutor != null) {
            try {
                discoveryExecutor.close();
            } catch (Exception e) {
                logger.warn("Problems closing DiscoveryExecutor", e);
            }
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
