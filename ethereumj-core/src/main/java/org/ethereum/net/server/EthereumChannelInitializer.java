package org.ethereum.net.server;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    ChannelManager channelManager;

    private String remoteId;

    private boolean peerDiscoveryMode = false;

    public EthereumChannelInitializer(String remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            logger.info("Open connection, channel: {}", ch.toString());

            final Channel channel = ctx.getBean(Channel.class);
            channel.init(ch, remoteId, peerDiscoveryMode);

            if(!peerDiscoveryMode) {
                channelManager.add(channel);
            }

            // be aware of channel closing
            ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!peerDiscoveryMode) {
                        channelManager.notifyDisconnect(channel);
                    }
                }
            });

        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        }
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}
