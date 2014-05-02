package samples.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 08:20
 */
public class Server {

    int port;


    public Server(int port) {
        this.port = port;
    }


    public void start(){

        EventLoopGroup bossGroup  = new NioEventLoopGroup();
        EventLoopGroup workerGroup  = new NioEventLoopGroup();


        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                   .channel(NioServerSocketChannel.class)
                   .childHandler(new ChannelInitializer<NioSocketChannel>() {

                        public void initChannel(NioSocketChannel channel){

                            channel.pipeline().addLast(new ServerMessageHandler());

                        };

                    }).option(ChannelOption.SO_BACKLOG, 128)
                      .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {

            System.out.println("Server started");
            ChannelFuture future = bootstrap.bind(port).sync();

            future.channel().closeFuture().sync();



        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String args[]){

        new Server(10101).start();

    }
}
