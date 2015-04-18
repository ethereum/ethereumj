package org.ethereum.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.rlpx.FrameCodec;
import org.ethereum.net.rlpx.RLPXHandler;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.wire.MessageDecoder;
import org.ethereum.net.wire.MessageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;

import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
@Scope("prototype")
public class Channel {

    @Autowired
    ChannelManager channelManager;

    @Autowired
    MessageQueue msgQueue;

    @Autowired
    P2pHandler p2pHandler;

    @Autowired
    EthHandler ethHandler;

    @Autowired
    ShhHandler shhHandler;

    @Autowired
    RLPXHandler rlpxHandler;


    @Autowired
    MessageDecoder messageDecoder;

    @Autowired
    MessageEncoder messageEncoder;

    InetSocketAddress inetSocketAddress;


    private long startupTS;


    public Channel() {
    }

    public void init(String remoteId) {

        rlpxHandler.setRemoteId(remoteId, this);
        rlpxHandler.setMsgQueue(msgQueue);

        p2pHandler.setMsgQueue(msgQueue);
        ethHandler.setMsgQueue(msgQueue);
        shhHandler.setMsgQueue(msgQueue);

        startupTS = System.currentTimeMillis();
    }

    public void publicRLPxHandshakeFinished(ChannelHandlerContext ctx, FrameCodec frameCodec, HelloMessage helloRemote, byte[] nodeId) throws IOException, InterruptedException {


        messageDecoder.setFrameCodec(frameCodec);
        messageEncoder.setFrameCodec(frameCodec);

        ctx.pipeline().addLast("in  encoder", messageDecoder);
        ctx.pipeline().addLast("out encoder", messageEncoder);
        ctx.pipeline().addLast(Capability.P2P, p2pHandler);


        p2pHandler.setChannel(this);
        p2pHandler.setHandshake(helloRemote, ctx);

//        ctx.pipeline().addLast(Capability.ETH, getEthHandler());
//        ctx.pipeline().addLast(Capability.SHH, getShhHandler());
    }


    public void sendHelloMessage(ChannelHandlerContext ctx, FrameCodec frameCodec, String nodeId) throws IOException, InterruptedException {

        HELLO_MESSAGE.setPeerId(nodeId);
        byte[] payload = HELLO_MESSAGE.getEncoded();

        ByteBuf byteBufMsg = ctx.alloc().buffer();
        frameCodec.writeFrame(new FrameCodec.Frame(HELLO_MESSAGE.getCode(), payload), byteBufMsg);
        ctx.writeAndFlush(byteBufMsg).sync();
    }



    public P2pHandler getP2pHandler() {
        return p2pHandler;
    }

    public EthHandler getEthHandler() {
        return ethHandler;
    }

    public ShhHandler getShhHandler() {
        return shhHandler;
    }

    public RLPXHandler getRlpxHandler() {
        return rlpxHandler;
    }

    public MessageDecoder getMessageDecoder() {
        return messageDecoder;
    }

    public MessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    public void sendTransaction(Transaction tx) {
        ethHandler.sendTransaction(tx);
    }

    public void sendNewBlock(Block block) {

        // 1. check by best block send or not to send
        ethHandler.sendNewBlock(block);

    }

    public HelloMessage getHandshakeHelloMessage() {
        return getP2pHandler().getHandshakeHelloMessage();
    }


    public boolean isSync() {
        return ethHandler.getSyncStatus() == EthHandler.SyncStatus.SYNC_DONE;
    }


    public BigInteger getTotalDifficulty() {
        return ethHandler.getTotalDifficulty();
    }

    public void ethSync() {
        ethHandler.doSync();
    }

    public long getStartupTS() {
        return startupTS;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }
}
