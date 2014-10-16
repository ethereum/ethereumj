package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.wire.MessageDecoder;
import org.ethereum.net.wire.MessageEncoder;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.peerdiscovery.PeerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the connection to an remote address using the Netty framework
 * @see <a href="http://netty.io">http://netty.io</a>
 */
public class PeerClient {

    private Logger logger = LoggerFactory.getLogger("wire");

    private PeerListener peerListener;
    private P2pHandler p2pHandler;

    private MessageQueue queue;

    private boolean peerDiscoveryMode = false;

    public PeerClient() {
    }

    public PeerClient(boolean peerDiscoveryMode){
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public PeerClient(PeerListener peerListener) {
    	this.peerListener = peerListener;
    }

    public void connect(String host, int port) {

    	EventLoopGroup workerGroup = new NioEventLoopGroup();

        if (peerListener != null)
        	peerListener.console("Connecting to: " + host + ":" + port);

        if (peerDiscoveryMode)
            p2pHandler = new P2pHandler(peerDiscoveryMode);
        else
            p2pHandler = new P2pHandler(peerListener);
        
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());
            b.remoteAddress(host, port);
            
            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {
					ch.pipeline().addLast("readTimeoutHandler",
							new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
					ch.pipeline().addLast("out encoder", new MessageEncoder());
					ch.pipeline().addLast("in  encoder", new MessageDecoder());
					ch.pipeline().addLast("p2p", p2pHandler);

                    // limit the size of receiving buffer to 1024
                    ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
                    ch.config().setOption(ChannelOption.SO_RCVBUF, 32368);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect().sync();
            WorldManager.getInstance().setActivePeer(this);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
            logger.debug("Connection is closed");

        } catch (Exception e) {
        	logger.debug("Exception: {} ({})", e.getMessage(), e.getClass().getName());
        } finally {
        	workerGroup.shutdownGracefully();

        	p2pHandler.killTimers();

            final Set<PeerData> peers =  WorldManager.getInstance().getPeerDiscovery().getPeers();

			synchronized (peers) {
				for (PeerData peer : peers) {
					if (host.equals(peer.getAddress().getHostAddress())
							&& port == peer.getPort())
						peer.setOnline(false);
				}
			}
        }
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

	public P2pHandler getP2pHandler() {
		return p2pHandler;
	}
}
