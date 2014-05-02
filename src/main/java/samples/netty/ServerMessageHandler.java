package samples.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class ServerMessageHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        String helloString = "9191-Hello";

        final ByteBuf helloMessage = ctx.alloc().buffer(helloString.length());
        helloMessage.writeBytes(helloString.getBytes());

        final ChannelFuture channelFuture = ctx.writeAndFlush(helloMessage);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


         ByteBuf input = ((ByteBuf)msg);

        try {

            int inputSize = input.readableBytes();
            byte[] payload = new byte[inputSize];
            input.readBytes(payload);

            String newMessage = new String(payload);
            System.out.println(newMessage);

            Thread.sleep(1000);

            String answer = RomanProtocol.getAnswer(newMessage);
            final ByteBuf buffer = ctx.alloc().buffer(answer.length());
            buffer.writeBytes(answer.getBytes());
            ctx.writeAndFlush(buffer);




        } finally {
            ReferenceCountUtil.release(input);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        System.out.println("Client disconnected");
//        cause.printStackTrace();
        ctx.close();
    }
}
