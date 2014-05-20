package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.core.Transaction;
import org.ethereum.gui.PeerListener;
import org.ethereum.manager.MainData;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.message.TransactionsMessage;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;


/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 12:28
 */
public class PeerTaster {

    PeerListener peerListener;
    Channel channel;

    public PeerTaster() {
    }

    public PeerTaster(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    public void connect(String host, int port){

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            final EthereumPeerTasterHandler handler = new EthereumPeerTasterHandler();

            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel ch) throws Exception {

                    ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(15));
                    ch.pipeline().addLast(new EthereumFrameDecoder());
                    ch.pipeline().addLast(handler);
                }
            });


            // Start the client.
//            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15);
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } catch (InterruptedException ie){
           System.out.println("-- ClientPeer: catch (InterruptedException ie) --");
           ie.printStackTrace();
        } finally {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("I am dead");
    }



    public static void main(String args[]){

        PeerTaster peerTaster = new PeerTaster();


        try {peerTaster.connect("54.211.14.10", 30303);} catch (Exception e) {e.printStackTrace();}
        try {peerTaster.connect("82.217.72.169", 30303);} catch (Exception e) {e.printStackTrace();}
        try {peerTaster.connect("54.201.28.117", 30303);} catch (Exception e) {e.printStackTrace();}
        try {peerTaster.connect("54.2.10.41", 30303);} catch (Exception e) {e.printStackTrace();}
        try {peerTaster.connect("0.204.10.41", 30303);} catch (Exception e) {e.printStackTrace();}
        try {peerTaster.connect("54.204.10.41", 30303);} catch (Exception e) {e.printStackTrace();}

        System.out.println("End of the roaad");

        for (PeerData peer : MainData.instance.getPeers()){

            System.out.println(peer.getInetAddress().getHostAddress().toString());
        };


    }
}
