package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.ProtocolHandler;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.P2pMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private EncryptionHandshake handshake;
    private byte[] initiatePacket;
    private boolean isHandshakeDone;

    private MessageCodecListener listener;

    private HelloMessage helloMessage;

    private ArrayList<ProtocolHandler> protocolHandlers = new ArrayList<>();

    @Autowired
    EthereumListener ethereumListener;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        listener.onMessageCodecException(ctx, cause);
        ctx.close();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            loggerWire.debug("Received packet bytes: " + in.readableBytes());
            if (!isHandshakeDone) {
                loggerWire.debug("Decoding handshake...");
                decodeHandshake(ctx, in);
            } else
                decodeMessage(ctx, in, out);
        } catch (Exception e) {
            loggerNet.error("Exception decoding message: {}", e);
            throw e;
        }
    }

    private void decodeMessage(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() == 0) return;

        Frame frame = null;
        frame = frameCodec.readFrame(in);

        // Check if a full frame was available.  If not, we'll try later when more bytes come in.
        if (frame == null) return;

        byte[] payload = ByteStreams.toByteArray(frame.getStream());

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Recv: Encoded: {} [{}]", frame.getType(), Hex.toHexString(payload));

        Message msg = createMessage((byte) frame.getType(), payload);

        listener.onMessageDecoded(msg);
        out.add(msg);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        try {
		    String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
		    ethereumListener.trace(output);

		    byte[] encoded = msg.getEncoded();
		    listener.onMessageEncoded(msg);

		    if (loggerWire.isDebugEnabled())
		        loggerWire.debug("Send: Encoded: {} [{}]", getCode(msg.getCommand()), Hex.toHexString(encoded));

		    /*  HERE WE ACTUALLY USING THE SECRET ENCODING */
		    byte code = getCode(msg.getCommand());
		    Frame frame = new Frame(code, msg.getEncoded());
		    frameCodec.writeFrame(frame, out);
        } catch(Exception e) {
            loggerNet.error("Exception encoding message: {}", e);
            throw e;
        }
    }

    // consume handshake, producing no resulting message to upper layers
    private void decodeHandshake(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

        if (handshake.isInitiator()) {
            if (frameCodec == null) {
                byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
                if (!buffer.isReadable(responsePacket.length))
                    return;
                buffer.readBytes(responsePacket);

                AuthResponseMessage response = this.handshake.handleAuthResponse(initiatePacket, responsePacket);
                if (loggerNet.isInfoEnabled())
                    loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), response);

                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                loggerNet.info("auth exchange done");
                sendHelloMessage(ctx, frameCodec);
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
                    listener.onHelloMessageReceived(helloMessage);
                    isHandshakeDone = true;
                    listener.onRLPxHandshakeFinished(ctx, helloMessage);
                } else {
                    DisconnectMessage message = new DisconnectMessage(payload);
                    listener.onRLPxDisconnect(message);
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

                AuthInitiateMessage initiateMessage;
                try {
                    initiateMessage = handshake.decryptAuthInitiate(authInitPacket, null);
                } catch (InvalidCipherTextException ce) {
                    loggerNet.warn("Can't decrypt AuthInitiateMessage from " + ctx.channel().remoteAddress() +
                            ". Most likely the remote peer used wrong public key (NodeID) to encrypt message.");
                    return;
                }

                loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), initiateMessage);

                AuthResponseMessage response = handshake.makeAuthInitiate(initiateMessage, null);

                loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), response);

                byte[] responsePacket = handshake.encryptAuthReponse(response);
                handshake.agreeSecret(authInitPacket, responsePacket);

                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                frameCodec = new FrameCodec(secrets);

                ECPoint remotePubKey = handshake.getRemotePublicKey();
                listener.onAuthentificationRequest(initiateMessage, remotePubKey);

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
                listener.onHelloMessageReceived(helloMessage);

                // Secret authentication finish here
                isHandshakeDone = true;
                sendHelloMessage(ctx, frameCodec);
                listener.onRLPxHandshakeFinished(ctx, helloMessage);
            }
        }
    }

    public void addProtocol(ProtocolHandler protocolHandler) {

        this.protocolHandlers.add(protocolHandler);
    }

    public void sendHelloMessage(ChannelHandlerContext ctx, FrameCodec frameCodec) throws IOException, InterruptedException {

        byte[] payload = helloMessage.getEncoded();

        ByteBuf byteBufMsg = ctx.alloc().buffer();
        frameCodec.writeFrame(new FrameCodec.Frame(helloMessage.getCode(), payload), byteBufMsg);
        ctx.writeAndFlush(byteBufMsg).sync();

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), helloMessage);
        listener.onHelloMessageSent(helloMessage);
    }

    public void sendInitiateMessage(ChannelHandlerContext ctx, ECKey myKey, ECPoint remotePublic)  throws Exception {

        handshake = remotePublic != null ? new EncryptionHandshake(myKey, remotePublic) : new EncryptionHandshake(myKey);
        AuthInitiateMessage initiateMessage = handshake.createAuthInitiate(null);
        initiatePacket = handshake.encryptAuthMessage(initiateMessage);
        final ByteBuf byteBufMsg = ctx.alloc().buffer(initiatePacket.length);
        byteBufMsg.writeBytes(initiatePacket);
        ctx.writeAndFlush(byteBufMsg).sync();
        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), initiatePacket);
    }

    private byte getCode(Enum msgCommand){

        byte code = 0;
        int offset = 0;
        for (ProtocolHandler protocolHandler : protocolHandlers) {
            if (protocolHandler.hasCommand(msgCommand)) {
                code = protocolHandler.getCommandCode(msgCommand);
                code += offset;
                break;
            } else {
                offset += protocolHandler.getMaxCommandCode() + 1;
            }
        }

        return code;
    }

    private Message createMessage(byte code, byte[] payload) {

        int offset = 0;
        for (ProtocolHandler protocolHandler : protocolHandlers) {
            if (code < offset) break;
            byte resolved = (byte)(code - offset);
            if (protocolHandler.hasCommandCode(resolved)) {
                return protocolHandler.createMessage(resolved, payload);
            } else {
                offset += protocolHandler.getMaxCommandCode() + 1;
            }
        }

        throw new IllegalArgumentException("No such message: " + code + " [" + Hex.toHexString(payload) + "]");
    }

    public void setHelloMessage(HelloMessage helloMessage) {
        this.helloMessage = helloMessage;
    }

    public void setListener(MessageCodecListener listener){
        this.listener = listener;
    }

}