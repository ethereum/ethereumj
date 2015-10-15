package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.P2pMessageFactory;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static org.ethereum.net.rlpx.FrameCodec.Frame;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class HandshakeHandler extends ByteToMessageDecoder  /*ChannelInboundHandlerAdapter*/ {

    @Autowired
    SystemProperties config;

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    public FrameCodec frameCodec;
    private ECKey myKey;
    private byte[] nodeId;
    private byte[] remoteId;
    private EncryptionHandshake handshake;
    private byte[] initiatePacket;
    private Channel channel;
    private boolean isHandshakeDone;
    private final InitiateHandler initiator = new InitiateHandler();

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

    public HandshakeHandler() {
    }

    // for testing purposes
    HandshakeHandler(ECKey myKey, EncryptionHandshake.Secrets secrets) {
        this.myKey = myKey;
        nodeId = myKey.getNodeId();
        frameCodec = new FrameCodec(secrets);
    }

    @PostConstruct
    private void init() {
        myKey = config.getMyKey();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isHandshakeDone) {
            loggerWire.debug("=== ctx.fireChannelRead: " + ((ByteBuf) msg).readableBytes());
            ctx.fireChannelRead(msg);
        } else {
            super.channelRead(ctx, msg);
            if (isHandshakeDone) {
                loggerWire.debug("=== ctx.fireChannelRead: " + ((ByteBuf) msg).readableBytes());
                ctx.fireChannelRead(msg);
            }
        }
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        loggerWire.debug("Received packet bytes: " + in.readableBytes());
        if (!isHandshakeDone) {
            loggerWire.debug("Decoding handshake... (" + in.readableBytes() + " bytes available)");
            decodeHandshake(ctx, in);
            loggerWire.debug("Decoded handshake (" + in.readableBytes() + " bytes available)");
            in.retain();
        }
//            decodeMessage(ctx, in, out);
    }

    public void decodeMessage(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        if (in.readableBytes() == 0) return;

        List<Frame> frames = frameCodec.readFrames(in);


        // Check if a full frame was available.  If not, we'll try later when more bytes come in.
        if (frames == null || frames.isEmpty()) return;

        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);

            if (loggerWire.isDebugEnabled())
                loggerWire.debug("Recv: Encoded: (" + (i + 1) + " of " + frames.size() + ") " +
                        frame.getType() + " [size: " + frame.getStream().available() + "]");
        }

        out.addAll(frames);
        channel.getNodeStatistics().rlpxInMessages.add();
    }

    public void encode(ChannelHandlerContext ctx, Frame frame, ByteBuf out) throws Exception {

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
    private void decodeHandshake(final ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

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
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(500);
//                            channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId), null);
            } else {
                loggerWire.info("MessageCodec: Buffer bytes: " + buffer.readableBytes());
                List<Frame> frames = frameCodec.readFrames(buffer);
                if (frames == null || frames.isEmpty())
                    return;
                Frame frame = frames.get(0);
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
                        loggerNet.info("From: \t{} \tRecv: \t{}", channel, message);
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
                List<Frame> frames = frameCodec.readFrames(buffer);
                if (frames == null || frames.isEmpty())
                    return;
                Frame frame = frames.get(0);

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

                HelloMessage inboundHelloMessage = (HelloMessage) message;

                // Secret authentication finish here
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId), inboundHelloMessage);
                isHandshakeDone = true;
                this.channel.publicRLPxHandshakeFinished(ctx, inboundHelloMessage);
            }
        }
        channel.getNodeStatistics().rlpxInHello.add();
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
            loggerNet.debug("MultiFrameCodec handling failed", cause);
        } else {
            if (cause instanceof IOException) {
                loggerNet.info("Connection with peer terminated: " + ctx.channel().remoteAddress() + "(" + cause.getMessage() + ")");
                loggerNet.debug("Connection with peer terminated: " + ctx.channel().remoteAddress(), cause);
            } else {
                loggerNet.error("MultiFrameCodec handling failed", cause);
            }
        }
        ctx.close();
    }
}