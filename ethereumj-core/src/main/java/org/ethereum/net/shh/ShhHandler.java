package org.ethereum.net.shh;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Process the messages between peers with 'shh' capability on the network.
 *
 * Peers with 'shh' capability can send/receive:
 */
@Component
@Scope("prototype")
public class ShhHandler extends SimpleChannelInboundHandler<ShhMessage> {

    public final static byte VERSION = 1;
    private MessageQueue msgQueue = null;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    WorldManager worldManager;

    public ShhHandler() {
    }

    public ShhHandler(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("ShhHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("ShhHandler invoke: [%s]", msg.getCommand()));

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

    public void activate() {
        logger.info("SHH protocol activated");
        worldManager.getListener().trace("SHH protocol activated");
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}