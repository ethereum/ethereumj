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
            this.channel = f.channel();
            MainData.instance.setActivePeer(this);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } catch (InterruptedException ie){
           System.out.println("-- ClientPeer: catch (InterruptedException ie) --");
           ie.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }


    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     * todo: find a way to set all "send to wire methods" in one place.
     */
    public void sendTransaction(Transaction transaction){

        transaction.getEncoded();
        java.util.List<Transaction> txList =  new ArrayList<Transaction>();
        txList.add(transaction);
        TransactionsMessage transactionsMessage = new TransactionsMessage(txList);

        byte[] payload = transactionsMessage.getPayload();

        ByteBuf buffer = channel.alloc().buffer(payload.length + 8);
        buffer.writeBytes(StaticMessages.MAGIC_PACKET);
        buffer.writeBytes(Utils.calcPacketSize(payload));
        buffer.writeBytes(payload);

        System.out.println("Send msg: [ " +
                Hex.toHexString(payload) +
                " ]");

        channel.writeAndFlush(buffer);
    }
}
