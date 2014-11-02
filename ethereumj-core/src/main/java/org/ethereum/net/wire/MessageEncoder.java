package org.ethereum.net.wire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * The PacketEncoder encodes the message and adds a sync token to every packet.
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

	private static final Logger loggerWire = LoggerFactory.getLogger("wire");
	private static final Logger loggerNet  = LoggerFactory.getLogger("net");


	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		
		if (loggerNet.isInfoEnabled())
            loggerNet.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);

		byte[] encoded = msg.getEncoded();
		
		if (loggerWire.isDebugEnabled())
			loggerWire.debug("Encoded: [{}]", Hex.toHexString(encoded));
		
		out.capacity(encoded.length + 8);
        out.writeBytes(StaticMessages.SYNC_TOKEN);
        out.writeBytes(ByteUtil.calcPacketLength(encoded));
        out.writeBytes(encoded);

        EthereumListener ethereumListener = WorldManager.getInstance().getListener();
        if (ethereumListener != null) {
            ethereumListener.onSendMessage(msg);
        }
    }
}