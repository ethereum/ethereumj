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

    public final static byte VERSION = 3;
    private MessageQueue msgQueue = null;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    WorldManager worldManager;

    @Autowired
    private Whisper whisper;

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
                worldManager.getListener().trace("[Recv: " + msg + "]");
                break;
            case MESSAGE:
                whisper.processEnvelope((ShhEnvelopeMessage) msg, this);
                break;
            case FILTER:
                whisper.setBloomFilter((ShhFilterMessage) msg, this);
                break;
            default:
                logger.error("Unknown SHH message type: " + msg.getCommand());
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Shh handling failed", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        active = false;
        whisper.removePeer(this);
        logger.debug("handlerRemoved: ... ");
    }

    public void activate() {
        logger.info("SHH protocol activated");
        worldManager.getListener().trace("SHH protocol activated");
//        whisper = new Whisper(msgQueue);
        whisper.addPeer(this);
        sendStatus();
        this.active = true;
    }

    private void sendStatus() {
        byte protocolVersion = ShhHandler.VERSION;
        ShhStatusMessage msg = new ShhStatusMessage(protocolVersion);
        sendMessage(msg);
    }

    void sendMessage(ShhMessage msg) {
        msgQueue.sendMessage(msg);
    }

    public Whisper getWhisper() {
        return whisper;
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}