package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.EthereumMessageSizeEstimator;
import org.ethereum.net.PeerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the initial connection using the Netty framework
 * @see <a href="http://netty.io">http://netty.io</a>
 */
public class PeerClient {

    private Logger logger = LoggerFactory.getLogger("wire");

    private PeerListener peerListener;
    private ProtocolHandler handler;

    public PeerClient() {
    }

    public PeerClient(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    public void connect(String host, int port) {

    	EventLoopGroup workerGroup = new NioEventLoopGroup();

        if (peerListener != null) {
        	peerListener.console("Connecting to: " + host + ":" + port);
            handler = new ProtocolHandler(peerListener);
        } else
            handler = new ProtocolHandler();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, new EthereumMessageSizeEstimator());
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());
            
            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {
					ch.pipeline().addLast("readTimeoutHandler",
							new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
                    ch.pipeline().addLast(new EthereumFrameDecoder());
                    ch.pipeline().addLast(handler);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            WorldManager.getInstance().setActivePeer(this);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
        	logger.debug("Exception: {} ({})", e.getMessage(), e.getClass().getName());
        } finally {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
            	logger.warn(e.getMessage(), e);
            }

            handler.killTimers();

            final Set<Peer> peers =  WorldManager.getInstance().getPeerDiscovery().getPeers();

			synchronized (peers) {
				for (Peer peer : peers) {
					if (host.equals(peer.getAddress().getHostAddress())
							&& port == peer.getPort())
						peer.setOnline(false);
				}
			}

            EthereumListener listener = WorldManager.getInstance().getListener();
            if (listener != null) listener.onPeerDisconnect(host, port);
        }
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

	public ProtocolHandler getHandler() {
		return handler;
	}
}
