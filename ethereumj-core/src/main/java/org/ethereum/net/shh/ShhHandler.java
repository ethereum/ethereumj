package org.ethereum.net.shh;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.net.PeerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the messages between peers with 'shh' capability on the network.
 * 
 * Peers with 'shh' capability can send/receive:
 * <ul>
 * </ul>
 */
public class ShhHandler extends SimpleChannelInboundHandler<ShhMessage> {

	public final static byte VERSION = 0x1;
	
	private final static Logger logger = LoggerFactory.getLogger("net");

	public ShhHandler() {
	}

	public ShhHandler(String peerId, PeerListener peerListener) {
		this();
	}

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("SHH protocol activated");
    }

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

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
    	logger.debug("handlerRemoved: ... ");

    }
}