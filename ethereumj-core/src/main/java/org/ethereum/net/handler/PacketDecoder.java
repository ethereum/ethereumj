package org.ethereum.net.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Roman Mandeleil
 * Created on: 13/04/14 21:51
 */
public class EthereumFrameDecoder extends ByteToMessageDecoder {

	private Logger logger = LoggerFactory.getLogger("wire");

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		// Ethereum message is at least 8 bytes
		if (in.readableBytes() < 8)
			return;

		long magicBytes = in.readUnsignedInt();
		long msgSize = in.readUnsignedInt();

        if (!((magicBytes >> 24   &  0xFF) == 0x22  &&
              (magicBytes >> 16   &  0xFF) == 0x40  &&
              (magicBytes >>  8   &  0xFF) == 0x08  &&
              (magicBytes         &  0xFF) == 0x91 )) {

			// TODO: Drop frame and continue.
			// A collision can happen (although rare)
			// If this happens too often, it's an attack.
			// In that case, drop the peer.

			logger.error("abandon garbage, wrong magic bytes: [{}] msgSize: [{}]", magicBytes, msgSize);
			ctx.close();
		}

		// Don't have the full packet yet
		if (msgSize > in.readableBytes()) {
			logger.trace("msg decode: magicBytes: [{}], readBytes: [{}] / msgSize: [{}] ",
					magicBytes, in.readableBytes(), msgSize);
			in.resetReaderIndex();
			return;
		}

		logger.trace("message fully constructed go handle it: readBytes: [{}] / msgSize: [{}]",
				in.readableBytes(), msgSize);

		byte[] decoded = new byte[(int) msgSize];
		in.readBytes(decoded);

		out.add(decoded);

		in.markReaderIndex();
    }
}