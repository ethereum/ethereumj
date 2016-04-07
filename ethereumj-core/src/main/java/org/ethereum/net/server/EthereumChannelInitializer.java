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
            if (!peerDiscoveryMode) {
                logger.debug("Open {} connection, channel: {}", isInbound() ? "inbound" : "outbound", ch.toString());
            }

            if (isInbound() && channelManager.isRecentlyDisconnected(ch.remoteAddress().getAddress())) {
                // avoid too frequent connection attempts
                logger.debug("Drop connection - the same IP was disconnected recently, channel: {}", ch.toString());
                ch.disconnect();
                return;
            }

            final Channel channel = ctx.getBean(Channel.class);
            channel.init(ch.pipeline(), remoteId, peerDiscoveryMode);

            if(!peerDiscoveryMode) {
                channelManager.add(channel);
            }

            // limit the size of receiving buffer to 1024
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(256 * 1024));
            ch.config().setOption(ChannelOption.SO_RCVBUF, 256 * 1024);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

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

    private boolean isInbound() {
        return remoteId == null || remoteId.isEmpty();
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}
