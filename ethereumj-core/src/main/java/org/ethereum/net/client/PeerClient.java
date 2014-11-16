package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.eth.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the connection to an remote address using the Netty framework
 * @see <a href="http://netty.io">http://netty.io</a>
 */
@Component
@Scope("prototype")
public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private boolean peerDiscoveryMode = false;

    @Autowired
    WorldManager worldManager;

    @Autowired
    public ChannelManager channelManager;

    @Autowired
    public EthereumChannelInitializer ethereumChannelInitializer;

    public void connect(String host, int port) {

    	EventLoopGroup workerGroup = new NioEventLoopGroup();
        worldManager.getListener().trace("Connecting to: " + host + ":" + port);

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
        	logger.debug("Exception: {} ({})", e.getMessage(), e.getClass().getName());
            throw new Error("Disconnnected");
        } finally {
        	workerGroup.shutdownGracefully();
        }
    }

}
