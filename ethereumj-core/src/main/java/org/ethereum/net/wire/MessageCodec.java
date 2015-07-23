package org.ethereum.net.wire;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageCodec;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.server.Channel;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static org.ethereum.net.rlpx.FrameCodec.Frame;
import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageCodec extends ByteToMessageCodec<Message> {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private FrameCodec frameCodec;
    private ECKey myKey = ECKey.fromPrivate(CONFIG.privateKey().getBytes()).decompress();
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
                initiate(ctx);
            } else {
                handshake = new EncryptionHandshake();
                byte[] nodeIdWithFormat = myKey.getPubKey();
                nodeId = new byte[nodeIdWithFormat.length - 1];
                System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);
            }
        }
    }

    @Autowired
    WorldManager worldManager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        loggerWire.debug("Received packet bytes: " + in.readableBytes());
        if (!isHandshakeDone)
            decodeHandshake(ctx, in);
        else
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

        Message msg = MessageFactory.createMessage((byte) frame.getType(), payload);

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

        channel.getShhHandler().setPrivKey(myKey);
        byte[] nodeIdWithFormat = myKey.getPubKey();
        nodeId = new byte[nodeIdWithFormat.length - 1];
        System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);


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
                loggerNet.info("MessageCodec: Buffer bytes: " + buffer.readableBytes());
                Frame frame = frameCodec.readFrame(buffer);
                if (frame == null)
                    return;
                byte[] payload = ByteStreams.toByteArray(frame.getStream());
                if (frame.getType() == P2pMessageCodes.HELLO.asByte()) {
                    HelloMessage helloMessage = new HelloMessage(payload);
                    if (loggerNet.isInfoEnabled())
                        loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), helloMessage);
                    isHandshakeDone = true;
                    this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage, nodeId);
                } else {
                    DisconnectMessage message = new DisconnectMessage(payload);
                    if (loggerNet.isInfoEnabled())
                        loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), message);
                    channel.getNodeStatistics().nodeDisconnectedRemote(message.getReason());
                }
            }
        } else {
            if (frameCodec == null) {
                byte[] authInitPacket = new byte[AuthInitiateMessage.getLength() + ECIESCoder.getOverhead()];
                if (!buffer.isReadable(authInitPacket.length))
                    return;
                buffer.readBytes(authInitPacket);

                this.handshake = new EncryptionHandshake();
                byte[] responsePacket = this.handshake.handleAuthInitiate(authInitPacket, myKey);
                EncryptionHandshake.Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                ECPoint remotePubKey = this.handshake.getRemotePublicKey();
                this.remoteId = remotePubKey.getEncoded();
                this.channel.init(Hex.toHexString(this.remoteId), false);

                final ByteBuf byteBufMsg = ctx.alloc().buffer(responsePacket.length);
                byteBufMsg.writeBytes(responsePacket);
                ctx.writeAndFlush(byteBufMsg).sync();
            } else {
                Frame frame = frameCodec.readFrame(buffer);
                if (frame == null)
                    return;
                byte[] payload = ByteStreams.toByteArray(frame.getStream());
                HelloMessage helloMessage = new HelloMessage(payload);
                if (loggerNet.isInfoEnabled())
                    loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), helloMessage);

                // Secret authentication finish here
                isHandshakeDone = true;
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
                this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage, nodeId);
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
            code = ((P2pMessageCodes)msgCommand).asByte();
        }

        if (msgCommand instanceof EthMessageCodes){
            code = ((EthMessageCodes)msgCommand).asByte();
        }

        if (msgCommand instanceof ShhMessageCodes){
            code = ((ShhMessageCodes)msgCommand).asByte();
        }

        if (msgCommand instanceof BzzMessageCodes){
            code = ((BzzMessageCodes)msgCommand).asByte();
        }

        return code;
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
}