package org.ethereum.net.client;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the connection to an remote address using the Netty framework
 *
 * @see <a href="http://netty.io">http://netty.io</a>
 */
@Component
@Scope("prototype")
public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger("net");

//    private boolean peerDiscoveryMode = false;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    WorldManager worldManager;

    @Autowired
    public ChannelManager channelManager;


    public void connect(String host, int port, String remoteId) {
        connect(host, port, remoteId, false);
    }

    public void connect(String host, int port, String remoteId, boolean discoveryMode) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        worldManager.getListener().trace("Connecting to: " + host + ":" + port);

        EthereumChannelInitializer ethereumChannelInitializer = ctx.getBean(EthereumChannelInitializer.class, remoteId);
        ethereumChannelInitializer.setPeerDiscoveryMode(discoveryMode);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());
            b.remoteAddress(host, port);

            b.handler(ethereumChannelInitializer);

            // Start the client.
            ChannelFuture f = b.connect().sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
            logger.error("Exception: {}", e.toString());
//            logger.trace("Exception details:", e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
