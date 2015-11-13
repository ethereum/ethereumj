package org.ethereum.net.swarm.bzz;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.ProtocolHandler;
import org.ethereum.net.swarm.NetStore;
import org.ethereum.util.Functional;
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
public class BzzHandler extends ProtocolHandler<BzzMessage>
        implements Functional.Consumer<BzzMessage> {

    public final static byte VERSION = 0;
    private MessageQueue msgQueue = null;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    BzzProtocol bzzProtocol;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    NetStore netStore;

    public BzzHandler() {
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, BzzMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (BzzMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("BzzHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("BzzHandler invoke: [%s]", msg.getCommand()));

        onMessageReceived(msg);

        if (bzzProtocol != null) {
            bzzProtocol.accept(msg);
        }
    }

    @Override
    public void accept(BzzMessage bzzMessage) {
        msgQueue.sendMessage(bzzMessage);
        onMessageSent(bzzMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Bzz handling failed", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        active = false;
        logger.debug("handlerRemoved: ... ");
    }

    @Override
    public boolean hasCommand(Enum msgCommand) {

        return msgCommand instanceof BzzMessageCodes;
    }

    @Override
    public byte getCommandCode(Enum msgCommand) {

        return ((BzzMessageCodes) msgCommand).asByte();
    }

    @Override
    public byte getMaxCommandCode() {

        return (byte)BzzMessageCodes.max();
    }

    @Override
    public boolean hasCommandCode(byte code) {

        return BzzMessageCodes.inRange(code);
    }

    public void activate(String name) {
        logger.info("BZZ protocol activated");
        ethereumListener.trace("BZZ protocol activated");
        messageFactory = new BzzMessageFactory();
        createBzzProtocol();
        this.active = true;
    }

    private void createBzzProtocol() {
        bzzProtocol = new BzzProtocol(netStore /*NetStore.getInstance()*/);
        bzzProtocol.setMessageSender(this);
        bzzProtocol.start();
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}