package samples.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class ClientMessageHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


         ByteBuf input = ((ByteBuf)msg);

        try {

            int inputSize = input.readableBytes();
            byte[] payload = new byte[inputSize];
            input.readBytes(payload);

            if (payload.length < 5){
                System.out.println("Not a Roman server disconnect");
                ctx.close();
            }

            String prefix = new String(payload, 0, 5);

            if (!prefix.equals("9191-")){
                System.out.println("Not a Roman server disconnect");
                ctx.close();
            }


            String newMessage = new String(payload);
            System.out.println(newMessage);

            Thread.sleep(1000);

            String answer = RomanProtocol.getAnswer(newMessage);
            final ByteBuf buffer = ctx.alloc().buffer(answer.length());
            buffer.writeBytes(answer.getBytes());
            ctx.writeAndFlush(buffer);

//            String answer2 = "cool sir 2!!!";
//            final ByteBuf helloMessage2 = ctx.alloc().buffer(answer2.length());
//            helloMessage2.writeBytes(answer2.getBytes());
//            ctx.writeAndFlush(helloMessage2);


        } finally {
            ReferenceCountUtil.release(input);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
