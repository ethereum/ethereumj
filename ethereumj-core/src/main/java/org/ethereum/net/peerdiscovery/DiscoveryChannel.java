package org.ethereum.net.peerdiscovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.net.rlpx.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the connection to an remote address using the Netty framework
 *
 * @see <a href="http://netty.io">http://netty.io</a>
 */
@Component
@Scope("prototype")
public class DiscoveryChannel {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private boolean peerDiscoveryMode = false;

    @Autowired
    WorldManager worldManager;

    @Autowired
    MessageQueue messageQueue;

    @Autowired
    P2pHandler p2pHandler;

    @Autowired
    EthHandler ethHandler;

    @Autowired
    ShhHandler shhHandler;

    @Autowired
    BzzHandler bzzHandler;

    @Autowired
    ApplicationContext ctx;


    public DiscoveryChannel() {

    }

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


            p2pHandler.setMsgQueue(messageQueue);
            p2pHandler.setPeerDiscoveryMode(true);

            ethHandler.setMsgQueue(messageQueue);
            ethHandler.setPeerDiscoveryMode(true);

            shhHandler.setMsgQueue(messageQueue);

            bzzHandler.setMsgQueue(messageQueue);

            final MessageCodec decoder = ctx.getBean(MessageCodec.class);

            b.handler(

                    new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {

                            logger.info("Open connection, channel: {}", ch.toString());

                            ch.pipeline().addLast("readTimeoutHandler",
                                    new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
                            ch.pipeline().addLast("initiator", decoder.getInitiator());
                            ch.pipeline().addLast("messageCodec", decoder);
                            ch.pipeline().addLast(Capability.P2P, p2pHandler);
                            ch.pipeline().addLast(Capability.ETH, ethHandler);
                            ch.pipeline().addLast(Capability.SHH, shhHandler);
                            ch.pipeline().addLast(Capability.BZZ, bzzHandler);

                            // limit the size of receiving buffer to 1024
                            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
                            ch.config().setOption(ChannelOption.SO_RCVBUF, 32368);
                            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);
                        }
                    }
            );

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

            if (!peerDiscoveryMode) {
//                EthereumListener listener =  WorldManager.getInstance().getListener();
//                listener.onPeerDisconnect(host, port);
            }

        }
    }

    public HelloMessage getHelloHandshake() {
        return p2pHandler.getHandshakeHelloMessage();
    }

    public StatusMessage getStatusHandshake() {
        return ethHandler.getHandshakeStatusMessage();
    }
}
