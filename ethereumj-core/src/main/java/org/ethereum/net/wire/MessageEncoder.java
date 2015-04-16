package org.ethereum.net.wire;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.rlpx.FrameCodec;
import org.ethereum.util.ByteUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.net.rlpx.FrameCodec.*;

/**
 * The PacketEncoder encodes the message and adds a sync token to every packet.
 */
@Component
@Scope("prototype")
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    @Autowired
    WorldManager worldManager;

    private boolean active = false;
    private FrameCodec frameCodec;

    public void setFrameCodec(FrameCodec frameCodec) {
        this.active = true;
        this.frameCodec = frameCodec;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        if (!active) return;

        String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
        worldManager.getListener().trace(output);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);

        byte[] encoded = msg.getEncoded();

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(encoded));

        if (frameCodec == null){
            System.out.println("You don't have RLPx set... than die painfully");
            System.exit(1);
        }

        /*  HERE WE ACTUALLY USING THE SECRET ENCODING */
        Frame frame = new Frame(msg.getCode(), msg.getEncoded());
        frameCodec.writeFrame(frame, out);
    }
}