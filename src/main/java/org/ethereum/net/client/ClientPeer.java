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
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 12:28
 */
public class ClientPeer {

    Logger logger = LoggerFactory.getLogger("wire");


    PeerListener peerListener;
    Channel channel;

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

                    ch.pipeline().addLast("readTimeoutHandler",
                            new ReadTimeoutHandler(CONFIG.activePeerChannelTimeout(), TimeUnit.SECONDS));
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

        if (peerListener != null)
         peerListener.console("Send msg: [ " +
                 Hex.toHexString(payload) +
                 " ]");

        ByteBuf buffer = channel.alloc().buffer(payload.length + 8);
        buffer.writeBytes(StaticMessages.MAGIC_PACKET);
        buffer.writeBytes(ByteUtil.calcPacketSize(payload));
        buffer.writeBytes(payload);

        logger.info("Send msg: [ " +
                Hex.toHexString(payload) +
                " ]");

        channel.writeAndFlush(buffer);
    }
}
