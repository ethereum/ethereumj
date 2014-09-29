package org.ethereum.net.peerdiscovery;

import static org.ethereum.net.message.StaticMessages.PING_MESSAGE;
import static org.ethereum.net.message.StaticMessages.PONG_MESSAGE;
import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;
import static org.ethereum.net.message.StaticMessages.GET_PEERS_MESSAGE;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.Command;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.client.Peer;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.message.StatusMessage;
import org.ethereum.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the basic messages between every peer on the network.
 * 
 * Peers can
 * <ul>
 * 	<li>HELLO		:	Announce themselves to the work</li>
 * 	<li>DISCONNECT: 	Disconnect themselves from the network</li>
 * 	<li>GET_PEERS	: 	Request a list of other knows peers</li>
 * 	<li>PEERS		:	Send a list of know peers</li>
 * 	<li>PING		: 	Check if another peer is still alive</li>
 * 	<li>PONG		:	Confirm that they themseves are still alive</li>
 * </ul>
 * 
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class PeerProtocolHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private long lastPongTime;;
    private boolean tearDown = false;
    private HelloMessage handshake = null;

    protected PeerListener peerListener;
    protected MessageQueue msgQueue = null;
    protected final Timer timer = new Timer("MiscMessageTimer");
    
    public PeerProtocolHandler() {}

    public PeerProtocolHandler(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

    	msgQueue = new MessageQueue(ctx);
        msgQueue.sendMessage(StaticMessages.HELLO_MESSAGE);
        sendPing();

        // sample for pinging in background
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (tearDown) this.cancel();
                sendPing();
            }
        }, 2000, 5000);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetPeers();
            }
        }, 2000, 60000);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        byte[] payload = (byte[]) msg;

        EthereumListener listener = WorldManager.getInstance().getListener();
        Command receivedCommand = Command.fromInt(RLP.getCommandCode(payload));       
        if (peerListener != null) peerListener.console("[Recv: " + receivedCommand.name() + "]");

        switch(receivedCommand) {
	        case HELLO:
	            HelloMessage helloMessage = new HelloMessage(payload);
	            if (peerListener != null) peerListener.console(helloMessage.toString());
	            msgQueue.receivedMessage(helloMessage);
	            handshake = helloMessage;            
	            if (listener != null)
	                listener.onRecvMessage(helloMessage);
	         	break;
	        case STATUS:
	        	StatusMessage statusMessage = new StatusMessage(payload);
	            if (peerListener != null) peerListener.console(statusMessage.toString());
	            if (listener != null)
	                listener.onRecvMessage(statusMessage);
	        	msgQueue.receivedMessage(statusMessage);
	        case DISCONNECT:
	            DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
	            msgQueue.receivedMessage(disconnectMessage);
	            if (peerListener != null) peerListener.console(disconnectMessage.toString());
	            if (listener != null)
	                listener.onRecvMessage(disconnectMessage);
	        	break;
	        case PING:
	        	msgQueue.receivedMessage(StaticMessages.PING_MESSAGE);
	        	sendPong();
	            if (listener != null)
	                listener.onRecvMessage(PING_MESSAGE);
	         	break;
	        case PONG:
	        	msgQueue.receivedMessage(StaticMessages.PONG_MESSAGE);
	            this.lastPongTime = System.currentTimeMillis();
	            if (listener != null)
	                listener.onRecvMessage(PONG_MESSAGE);
	            break;
	        case GET_PEERS:
	        	msgQueue.receivedMessage(StaticMessages.GET_PEERS_MESSAGE);
	        	sendPeers();
	            if (listener != null)
	                listener.onRecvMessage(GET_PEERS_MESSAGE);
    			break;        		
	        case PEERS:
	            PeersMessage peersMessage = new PeersMessage(payload);
	        	msgQueue.receivedMessage(peersMessage);
	            if (peerListener != null) peerListener.console(peersMessage.toString());
	            
	            WorldManager.getInstance().addPeers(peersMessage.getPeers());
//	            sendDisconnectNice(ctx);
	            if (listener != null)
	                listener.onRecvMessage(peersMessage);
	            break;
	        default:
	        	break;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // limit the size of recieving buffer to 1024
        ctx.channel().config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 32368);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.tearDown = true;
        logger.info("Lost connection to the server");
    	logger.error(cause.getMessage(), cause);

        ctx.close().sync();
        ctx.disconnect().sync();
    }
    
    public void sendMsg(Message msg) {
        msgQueue.sendMessage(msg);

        EthereumListener listener = WorldManager.getInstance().getListener();
        if (listener != null)
            listener.onSendMessage(msg);
    }
    
    protected void sendHello(ChannelHandlerContext ctx) {
        sendMsg(HELLO_MESSAGE);
    }
    
    protected void sendPing() {
        sendMsg(PING_MESSAGE);
    }

    protected void sendPong() {
        sendMsg(PONG_MESSAGE);
    }

    protected void sendGetPeers() {
        sendMsg(GET_PEERS_MESSAGE);
    }
    
    protected void sendPeers() {
    	Set<Peer> peers = WorldManager.getInstance().getPeers();
    	PeersMessage peersMessage = new PeersMessage(peers);
        sendMsg(peersMessage);
    }

    protected void sendDisconnectNice(ChannelHandlerContext ctx) throws InterruptedException {
        logger.info("[Send: DISCONNECT]");
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.DISCONNECT_PEER_QUITTING.length);
        buffer.writeBytes(StaticMessages.DISCONNECT_PEER_QUITTING);
        ctx.writeAndFlush(buffer);
        ctx.close().sync();
        ctx.disconnect().sync();
    }
    
    protected HelloMessage getHandshake() {
        return this.handshake;
    }
}