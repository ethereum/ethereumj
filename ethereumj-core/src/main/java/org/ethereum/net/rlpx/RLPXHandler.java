package org.ethereum.net.rlpx;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.FrameCodec.Frame;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.net.rlpx.EncryptionHandshake.Secrets;


@Component
@Scope("prototype")
public class RLPXHandler extends SimpleChannelInboundHandler {

    private final static Logger logger = LoggerFactory.getLogger("net");
    private String remoteId = "00";

    EncryptionHandshake handshake;
    ECKey myKey;
    byte[] initiatePacket;
    byte[] nodeId;

    private Channel channel;
    private MessageQueue msgQueue;

    FrameCodec frameCodec = null;

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        logger.info("RLPX protocol activated");

        myKey = new ECKey().decompress();
        byte[] nodeIdWithFormat = myKey.getPubKey();
        nodeId = new byte[nodeIdWithFormat.length - 1];
        System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);


        byte[] remoteId = Hex.decode(this.remoteId);
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

        if (logger.isInfoEnabled())
            logger.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), initiateMessage);

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buffer = ((ByteBuf)msg);

        if (handshake.isInitiator()) {
            if (frameCodec == null) {
                byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
                buffer.readBytes(responsePacket);

                this.handshake.handleAuthResponse(myKey, initiatePacket, responsePacket);
                Secrets secrets = this.handshake.getSecrets();
                this.frameCodec = new FrameCodec(secrets);

                logger.info("auth exchange done");
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
            } else {
                Frame frame = frameCodec.readFrame(buffer);
                byte[] payload = ByteStreams.toByteArray(frame.getStream());
                HelloMessage helloMessage = new HelloMessage(payload);
                if (logger.isInfoEnabled())
                    logger.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), helloMessage);
                ctx.pipeline().remove(this);
                this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage, nodeId);
            }
        } else {
            if (frameCodec == null) {
                // Respond to auth
                throw new UnsupportedOperationException();
            } else {
                Frame frame = frameCodec.readFrame(buffer);
                byte[] payload = ByteStreams.toByteArray(frame.getStream());
                HelloMessage helloMessage = new HelloMessage(payload);
                System.out.println("hello message received");

                // Secret authentication finish here
                ctx.pipeline().remove(this);
                channel.sendHelloMessage(ctx, frameCodec, Hex.toHexString(nodeId));
                this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage, nodeId);
            }
        }
    }


    public void setRemoteId(String remoteId, Channel channel){
        this.remoteId = remoteId;
        this.channel = channel;
    }


}
