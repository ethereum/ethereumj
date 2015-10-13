package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.server.Channel;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;
import org.ethereum.util.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ethereum.net.rlpx.FrameCodec.Frame;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageCodec extends MessageToMessageCodec<Frame, Message> {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private Channel channel;
    private MessageCodesResolver messageCodesResolver;

    private MessageFactory p2pMessageFactory;
    private MessageFactory ethMessageFactory;
    private MessageFactory shhMessageFactory;
    private MessageFactory bzzMessageFactory;
    private EthVersion ethVersion;

    @Autowired
    WorldManager worldManager;

    Map<Integer, Pair<? extends List<Frame>, AtomicInteger>> incompleteFrames = new LRUMap<>(1, 16);
    // LRU avoids OOM on invalid peers

    @Override
    protected void decode(ChannelHandlerContext ctx, Frame frame, List<Object> out) throws Exception {
        Frame completeFrame = null;
        if (frame.isChunked()) {
            Pair<? extends List<Frame>, AtomicInteger> frameParts = incompleteFrames.get(frame.contextId);
            if (frameParts == null) {
                if (frame.totalFrameSize < 0) {
//                    loggerNet.warn("No initial frame received for context-id: " + frame.contextId + ". Discarding this frame as invalid.");
                    // TODO: refactor this logic (Cpp sends non-chunked frames with context-id)
                    Message message = decodeMessage(ctx, Collections.singletonList(frame));
                    out.add(message);
                    return;
                } else {
                    frameParts = Pair.of(new ArrayList<Frame>(), new AtomicInteger(0));
                    incompleteFrames.put(frame.contextId, frameParts);
                }
            } else {
                if (frame.totalFrameSize >= 0) {
                    loggerNet.warn("Non-initial chunked frame shouldn't contain totalFrameSize field (context-id: " + frame.contextId + ", totalFrameSize: " + frame.totalFrameSize + "). Discarding this frame and all previous.");
                    incompleteFrames.remove(frame.contextId);
                    return;
                }
            }

            frameParts.getLeft().add(frame);
            int curSize = frameParts.getRight().addAndGet(frame.size);
            if (curSize > frameParts.getLeft().get(0).totalFrameSize) {
                loggerNet.warn("The total frame chunks size (" + curSize + ") is greater than expected (" + frameParts.getLeft().get(0).totalFrameSize + "). Discarding the frame.");
                incompleteFrames.remove(frame.contextId);
                return;
            }
            if (curSize == frameParts.getLeft().get(0).totalFrameSize) {
                Message message = decodeMessage(ctx, frameParts.getLeft());
                incompleteFrames.remove(frame.contextId);
                out.add(message);
            }
        } else {
            Message message = decodeMessage(ctx, Collections.singletonList(frame));
            out.add(message);
        }
    }

    private Message decodeMessage(ChannelHandlerContext ctx, List<Frame> frames) throws IOException {

        if (frames.size() > 1) throw new RuntimeException("Not implemented yet");

        Frame frame = frames.get(0);

        byte[] payload = ByteStreams.toByteArray(frame.getStream());

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Recv: Encoded: {} [{}]", frame.getType(), Hex.toHexString(payload));

        Message msg = createMessage((byte) frame.getType(), payload);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("From: \t{} \tRecv: \t{}", channel, msg);

        EthereumListener listener = worldManager.getListener();
        listener.onRecvMessage(msg);

        channel.getNodeStatistics().rlpxInMessages.add();
        return msg;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
        worldManager.getListener().trace(output);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", channel, msg);

        byte[] encoded = msg.getEncoded();

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Send: Encoded: {} [{}]", getCode(msg.getCommand()), Hex.toHexString(encoded));

        /*  HERE WE ACTUALLY USING THE SECRET ENCODING */
        byte code = getCode(msg.getCommand());
        Frame frame = new Frame(code, msg.getEncoded());
//        frameCodec.writeFrame(frame, out);
        out.add(frame);

        channel.getNodeStatistics().rlpxOutMessages.add();
    }

    /* TODO: this dirty hack is here cause we need to use message
       TODO: adaptive id on high message abstraction level,
       TODO: need a solution here*/
    private byte getCode(Enum msgCommand){
        byte code = 0;

        if (msgCommand instanceof P2pMessageCodes){
            code = messageCodesResolver.withP2pOffset(((P2pMessageCodes) msgCommand).asByte());
        }

        if (msgCommand instanceof EthMessageCodes){
            code = messageCodesResolver.withEthOffset(((EthMessageCodes) msgCommand).asByte());
        }

        if (msgCommand instanceof ShhMessageCodes){
            code = messageCodesResolver.withShhOffset(((ShhMessageCodes)msgCommand).asByte());
        }

        if (msgCommand instanceof BzzMessageCodes){
            code = messageCodesResolver.withBzzOffset(((BzzMessageCodes) msgCommand).asByte());
        }

        return code;
    }

    private Message createMessage(byte code, byte[] payload) {

        byte resolved = messageCodesResolver.resolveP2p(code);
        if (p2pMessageFactory != null && P2pMessageCodes.inRange(resolved)) {
            return p2pMessageFactory.create(resolved, payload);
        }

        resolved = messageCodesResolver.resolveEth(code);
        if (ethMessageFactory != null && EthMessageCodes.inRange(resolved, ethVersion)) {
            return ethMessageFactory.create(resolved, payload);
        }

        resolved = messageCodesResolver.resolveShh(code);
        if (shhMessageFactory != null && ShhMessageCodes.inRange(resolved)) {
            return shhMessageFactory.create(resolved, payload);
        }

        resolved = messageCodesResolver.resolveBzz(code);
        if (bzzMessageFactory != null && BzzMessageCodes.inRange(resolved)) {
            return bzzMessageFactory.create(resolved, payload);
        }

        throw new IllegalArgumentException("No such message: " + code + " [" + Hex.toHexString(payload) + "]");
    }

    public void setRemoteId(String remoteId, Channel channel){
        this.channel = channel;
    }

    public void setEthVersion(EthVersion ethVersion) {
        this.ethVersion = ethVersion;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (channel.isDiscoveryMode()) {
            loggerNet.debug("MessageCodec handling failed", cause);
        } else {
            loggerNet.error("MessageCodec handling failed", cause);
        }
        ctx.close();
    }

    public void initMessageCodes(List<Capability> caps) {
        this.messageCodesResolver = new MessageCodesResolver(caps);
    }

    public void setP2pMessageFactory(MessageFactory p2pMessageFactory) {
        this.p2pMessageFactory = p2pMessageFactory;
    }

    public void setEthMessageFactory(MessageFactory ethMessageFactory) {
        this.ethMessageFactory = ethMessageFactory;
    }

    public void setShhMessageFactory(MessageFactory shhMessageFactory) {
        this.shhMessageFactory = shhMessageFactory;
    }

    public void setBzzMessageFactory(MessageFactory bzzMessageFactory) {
        this.bzzMessageFactory = bzzMessageFactory;
    }
}