package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
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
import org.ethereum.util.ByteUtil;
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
import static org.ethereum.util.ByteUtil.bigEndianToShort;
import static org.ethereum.util.ByteUtil.merge;

/**
 * The Netty handler which manages initial negotiation with peer
 * (when either we initiating connection or remote peer initiates)
 *
 * The initial handshake includes:
 * - first AuthInitiate -> AuthResponse messages when peers exchange with secrets
 * - second P2P Hello messages when P2P protocol and subprotocol capabilities are negotiated
 *
 * After the handshake is done this handler reports secrets and other data to the Channel
 * which installs further handlers depending on the protocol parameters.
 * This handler is finally removed from the pipeline.
 */
@Component
@Scope("prototype")
public class HandshakeHandler extends ByteToMessageDecoder {

    @Autowired
    SystemProperties config;

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private FrameCodec frameCodec;
    private ECKey myKey;
    private byte[] nodeId;
    private byte[] remoteId;
    private EncryptionHandshake handshake;
    private byte[] initiatePacket;
    private Channel channel;
    private boolean isHandshakeDone;

    public HandshakeHandler() {
    }

    @PostConstruct
    private void init() {
        myKey = config.getMyKey();
    }

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

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        loggerWire.debug("Decoding handshake... (" + in.readableBytes() + " bytes available)");
        decodeHandshake(ctx, in);
        if (isHandshakeDone) {
            loggerWire.debug("Handshake done, removing HandshakeHandler from pipeline.");
            ctx.pipeline().remove(this);
        }
    }

    public void initiate(ChannelHandlerContext ctx) throws Exception {

        loggerNet.debug("RLPX protocol activated");

        nodeId = myKey.getNodeId();

        byte[] remotePublicBytes = new byte[remoteId.length + 1];
        System.arraycopy(remoteId, 0, remotePublicBytes, 1, remoteId.length);
        remotePublicBytes[0] = 0x04; // uncompressed
        ECPoint remotePublic = ECKey.fromPublicOnly(remotePublicBytes).getPubKeyPoint();
        handshake = new EncryptionHandshake(remotePublic);

        Object msg;
        if (config.eip8()) {
            AuthInitiateMessageV4 initiateMessage = handshake.createAuthInitiateV4(myKey);
            initiatePacket = handshake.encryptAuthInitiateV4(initiateMessage);
            msg = initiateMessage;
        } else {
            AuthInitiateMessage initiateMessage = handshake.createAuthInitiate(null, myKey);
            initiatePacket = handshake.encryptAuthMessage(initiateMessage);
            msg = initiateMessage;
        }

        final ByteBuf byteBufMsg = ctx.alloc().buffer(initiatePacket.length);
        byteBufMsg.writeBytes(initiatePacket);
        ctx.writeAndFlush(byteBufMsg).sync();

        channel.getNodeStatistics().rlpxAuthMessagesSent.add();

        if (loggerNet.isDebugEnabled())
            loggerNet.debug("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);
    }

    // consume handshake, producing no resulting message to upper layers
    private void decodeHandshake(final ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

        if (handshake.isInitiator()) {
            if (frameCodec == null) {

                byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
                if (!buffer.isReadable(responsePacket.length))
                    return;
                buffer.readBytes(responsePacket);

                try {

                    // trying to decode as pre-EIP-8

                    AuthResponseMessage response = handshake.handleAuthResponse(myKey, initiatePacket, responsePacket);
                    loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), response);

                } catch (Throwable t) {

                    // it must be format defined by EIP-8 then

                    responsePacket = readEIP8Packet(buffer, responsePacket);

                    if (responsePacket == null) return;

                    AuthResponseMessageV4 response = handshake.handleAuthResponseV4(myKey, initiatePacket, responsePacket);
                    loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), response);
                }

                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                loggerNet.debug("auth exchange done");
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
                    if (loggerNet.isDebugEnabled())
                        loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), helloMessage);
                    isHandshakeDone = true;
                    this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage);
                } else {
                    DisconnectMessage message = new DisconnectMessage(payload);
                    if (loggerNet.isDebugEnabled())
                        loggerNet.debug("From: \t{} \tRecv: \t{}", channel, message);
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

                byte[] responsePacket;

                try {

                    // trying to decode as pre-EIP-8
                    AuthInitiateMessage initiateMessage = handshake.decryptAuthInitiate(authInitPacket, myKey);
                    loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), initiateMessage);

                    AuthResponseMessage response = handshake.makeAuthInitiate(initiateMessage, myKey);
                    loggerNet.debug("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), response);
                    responsePacket = handshake.encryptAuthResponse(response);

                } catch (Throwable t) {

                    // it must be format defined by EIP-8 then
                    try {

                        authInitPacket = readEIP8Packet(buffer, authInitPacket);

                        if (authInitPacket == null) return;

                        AuthInitiateMessageV4 initiateMessage = handshake.decryptAuthInitiateV4(authInitPacket, myKey);
                        loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), initiateMessage);

                        AuthResponseMessageV4 response = handshake.makeAuthInitiateV4(initiateMessage, myKey);
                        loggerNet.debug("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), response);
                        responsePacket = handshake.encryptAuthResponseV4(response);

                    } catch (InvalidCipherTextException ce) {
                        loggerNet.warn("Can't decrypt AuthInitiateMessage from " + ctx.channel().remoteAddress() +
                                ". Most likely the remote peer used wrong public key (NodeID) to encrypt message.");
                        return;
                    }
                }

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
                loggerNet.debug("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), message);

                if (frame.getType() == P2pMessageCodes.DISCONNECT.asByte()) {
                    loggerNet.debug("Active remote peer disconnected right after handshake.");
                    return;
                }

                if (frame.getType() != P2pMessageCodes.HELLO.asByte()) {
                    throw new RuntimeException("The message type is not HELLO or DISCONNECT: " + message);
                }

                HelloMessage inboundHelloMessage = (HelloMessage) message;

                // Secret authentication finish here
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId), inboundHelloMessage);
                isHandshakeDone = true;
                this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, inboundHelloMessage);
            }
        }
        channel.getNodeStatistics().rlpxInHello.add();
    }

    private byte[] readEIP8Packet(ByteBuf buffer, byte[] plainPacket) {

        int size = bigEndianToShort(plainPacket);
        if (size < plainPacket.length)
            throw new IllegalArgumentException("AuthResponse packet size is too low");

        int bytesLeft = size - plainPacket.length + 2;
        byte[] restBytes = new byte[bytesLeft];

        if (!buffer.isReadable(restBytes.length))
            return null;

        buffer.readBytes(restBytes);

        byte[] fullResponse = new byte[size + 2];
        System.arraycopy(plainPacket, 0, fullResponse, 0, plainPacket.length);
        System.arraycopy(restBytes, 0, fullResponse, plainPacket.length, restBytes.length);

        return fullResponse;
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
            loggerNet.debug("Handshake failed: ", cause);
        } else {
            if (cause instanceof IOException) {
                loggerNet.debug("Handshake failed: " + ctx.channel().remoteAddress(), cause);
            } else {
                loggerNet.warn("Handshake failed: ", cause);
            }
        }
        ctx.close();
    }
}