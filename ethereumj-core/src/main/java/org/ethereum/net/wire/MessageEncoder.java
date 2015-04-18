package org.ethereum.net.wire;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.rlpx.FrameCodec;
import org.ethereum.net.shh.ShhMessageCodes;
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

import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;
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

    private FrameCodec frameCodec;

    public void setFrameCodec(FrameCodec frameCodec) {
        this.frameCodec = frameCodec;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
        worldManager.getListener().trace(output);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);

        byte[] encoded = msg.getEncoded();

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(encoded));

        /*  HERE WE ACTUALLY USING THE SECRET ENCODING */
        byte code = getCode(msg.getCommand());
        Frame frame = new Frame(code, msg.getEncoded());
        frameCodec.writeFrame(frame, out);
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

        return code;
    }
}