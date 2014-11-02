package org.ethereum.net.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.wire.MessageDecoder;
import org.ethereum.net.wire.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 01/11/2014 10:58
 */

public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");
    private PeerServer peerServer;

    public EthereumChannelInitializer(PeerServer peerServer) {
        this.peerServer = peerServer;
    }

    public void initChannel(NioSocketChannel ch) throws Exception {

        MessageQueue msgQueue;
        P2pHandler p2pHandler;
        EthHandler ethHandler;
        ShhHandler shhHandler;

        msgQueue = new MessageQueue(null);

        logger.info("Incoming connection from: {}", ch.toString());

        ch.remoteAddress();

        p2pHandler = new P2pHandler(msgQueue, null, false);
        p2pHandler.activate();

        ethHandler = new EthHandler(msgQueue, null, false);
        shhHandler = new ShhHandler(msgQueue, null);


        ch.pipeline().addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
        ch.pipeline().addLast("out encoder", new MessageEncoder());
        ch.pipeline().addLast("in  encoder", new MessageDecoder());
        ch.pipeline().addLast(Capability.P2P, p2pHandler);
        ch.pipeline().addLast(Capability.ETH, ethHandler);
        ch.pipeline().addLast(Capability.SHH, shhHandler);

        // limit the size of receiving buffer to 1024
        ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ch.config().setOption(ChannelOption.SO_RCVBUF, 32368);

        peerServer.addChannel(new Channel(msgQueue, p2pHandler, ethHandler, shhHandler));

        // todo: check if have or not active peer if not set this one
    }

}
