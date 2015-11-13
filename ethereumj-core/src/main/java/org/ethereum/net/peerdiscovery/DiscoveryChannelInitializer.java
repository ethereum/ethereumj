package org.ethereum.net.peerdiscovery;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Tiberius Iliescu
 */
public class DiscoveryChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private boolean peerDiscoveryMode = true;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    ApplicationContext ctx;

    DiscoveryChannel discoveryChannel;

    public DiscoveryChannelInitializer() {

    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {

        logger.info("Open connection, channel: {}", ch.toString());

        discoveryChannel = ctx.getBean(DiscoveryChannel.class);
        discoveryChannel.init(ch, "", peerDiscoveryMode);

        // limit the size of receiving buffer to 1024
        ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ch.config().setOption(ChannelOption.SO_RCVBUF, 32368);
        ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public HelloMessage getHelloHandshake() {
        return discoveryChannel.getHelloHandshake();
    }

    public StatusMessage getStatusHandshake() {
        return discoveryChannel.getStatusHandshake();
    }
}
