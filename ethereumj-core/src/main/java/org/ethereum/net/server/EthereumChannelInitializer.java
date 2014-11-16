package org.ethereum.net.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.facade.Blockchain;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.wire.MessageDecoder;
import org.ethereum.net.wire.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 01/11/2014 10:58
 */
@Component
public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    Blockchain blockchain;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    MessageDecoder messageDecoder;

    @Autowired
    MessageEncoder messageEncoder;

    @Autowired
    WorldManager worldManager;

    public EthereumChannelInitializer() {
    }

    public void initChannel(NioSocketChannel ch) throws Exception {

        logger.info("Open connection, channel: {}", ch.toString());

        Channel channel = ctx.getBean(Channel.class);
        channel.init();

        channelManager.addChannel(channel);
        channel.getP2pHandler().activate();

        ch.pipeline().addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
        ch.pipeline().addLast("out encoder", messageEncoder);
        ch.pipeline().addLast("in  encoder", messageDecoder);
        ch.pipeline().addLast(Capability.P2P, channel.getP2pHandler());
        ch.pipeline().addLast(Capability.ETH, channel.getEthHandler());
        ch.pipeline().addLast(Capability.SHH, channel.getShhHandler());

        // limit the size of receiving buffer to 1024
        ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ch.config().setOption(ChannelOption.SO_RCVBUF, 32368);
        ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

    }

}
