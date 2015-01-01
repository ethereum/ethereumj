package org.ethereum.net.wire;

import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.util.ByteUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The PacketEncoder encodes the message and adds a sync token to every packet.
 */
@Component
@Scope("prototype")
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    @Inject
    WorldManager worldManager;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        String output = String.format("To: \t%s \tSend: \t%s", ctx.channel().remoteAddress(), msg);
        worldManager.getListener().trace(output);

        if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);

        byte[] encoded = msg.getEncoded();

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(encoded));

        out.capacity(encoded.length + 8);
        out.writeBytes(StaticMessages.SYNC_TOKEN);
        out.writeBytes(ByteUtil.calcPacketLength(encoded));
        out.writeBytes(encoded);
    }
}