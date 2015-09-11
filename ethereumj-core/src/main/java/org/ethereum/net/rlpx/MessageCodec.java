package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageCodec;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.P2pMessageFactory;
import org.ethereum.net.server.Channel;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.rlpx.FrameCodec.Frame;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageCodec extends ByteToMessageCodec<Message> {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private FrameCodec frameCodec;
    private ECKey myKey = CONFIG.getMyKey();
    private byte[] nodeId;
    private byte[] remoteId;
    private EncryptionHandshake handshake;
    private byte[] initiatePacket;
    private Channel channel;
    private MessageCodesResolver messageCodesResolver;
    private boolean isHandshakeDone;
    private final InitiateHandler initiator = new InitiateHandler();

    private MessageFactory p2pMessageFactory;
    private MessageFactory ethMessageFactory;
    private MessageFactory shhMessageFactory;
    private MessageFactory bzzMessageFactory;

    public InitiateHandler getInitiator() {
        return initiator;
    }

    public class InitiateHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            channel.setInetSocketAddress((InetSocketAddress) ctx.channel().remoteAddress());
            if (remoteId.length == 64) {
                channel.setNode(remoteId);
                initiate(ctx);
            } else {
                handshake = new EncryptionHandshake();
                nodeId = myKey.getNodeId();
            }
        }
    }

    @Autowired
    WorldManager worldManager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        loggerWire.debug("Received packet bytes: " + in.readableBytes());
        if (!isHandshakeDone) {
            loggerWire.debug("Decoding handshake...");
            decodeHandshake(ctx, in);
        } else
            decodeMessage(ctx, in, out);
    }

    private void decodeMessage(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        if (in.readableBytes() == 0) return;

        Frame frame = null;
        frame = frameCodec.readFrame(in);


        // Check if a full frame was available.  If not, we'll try later when more bytes come in.
        if (frame == null) return;

        byte[] payload = ByteStreams.toByteArray(frame.getStream());

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Recv: Encoded: {} [{}]", frame.getType(), Hex.toHexString(payload));

        Message msg = createMessage((byte) frame.getType(), payload);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), msg);

        EthereumListener listener = worldManager.getListener();
        listener.onRecvMessage(msg);

        out.add(msg);
        channel.getNodeStatistics().rlpxInMessages.add();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
        worldManager.getListener().trace(output);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);

        byte[] encoded = msg.getEncoded();

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Send: Encoded: {} [{}]", getCode(msg.getCommand()), Hex.toHexString(encoded));

        /*  HERE WE ACTUALLY USING THE SECRET ENCODING */
        byte code = getCode(msg.getCommand());
        Frame frame = new Frame(code, msg.getEncoded());
        frameCodec.writeFrame(frame, out);

        channel.getNodeStatistics().rlpxOutMessages.add();
    }


    public void initiate(ChannelHandlerContext ctx) throws Exception {

        loggerNet.info("RLPX protocol activated");

        nodeId = myKey.getNodeId();

        byte[] remotePublicBytes = new byte[remoteId.length + 1];
        System.arraycopy(remoteId, 0, remotePublicBytes, 1, remoteId.length);
        remotePublicBytes[0] = 0x04; // uncompressed
        ECPoint remotePublic = ECKey.fromPublicOnly(remotePublicBytes).getPubKeyPoint();
        handshake = new EncryptionHandshake(remotePublic);
        AuthInitiateMessage initiateMessage = handshake.createAuthInitiate(null, myKey);
        initiatePacket = handshake.encryptAuthMessage(initiateMessage);

        final ByteBuf byteBufMsg = ctx.alloc().buffer(initiatePacket.length);
        byteBufMsg.writeBytes(initiatePacket);
        ctx.writeAndFlush(byteBufMsg).sync();

        channel.getNodeStatistics().rlpxAuthMessagesSent.add();

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), initiateMessage);
    }

    // consume handshake, producing no resulting message to upper layers
    private void decodeHandshake(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

        if (handshake.isInitiator()) {
            if (frameCodec == null) {
                byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
                if (!buffer.isReadable(responsePacket.length))
                    return;
                buffer.readBytes(responsePacket);

                AuthResponseMessage response = this.handshake.handleAuthResponse(myKey, initiatePacket, responsePacket);
                if (loggerNet.isInfoEnabled())
                    loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), response);

                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                loggerNet.info("auth exchange done");
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
            } else {
                loggerWire.info("MessageCodec: Buffer bytes: " + buffer.readableBytes());
                Frame frame = frameCodec.readFrame(buffer);
                if (frame == null)
                    return;
                byte[] payload = ByteStreams.toByteArray(frame.getStream());
                if (frame.getType() == P2pMessageCodes.HELLO.asByte()) {
                    HelloMessage helloMessage = new HelloMessage(payload);
                    if (loggerNet.isInfoEnabled())
                        loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), helloMessage);
                    isHandshakeDone = true;
                    this.channel.publicRLPxHandshakeFinished(ctx, helloMessage);
                } else {
                    DisconnectMessage message = new DisconnectMessage(payload);
                    if (loggerNet.isInfoEnabled())
                        loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), message);
                    channel.getNodeStatistics().nodeDisconnectedRemote(message.getReason());
                }
            }
        } else {
            loggerWire.debug("Not initiator.");
            if (frameCodec == null) {
                loggerWire.debug("FrameCodec == null");
                byte[] authInitPacket = new byte[AuthInitiateMessage.getLength() + ECIESCoder.getOverhead()];
                if (!buffer.isReadable(authInitPacket.length))
                    return;
                buffer.readBytes(authInitPacket);

                this.handshake = new EncryptionHandshake();

                AuthInitiateMessage initiateMessage;
                try {
                    initiateMessage = handshake.decryptAuthInitiate(authInitPacket, myKey);
                } catch (InvalidCipherTextException ce) {
                    loggerNet.warn("Can't decrypt AuthInitiateMessage from " + ctx.channel().remoteAddress() +
                            ". Most likely the remote peer used wrong public key (NodeID) to encrypt message.");
                    return;
                }

                loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), initiateMessage);

                AuthResponseMessage response = handshake.makeAuthInitiate(initiateMessage, myKey);

                loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), response);

                byte[] responsePacket = handshake.encryptAuthReponse(response);
                handshake.agreeSecret(authInitPacket, responsePacket);

                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                ECPoint remotePubKey = this.handshake.getRemotePublicKey();

                byte[] compressed = remotePubKey.getEncoded();

                this.remoteId = new byte[compressed.length - 1];
                System.arraycopy(compressed, 1, this.remoteId, 0, this.remoteId.length);
                channel.setNode(remoteId);

                final ByteBuf byteBufMsg = ctx.alloc().buffer(responsePacket.length);
                byteBufMsg.writeBytes(responsePacket);
                ctx.writeAndFlush(byteBufMsg).sync();
            } else {
                Frame frame = frameCodec.readFrame(buffer);
                if (frame == null)
                    return;
                Message message = new P2pMessageFactory().create((byte) frame.getType(),
                        ByteStreams.toByteArray(frame.getStream()));
                loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), message);

                if (frame.getType() == P2pMessageCodes.DISCONNECT.asByte()) {
                    loggerNet.info("Active remote peer disconnected right after handshake.");
                    return;
                }

                if (frame.getType() != P2pMessageCodes.HELLO.asByte()) {
                    throw new RuntimeException("The message type is not HELLO or DISCONNECT: " + message);
                }

                HelloMessage helloMessage = (HelloMessage) message;

                // Secret authentication finish here
                isHandshakeDone = true;
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
                this.channel.publicRLPxHandshakeFinished(ctx, helloMessage);
            }
        }
        channel.getNodeStatistics().rlpxInHello.add();
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
        if (ethMessageFactory != null && EthMessageCodes.inRange(resolved)) {
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

        throw new IllegalArgumentException("No such message: " + code + " [" + Hex.encode(payload) + "]");
    }

    public void setRemoteId(String remoteId, Channel channel){
        this.remoteId = Hex.decode(remoteId);
        this.channel = channel;
    }

    /**
     * Generate random Key (and thus NodeID) per channel for 'anonymous'
     * connection (e.g. for peer discovery)
     */
    public void generateTempKey() {
        myKey = new ECKey().decompress();
    }

    public byte[] getRemoteId() {
        return remoteId;
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