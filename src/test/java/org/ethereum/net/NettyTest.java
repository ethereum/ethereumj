/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 16.10.2015.
 */
public class NettyTest {
    @Test
    public void pipelineTest() {

        final int[] int2 = new int[1];
        final boolean[] exception = new boolean[1];

        final ByteToMessageDecoder decoder2 = new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                int i = in.readInt();
                System.out.println("decoder2 read int (4 bytes): " + Integer.toHexString(i));
                int2[0] = i;
                if (i == 0) out.add("aaa");
            }
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("Decoder2 exception: " + cause);
            }
        };

        final MessageToMessageCodec decoder3 = new MessageToMessageCodec<Object, Object>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
                System.out.println("NettyTest.decode: msg = [" + msg + "]");
                if (msg == "aaa") {
                    throw new RuntimeException("Test exception 3");
                }
            }

            @Override
            protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
                throw new RuntimeException("Test exception 4");
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("Decoder3 exception: " + cause);
                exception[0] = true;
            }
        };

        final ByteToMessageDecoder decoder1 = new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                int i = in.readInt();
                System.out.println("decoder1 read int (4 bytes). Needs no more: " + Integer.toHexString(i));
                ctx.pipeline().addAfter("decoder1", "decoder2", decoder2);
                ctx.pipeline().addAfter("decoder2", "decoder3", decoder3);
                ctx.pipeline().remove(this);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("Decoder1 exception: " + cause);
            }
        };

        ChannelInboundHandlerAdapter initiator = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                ctx.pipeline().addFirst("decoder1", decoder1);
                System.out.println("NettyTest.channelActive");
            }
        };

        EmbeddedChannel channel0 = new EmbeddedChannel(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                throw new RuntimeException("Test");
            }
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("Exception caught: " + cause);
            }

        });
        EmbeddedChannel channel = new EmbeddedChannel(initiator);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(0x12345678);
        buffer.writeInt(0xabcdefff);
        channel.writeInbound(buffer);
        Assert.assertEquals(0xabcdefff, int2[0]);

        channel.writeInbound(Unpooled.buffer().writeInt(0));
        Assert.assertTrue(exception[0]);

        // Need the following for the exception in outbound handler to be fired
        // ctx.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

//        exception[0] = false;
//        channel.writeOutbound("outMsg");
//        Assert.assertTrue(exception[0]);
    }

}
