package org.ethereum.net.swarm.bzz;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.shh.ShhMessage;
import org.ethereum.net.shh.ShhMessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Process the messages between peers with 'bzz' capability on the network.
 */
@Component
@Scope("prototype")
public class BzzHandler extends SimpleChannelInboundHandler<BzzMessage> {

    public final static byte VERSION = 1;
    private MessageQueue msgQueue = null;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    WorldManager worldManager;

    public BzzHandler() {
    }

    public BzzHandler(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, BzzMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("BzzHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("BzzHandler invoke: [%s]", msg.getCommand()));

        switch (msg.getCommand()) {
            case STATUS:
                break;
            case STORE_REQUEST:
                break;
            case RETRIEVE_REQUEST:
                break;
            case PEERS:
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
        logger.info("BZZ protocol activated");
        worldManager.getListener().trace("BZZ protocol activated");
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}