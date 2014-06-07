package org.ethereum.net.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 13/04/14 21:51
 */
public class EthereumFrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // No header for Eth. message
        if (in.readableBytes() < 8) return;

        long magicBytes = in.readUnsignedInt();
        long msgSize = in.readUnsignedInt();

        if (!((magicBytes >> 24   &  0xFF) == 0x22  &&
              (magicBytes >> 16   &  0xFF) == 0x40  &&
              (magicBytes >>  8   &  0xFF) == 0x08  &&
              (magicBytes         &  0xFF) == 0x91 )){

            System.out.println("Not ethereum packet");
            ctx.close();
        }

        // Don't have the full packet yet
        if (msgSize > in.readableBytes()) {
            in.resetReaderIndex();
            return;
        }

        byte[] decoded = new byte[(int)msgSize];
        in.readBytes(decoded);

        out.add(decoded);

        // Chop the achieved data.
        in.markReaderIndex();
    }
}