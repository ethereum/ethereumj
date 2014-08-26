package org.ethereum.net.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.EthereumMessageSizeEstimator;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.TransactionsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 12:28
 */
public class ClientPeer {

    private Logger logger = LoggerFactory.getLogger("wire");

    private PeerListener peerListener;
    private EthereumProtocolHandler handler;

    public ClientPeer() {
    }

    public ClientPeer(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    public void connect(String host, int port) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, new EthereumMessageSizeEstimator());

            if (peerListener != null) {
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
            WorldManager.getInstance().setActivePeer(this);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();


        } catch (InterruptedException ie) {
           logger.error("-- ClientPeer: catch (InterruptedException ie) --", ie);
        } finally {
            workerGroup.shutdownGracefully();

            handler.killTimers();

            final Collection<PeerData> peers =  WorldManager.getInstance().getPeers();

            for (PeerData peer : peers){
                if (host.equals(peer.getInetAddress().getHostAddress()) &&
                        port == peer.getPort()){
                    peer.setOnline(false);
                }
            }

            EthereumListener listener = WorldManager.getInstance().getListener();
            if (listener != null){
                listener.onPeerDisconnect(host, port);
            }

        }
    }

    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     */
    public void sendTransaction(Transaction transaction) {

        transaction.getEncoded();
        java.util.List<Transaction> txList =  new ArrayList<Transaction>();
        txList.add(transaction);
        TransactionsMessage transactionsMessage = new TransactionsMessage(txList);


        byte[] payload = transactionsMessage.getPayload();

        if (peerListener != null)
         peerListener.console("Send msg: [ " +
                 Hex.toHexString(payload) +
                 " ]");

        handler.sendMsg(transactionsMessage);
    }
}
