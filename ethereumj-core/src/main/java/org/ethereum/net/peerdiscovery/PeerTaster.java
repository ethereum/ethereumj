package org.ethereum.net.peerdiscovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.net.client.EthereumFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 12:28
 */
public class PeerTaster {

    private Logger logger = LoggerFactory.getLogger("peerdiscovery");
    final EthereumPeerTasterHandler handler = new EthereumPeerTasterHandler();

    public PeerTaster() {
    }

    public void connect(String host, int port) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(15));
                    ch.pipeline().addLast(new EthereumFrameDecoder());
                    ch.pipeline().addLast(handler);
                }
            });

            // Start the client.
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerDiscoveryTimeout());
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } catch (InterruptedException ie) {
           logger.info("-- ClientPeer: catch (InterruptedException ie) --");
        } finally {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }

    public byte getCapabilities() {
        return handler.getCapabilities();
    }

}
