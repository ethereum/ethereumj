package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.gui.PeerListener;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 12:28
 */
public class ClientPeer {

    PeerListener peerListener;

    public ClientPeer() {
    }

    public ClientPeer(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    public void connect(String host, int port){

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);

            final EthereumProtocolHandler handler;
            if (peerListener != null){
                handler = new EthereumProtocolHandler(peerListener);
                peerListener.console("connecting to: " + host + ":" + port);
            }
            else
                handler = new EthereumProtocolHandler();

            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {

                    ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(15));
                    ch.pipeline().addLast(new EthereumFrameDecoder());
                    ch.pipeline().addLast(handler);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();


        } catch (InterruptedException ie){

           System.out.println("-- ClientPeer: catch (InterruptedException ie) --");
           ie.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }


    }
}
