package org.ethereum.net.wire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
public class MessageDecoder extends ByteToMessageDecoder {

	private Logger logger = LoggerFactory.getLogger("wire");

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		if (!isValidEthereumPacket(in)) {
			return;
		}

		byte[] encoded = new byte[in.readInt()];
		in.readBytes(encoded);
	
		if (logger.isDebugEnabled())
			logger.debug("Encoded: [{}]", Hex.toHexString(encoded));

		Message msg = MessageFactory.createMessage(encoded);

		if (logger.isInfoEnabled())
//				&& msg.getCommand() != Command.PING
//				&& msg.getCommand() != Command.PONG 
//				&& msg.getCommand() != Command.PEERS 
//				&& msg.getCommand() != Command.GET_PEERS)
			logger.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), msg);

		out.add(msg);
        in.markReaderIndex();
    }
	
	private boolean isValidEthereumPacket(ByteBuf in) {
		// Ethereum message is at least 8 bytes
		if (in.readableBytes() < 8)
			return false;

		long syncToken = in.readUnsignedInt();

        if (!((syncToken >> 24   &  0xFF) == 0x22  &&
              (syncToken >> 16   &  0xFF) == 0x40  &&
              (syncToken >>  8   &  0xFF) == 0x08  &&
              (syncToken         &  0xFF) == 0x91 )) {

			// TODO: Drop frame and continue.
			// A collision can happen (although rare)
			// If this happens too often, it's an attack.
			// In that case, drop the peer.
			logger.error("Abandon garbage, wrong sync token: [{}]", syncToken);
		}

		// Don't have the full message yet
        long msgSize = in.getInt(in.readerIndex());
		if (msgSize > in.readableBytes()) {
			logger.trace("msg decode: magicBytes: [{}], readBytes: [{}] / msgSize: [{}] ",
					syncToken, in.readableBytes(), msgSize);
			in.resetReaderIndex();
			return false;
		}

		logger.trace("Message fully constructed: readBytes: [{}] / msgSize: [{}]", in.readableBytes(), msgSize);
		return true;
	}
}