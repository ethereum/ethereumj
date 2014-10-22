package org.ethereum.net.p2p;

import static org.ethereum.net.message.StaticMessages.PING_MESSAGE;
import static org.ethereum.net.message.StaticMessages.PONG_MESSAGE;
import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.eth.TransactionsMessage;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.message.*;
import org.ethereum.net.peerdiscovery.PeerData;
import org.ethereum.net.shh.ShhMessageCodes;
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
public class P2pHandler extends SimpleChannelInboundHandler<P2pMessage> {

	public final static byte VERSION = 0x2;
	
	private final static Logger logger = LoggerFactory.getLogger("net");

	private final Timer timer = new Timer("MessageTimer");

	private PeerListener peerListener;
	
	private MessageQueue msgQueue;
	private boolean tearDown = false;

    private boolean active = false;

    private boolean peerDiscoveryMode = false;

	public P2pHandler() {
	}

    public P2pHandler(boolean peerDiscoveryMode) {
        super();
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

	public P2pHandler(PeerListener peerListener, MessageQueue msgQueue) {
		this();
        this.msgQueue = msgQueue;
		this.peerListener = peerListener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
        msgQueue.activate(ctx);
		// Send HELLO once when channel connection has been established
		msgQueue.sendMessage(HELLO_MESSAGE);
		startTimers();
	}

    public void activate(){
        logger.info("P2P protocol activated");
        active = true;
    }

    public boolean isActive(){
        return active;
    }

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, P2pMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (P2pMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("P2PHandler invoke: [{}]", msg.getCommand());

		switch (msg.getCommand()) {
			case HELLO:
				msgQueue.receivedMessage(msg);
                if (!peerDiscoveryMode)
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
                //sendPeers(); // todo: implement session management for peer request
				break;
			case PEERS:
				msgQueue.receivedMessage(msg);
				processPeers(ctx, (PeersMessage) msg);

                if (peerDiscoveryMode){
                    msgQueue.sendMessage(new DisconnectMessage(ReasonCode.REQUESTED));
                    killTimers();
                    ctx.close().sync();
                    ctx.disconnect().sync();
                }
                break;
            case USER:

                processUser((UserMessage) msg);
                break;
			default:
				ctx.fireChannelRead(msg);
				break;
		}
	}


    public void processUser(UserMessage msg){
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
        WorldManager.getInstance().getPeerDiscovery().addPeers(peersMessage.getPeers());
	}

    private void sendPeers() {
        Set<PeerData> peers = WorldManager.getInstance().getPeerDiscovery().getPeers();
    	PeersMessage msg = new PeersMessage(peers);
    	msgQueue.sendMessage(msg);
    }


	private void setHandshake(HelloMessage msg, ChannelHandlerContext ctx) {
		if (msg.getP2PVersion() != P2pHandler.VERSION)
			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_PROTOCOL));
		else {
			List<Capability> capInCommon = new ArrayList<>();
			for (Capability capability : msg.getCapabilities()) {
				if (HELLO_MESSAGE.getCapabilities().contains(capability)) {
	    			if (capability.getName().equals(Capability.ETH)){

                        // Activate EthHandler for this peer
                        EthHandler ethHandler =
                                (EthHandler)ctx.pipeline().get(Capability.ETH);

                        ethHandler.setPeerId(msg.getPeerId());
                        ethHandler.activate();
                    }
                    else if (capability.getName().equals(Capability.SHH)){

                        // Activate ShhHandler for this peer
                        ShhHandler shhHandler =
                                (ShhHandler)ctx.pipeline().get(Capability.SHH);
                        shhHandler.activate();
                    }


					capInCommon.add(capability);
				}
			}
            adaptMessageIds(capInCommon);

			InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
			int port = msg.getListenPort();
			PeerData confirmedPeer = new PeerData(address, port, msg.getPeerId());
			confirmedPeer.setOnline(true);
			confirmedPeer.getCapabilities().addAll(msg.getCapabilities());

            //todo calculate the Offsets
			WorldManager.getInstance().getPeerDiscovery().getPeers().add(confirmedPeer);
		}
	}

    /**
     * submit transaction to the network
     * @param tx - fresh transaction object
     */
    public void sendTransaction(Transaction tx ){

        TransactionsMessage msg = new TransactionsMessage(tx);
        msgQueue.sendMessage(msg);
    }

    public void adaptMessageIds(List<Capability> capabilities) {

    	Collections.sort(capabilities);
        byte offset = (byte) (P2pMessageCodes.USER.asByte() + 1);

        for (Capability capability : capabilities) {

            if (capability.getName().equals(Capability.ETH)) {
                EthMessageCodes.setOffset(offset);
                offset += EthMessageCodes.values().length;
            } 

            if (capability.getName().equals(Capability.SHH)) {
                ShhMessageCodes.setOffset(offset);
                offset += ShhMessageCodes.values().length;
            }
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

/*
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	msgQueue.sendMessage(GET_PEERS_MESSAGE);
            }
        }, 500, 25000);
*/
    }
	
    public void killTimers(){
        timer.cancel();
        timer.purge();
        msgQueue.close();

    }
}