package org.ethereum.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Not for regular run, but just for testing UDP client/server communication
 *
 * For running server with gradle:
 * - adjust server() params
 * - remove @Ignore from server() method
 * - > ./gradlew -Dtest.single=UdpTest test
 *
 * Created by Anton Nashatyrev on 28.12.2016.
 */
public class UdpTest {

    public Channel create(String bindAddr, int port, ChannelHandler handler) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioDatagramChannel.class)
            .handler(handler);

        return b.bind(bindAddr, port).sync().channel();
    }

    public void startServer(final String host, final int port) throws InterruptedException {
        ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("Channel initialized on " + host + ":" + port);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                DatagramPacket in = (DatagramPacket) msg;
                DatagramPacket packet = new DatagramPacket(in.content(), in.sender());
                System.out.println("Message received: " + in.content() + ", sending back to " + in.sender());
                ctx.writeAndFlush(packet);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
            }
        };
        create(host, port, handler).closeFuture().sync();
    }

    public void startClient(final String targetHost, final int targetPort, String bindAddr, int bindPort, final String message)
            throws InterruptedException {
        ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                try {
                    InetSocketAddress address = new InetSocketAddress(targetHost, targetPort);
                    DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(message.getBytes()), address);
                    System.out.println("Sending message");
                    ctx.writeAndFlush(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("Response received: " + msg);
                ctx.close();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
            }
        };
        boolean ok = create(bindAddr, bindPort, handler).closeFuture().await(10, TimeUnit.SECONDS);
        if (!ok) {
            System.out.println("ERROR: Timeout waiting for response");
        } else {
            System.out.println("OK");
        }
    }

    @Ignore
    @Test
    public void server() throws Exception {
        startServer("0.0.0.0", 30303);
    }

    @Ignore
    @Test
    public void client() throws Exception {
        startClient("localhost", 30303, "0.0.0.0", 8888, "Hello!");
    }

}
