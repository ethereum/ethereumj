package org.ethereum.net.shh;

import org.ethereum.listener.EthereumListener;

import io.netty.channel.ChannelHandlerContext;

import org.ethereum.net.ProtocolHandler;
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
public class ShhHandler extends ProtocolHandler<ShhMessage> {
    private final static Logger logger = LoggerFactory.getLogger("net.shh");
    public final static byte VERSION = 3;

    private boolean active = false;
    private BloomFilter peerBloomFilter = BloomFilter.createAll();

    @Autowired
    private EthereumListener ethereumListener;

    @Autowired
    private WhisperImpl whisper;

    public ShhHandler() {
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("ShhHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("ShhHandler invoke: [%s]", msg.getCommand()));

        onMessageReceived(msg);

        switch (msg.getCommand()) {
            case STATUS:
                ethereumListener.trace("[Recv: " + msg + "]");
                break;
            case MESSAGE:
                whisper.processEnvelope((ShhEnvelopeMessage) msg, this);
                break;
            case FILTER:
                setBloomFilter((ShhFilterMessage) msg);
                break;
            default:
                logger.error("Unknown SHH message type: " + msg.getCommand());
                break;
        }
    }

    private void setBloomFilter(ShhFilterMessage msg) {
        peerBloomFilter = new BloomFilter(msg.getBloomFilter());
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

    @Override
    public boolean hasCommand(Enum msgCommand) {

        return msgCommand instanceof ShhMessageCodes;
    }

    @Override
    public byte getCommandCode(Enum msgCommand) {

        return ((ShhMessageCodes)msgCommand).asByte();
    }

    @Override
    public byte getMaxCommandCode() {

        return (byte)ShhMessageCodes.max();
    }

    @Override
    public boolean hasCommandCode(byte code) {

        return ShhMessageCodes.inRange(code);
    }

    @Override
    public void activate(String name) {
        super.activate(name);
        messageFactory = new ShhMessageFactory();
        logger.info("SHH protocol activated");
        ethereumListener.trace("SHH protocol activated");
        whisper.addPeer(this);
        sendStatus();
        sendHostBloom();
        this.active = true;
    }

    private void sendStatus() {
        byte protocolVersion = ShhHandler.VERSION;
        ShhStatusMessage msg = new ShhStatusMessage(protocolVersion);
        sendMessage(msg);
    }

    void sendHostBloom() {
        ShhFilterMessage msg = ShhFilterMessage.createFromFilter(whisper.hostBloomFilter.toBytes());
        sendMessage(msg);
    }

    void sendEnvelope(ShhEnvelopeMessage env) {
        sendMessage(env);
//        Topic[] topics = env.getTopics();
//        for (Topic topic : topics) {
//            if (peerBloomFilter.hasTopic(topic)) {
//                sendMessage(env);
//                break;
//            }
//        }
    }

    void sendMessage(ShhMessage msg) {
        messageQueue.sendMessage(msg);
        onMessageSent(msg);
    }

    public boolean isActive() {
        return active;
    }

}