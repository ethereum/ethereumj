package org.ethereum.net.rlpx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Since decoder field is not modifiable in the ByteToMessageCodec this class
 * overrides it to set the COMPOSITE_CUMULATOR for ByteToMessageDecoder as it
 * is more effective than the default one.
 */
public abstract class NettiByteToMessageCodec<I> extends ByteToMessageCodec<I> {

    private final ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
        {
            setCumulator(COMPOSITE_CUMULATOR);
        }

        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            NettiByteToMessageCodec.this.decode(ctx, in, out);
        }

        @Override
        protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            NettiByteToMessageCodec.this.decodeLast(ctx, in, out);
        }
    };

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decoder.channelRead(ctx, msg);
    }
}
