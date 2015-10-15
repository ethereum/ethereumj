package org.ethereum.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 16.10.2015.
 */
@Ignore
public class NettyTest {
    @Test
    public void pipelineTest() {

        ByteToMessageDecoder decoder1 = new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                int i = in.readInt();
                System.out.println("decoder1 read int (4 bytes). Needs no more: " + Integer.toHexString(i));
                ctx.pipeline().remove(this);
            }
        };

        ByteToMessageDecoder decoder2 = new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                int i = in.readInt();
                System.out.println("decoder2 read int (4 bytes): " + Integer.toHexString(i));
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(decoder1, decoder2);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(0x12345678);
        buffer.writeInt(0xabcdefff);
        channel.writeInbound(buffer);

    }

}
