package org.ethereum.net.peerdiscovery;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Tiberius Iliescu
 */
@Component
@Scope("prototype")
public class DiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    EthereumListener ethereumListener;

    public DiscoveryChannelInitializer discoveryChannelInitializer;

    public DiscoveryClient() {}

    public void connect(String host, int port) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ethereumListener.trace("Connecting to: " + host + ":" + port);

        discoveryChannelInitializer = ctx.getBean(DiscoveryChannelInitializer.class, "");
        discoveryChannelInitializer.setPeerDiscoveryMode(true);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());
            b.remoteAddress(host, port);



            b.handler(discoveryChannelInitializer);

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

    public HelloMessage getHelloHandshake() {
        return discoveryChannelInitializer.getHelloHandshake();
    }

    public StatusMessage getStatusHandshake() {
        return discoveryChannelInitializer.getStatusHandshake();
    }
}
