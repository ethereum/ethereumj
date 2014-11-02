package org.ethereum.net.shh;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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

    public ShhHandler(){
    }

	public ShhHandler(MessageQueue msgQueue, PeerListener peerListener) {
        this.msgQueue = msgQueue;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("ShhHandler invoke: [{}]", msg.getCommand());

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
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }
}