package org.ethereum.net.rlpx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;
import static org.ethereum.net.rlpx.EncryptionHandshake.Secrets;


@Component
@Scope("prototype")
public class RLPXHandler extends ByteToMessageDecoder {

    private final static Logger logger = LoggerFactory.getLogger("net");
    private boolean active = false;
    private String remoteId = "00";

    EncryptionHandshake initiator;
    ECKey myKey;
    byte[] initiatePacket;
    byte[] nodeId;

    private Channel channel;
    private MessageQueue msgQueue;

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        msgQueue.activate(ctx);

        logger.info("RLPX protocol activated");
        active = true;

        myKey = new ECKey().decompress();
        byte[] nodeIdWithFormat = myKey.getPubKey();
        nodeId = new byte[nodeIdWithFormat.length - 1];
        System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);


        byte[] remoteId = Hex.decode(this.remoteId);
        byte[] remotePublicBytes = new byte[remoteId.length + 1];
        System.arraycopy(remoteId, 0, remotePublicBytes, 1, remoteId.length);
        remotePublicBytes[0] = 0x04; // uncompressed
        ECPoint remotePublic = ECKey.fromPublicOnly(remotePublicBytes).getPubKeyPoint();
        initiator = new EncryptionHandshake(remotePublic);
        AuthInitiateMessage initiateMessage = initiator.createAuthInitiate(null, myKey);
        initiatePacket = initiator.encryptAuthMessage(initiateMessage);

        final ByteBuf byteBufMsg = ctx.alloc().buffer(initiatePacket.length);
        byteBufMsg.writeBytes(initiatePacket);
        ctx.writeAndFlush(byteBufMsg).sync();

    }



    public boolean isActive() {
        return active;
    }

    public void setRemoteId(String remoteId, Channel channel){
        this.remoteId = remoteId;
        this.channel = channel;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (!active){
            out.add(in);
            return;
        }

        if (in.readableBytes() < AuthResponseMessage.getLength()) return; // didn't got all the message yet

        byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
        in.readBytes(responsePacket);
        in.markReaderIndex();

        this.initiator.handleAuthResponse(myKey, initiatePacket, responsePacket);
        Secrets secrets = this.initiator.getSecrets();

        this.channel.publicRLPxHandshakeFinished(new FrameCodec(secrets, null, null));

        HELLO_MESSAGE.setPeerId(Hex.toHexString(this.nodeId));
        FrameCodec frameCodec = new FrameCodec(secrets, null, null);
        byte[] payload = HELLO_MESSAGE.getEncoded();

        final ByteBuf byteBufMsg = ctx.alloc().buffer(payload.length);

        frameCodec.writeFrame(new FrameCodec.Frame(HELLO_MESSAGE.getCode(), payload), byteBufMsg);
        ctx.writeAndFlush(byteBufMsg).sync();




    }

}
