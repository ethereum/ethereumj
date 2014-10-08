package org.ethereum.net.handler;

import static org.ethereum.net.message.StaticMessages.PING_MESSAGE;
import static org.ethereum.net.message.StaticMessages.PONG_MESSAGE;
import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;
import static org.ethereum.net.message.StaticMessages.GET_PEERS_MESSAGE;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.client.Peer;
import org.ethereum.net.client.PeerDiscovery;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.P2pMessage;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.message.ReasonCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the basic protocol messages between every peer on the network.
 * 
 * Peers can send/receive
 * <ul>
 * 	<li>HELLO		:	Announce themselves to the network</li>
 * 	<li>DISCONNECT	: 	Disconnect themselves from the network</li>
 * 	<li>GET_PEERS	: 	Request a list of other knows peers</li>
 * 	<li>PEERS		:	Send a list of known peers</li>
 * 	<li>PING		: 	Check if another peer is still alive</li>
 * 	<li>PONG		:	Confirm that they themselves are still alive</li>
 * </ul>
 */
@ChannelHandler.Sharable
public class P2pHandler extends SimpleChannelInboundHandler<P2pMessage> {

	private final static Logger logger = LoggerFactory.getLogger("wire");

	private final Timer timer = new Timer("MessageTimer");

	private PeerDiscovery peerDiscovery;
	private PeerListener peerListener;
	
	private MessageQueue msgQueue = null;
	private boolean tearDown = false;

	public P2pHandler() {
		this.peerDiscovery = WorldManager.getInstance().getPeerDiscovery();
	}

	public P2pHandler(PeerListener peerListener) {
		this();
		this.peerListener = peerListener;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		msgQueue = new MessageQueue(ctx, peerListener);
		// Send HELLO once when channel connection has been established
		msgQueue.sendMessage(HELLO_MESSAGE);
		startTimers();
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, P2pMessage msg) throws InterruptedException {
		logger.trace("Read channel for {}", ctx.channel().remoteAddress());
		
		switch (msg.getCommand()) {
			case HELLO:
				msgQueue.receivedMessage(msg);
				setHandshake((HelloMessage) msg, ctx);
				break;
			case DISCONNECT:
				msgQueue.receivedMessage(msg);
				break;
			case PING:
				msgQueue.receivedMessage(msg);
				msgQueue.sendMessage(PONG_MESSAGE);
				break;
			case PONG:
				msgQueue.receivedMessage(msg);
				break;
			case GET_PEERS:
				msgQueue.receivedMessage(msg);
				sendPeers();
				break;
			case PEERS:
				msgQueue.receivedMessage(msg);
				processPeers(ctx, (PeersMessage) msg);
				break;
			default:
				ctx.fireChannelRead(msg);
				break;
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.killTimers();
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getCause().toString());
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
        
    private void processPeers(ChannelHandlerContext ctx, PeersMessage peersMessage) {
    	peerDiscovery.addPeers(peersMessage.getPeers());
	}
    
    private void sendPeers() {
    	PeersMessage msg = new PeersMessage(peerDiscovery.getPeers());
    	msgQueue.sendMessage(msg);
    }
    
	private void setHandshake(HelloMessage msg, ChannelHandlerContext ctx) {
		if (msg.getP2PVersion() != 0)
			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_PROTOCOL));
		else {
			if (msg.getCapabilities().contains("eth")) {
				// Activate EthHandler for this peer
				ctx.pipeline().addLast(new EthHandler(msg.getPeerId(), peerListener));
				ctx.fireChannelActive();
			}

			InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
			int port = msg.getListenPort();
			Peer confirmedPeer = new Peer(address, port, msg.getPeerId());
			confirmedPeer.setOnline(true);
			confirmedPeer.getCapabilities().addAll(msg.getCapabilities());
			WorldManager.getInstance().getPeerDiscovery().getPeers().add(confirmedPeer);
		}
	}
    
    private void startTimers() {
        // sample for pinging in background
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (tearDown) cancel();
                msgQueue.sendMessage(PING_MESSAGE);
            }
        }, 2000, 5000);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	msgQueue.sendMessage(GET_PEERS_MESSAGE);
            }
        }, 2000, 25000);
    }
	
    public void killTimers(){
        timer.cancel();
        timer.purge();
    }
}