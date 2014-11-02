package org.ethereum.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.ethereum.net.PeerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class establish a listener for incoming connections
 * @see <a href="http://netty.io">http://netty.io</a>
 *
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 01/11/2014 10:11
 */
public class PeerServer {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private PeerListener peerListener;

    private boolean peerDiscoveryMode = false;

    public PeerServer() {
    }

    public PeerServer(PeerListener peerListener) {
        this();
    	this.peerListener = peerListener;
    }

    public void start(int port) {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        if (peerListener != null)
        	peerListener.console("Listening on port " + port);


        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());

            b.handler(new LoggingHandler());
            b.childHandler(new EthereumChannelInitializer());

            // Start the client.
            logger.info("Listening for incoming connections, port: [{}] ", port);
            ChannelFuture f = b.bind(port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
        	logger.debug("Exception: {} ({})", e.getMessage(), e.getClass().getName());
            throw new Error("Disconnnected");
        } finally {
        	workerGroup.shutdownGracefully();

        }
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }



}
