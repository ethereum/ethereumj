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

    EncryptionHandshake initiator;
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
        initiator = new EncryptionHandshake(remotePublic);
        AuthInitiateMessage initiateMessage = initiator.createAuthInitiate(null, myKey);
        initiatePacket = initiator.encryptAuthMessage(initiateMessage);

        final ByteBuf byteBufMsg = ctx.alloc().buffer(initiatePacket.length);
        byteBufMsg.writeBytes(initiatePacket);
        ctx.writeAndFlush(byteBufMsg).sync();

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buffer = ((ByteBuf)msg);

        if (frameCodec == null){

            byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
            buffer.readBytes(responsePacket);

            this.initiator.handleAuthResponse(myKey, initiatePacket, responsePacket);
            Secrets secrets = this.initiator.getSecrets();
            this.frameCodec = new FrameCodec(secrets, null, null);

            System.out.println("[Auth exchange done]");
        } else{

            Frame frame = frameCodec.readFrame(buffer);
            byte[] payload = ByteStreams.toByteArray(frame.getStream());
            HelloMessage helloMessage = new HelloMessage(payload);

            // Secret authentication finish here
            ctx.pipeline().remove(this);
            this.channel.publicRLPxHandshakeFinished(ctx, frameCodec, helloMessage, nodeId);
        }
    }


    public void setRemoteId(String remoteId, Channel channel){
        this.remoteId = remoteId;
        this.channel = channel;
    }


}
