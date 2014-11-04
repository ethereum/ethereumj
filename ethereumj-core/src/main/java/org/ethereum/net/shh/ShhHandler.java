package org.ethereum.net.shh;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the messages between peers with 'shh' capability on the network.
 * 
 * Peers with 'shh' capability can send/receive:
 *
 *
 */
public class ShhHandler extends SimpleChannelInboundHandler<ShhMessage> {

	public final static byte VERSION = 0x1;
    private MessageQueue msgQueue = null;

    private boolean active = false;

	private final static Logger logger = LoggerFactory.getLogger("net");
    private PeerListener peerListener;

    public ShhHandler(){
    }

	public ShhHandler(MessageQueue msgQueue, PeerListener peerListener) {
        this.msgQueue = msgQueue;
        this.peerListener = peerListener;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("ShhHandler invoke: [{}]", msg.getCommand());

        if (peerListener != null)
            peerListener.console(String.format( "ShhHandler invoke: [%s]", msg.getCommand()));

		switch (msg.getCommand()) {
            case STATUS:
                break;
            case MESSAGE:
                break;
            case ADD_FILTER:
                break;
            case REMOVE_FILTER:
                break;
            case PACKET_COUNT:
                break;
            default:
				break;
		}
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getCause().toString());
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        active = false;
    	logger.debug("handlerRemoved: ... ");
    }

    public void activate(){
        logger.info("SHH protocol activated");
        if (peerListener != null)
            peerListener.console("SHH protocol activated");
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }
}