package org.ethereum.net.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.facade.Blockchain;
import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
@Scope("prototype")
public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    Blockchain blockchain;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    WorldManager worldManager;


    String remoteId;
    Channel channel;

    private boolean peerDiscoveryMode = false;

    public EthereumChannelInitializer(String remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            logger.info("Open connection, channel: {}", ch.toString());

            channel = ctx.getBean(Channel.class);
            channel.init(remoteId, peerDiscoveryMode);

            channelManager.addChannel(channel);

            ch.pipeline().addLast("readTimeoutHandler",
                    new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
            //        ch.pipeline().addLast("in  encoder", channel.getMessageDecoder());
            //        ch.pipeline().addLast("out encoder", channel.getMessageEncoder());
            //        ch.pipeline().addLast(Capability.P2P, channel.getP2pHandler());
            //        ch.pipeline().addLast(Capability.ETH, channel.getEthHandler());
            //        ch.pipeline().addLast(Capability.SHH, channel.getShhHandler());
            ch.pipeline().addLast("initiator", channel.getMessageCodec().getInitiator());
            ch.pipeline().addLast("messageCodec", channel.getMessageCodec());

            // limit the size of receiving buffer to 1024
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(16_777_216));
            ch.config().setOption(ChannelOption.SO_RCVBUF, 16_777_216);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}
