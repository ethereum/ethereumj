package org.ethereum.net.server;

import org.ethereum.config.SystemProperties;
import org.ethereum.manager.WorldManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class establishes a listener for incoming connections.
 * See <a href="http://netty.io">http://netty.io</a>.
 *
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
public class PeerServer {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    public ChannelManager channelManager;

    public EthereumChannelInitializer ethereumChannelInitializer;

    @Autowired
    WorldManager worldManger;

    public PeerServer() {
    }


    public void start(int port) {


        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ethereumChannelInitializer = ctx.getBean(EthereumChannelInitializer.class, "");

        worldManger.getListener().trace("Listening on port " + port);


        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());

            b.handler(new LoggingHandler());
            b.childHandler(ethereumChannelInitializer);

            // Start the client.
            logger.info("Listening for incoming connections, port: [{}] ", port);
            logger.info("NodeId: [{}] ", Hex.toHexString(SystemProperties.CONFIG.nodeId()));

            ChannelFuture f = b.bind(port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
            logger.debug("Exception: {} ({})", e.getMessage(), e.getClass().getName());
            throw new Error("Server Disconnected");
        } finally {
            workerGroup.shutdownGracefully();

        }
    }

}
