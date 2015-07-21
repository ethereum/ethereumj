package org.ethereum.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.rlpx.FrameCodec;
import org.ethereum.net.rlpx.MessageCodesResolver;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.net.rlpx.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;

//import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
@Scope("prototype")
public class Channel {

    private final static Logger logger = LoggerFactory.getLogger("net");

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
    BzzHandler bzzHandler;

    @Autowired
    MessageCodec messageCodec;

    @Autowired
    NodeManager nodeManager;

    MessageCodesResolver messageCodesResolver = new MessageCodesResolver();
    
    InetSocketAddress inetSocketAddress;

    Node node;
    NodeStatistics nodeStatistics;

    private long startupTS;

    String remoteId;


    public Channel() {
    }

    public void init(String remoteId, boolean discoveryMode) {

        messageCodec.setRemoteId(remoteId, this);
        if (discoveryMode) {
            // temporary key/nodeId to not accidentally smear our reputation with
            // unexpected disconnect
            messageCodec.generateTempKey();
        }
        //messageCodec.setMsgQueue(msgQueue);

        p2pHandler.setMsgQueue(msgQueue);
        ethHandler.setMsgQueue(msgQueue);
        ethHandler.setPeerDiscoveryMode(discoveryMode);
        shhHandler.setMsgQueue(msgQueue);
        bzzHandler.setMsgQueue(msgQueue);

        ethHandler.setChannel(this);

        startupTS = System.currentTimeMillis();
    }

    public void publicRLPxHandshakeFinished(ChannelHandlerContext ctx, FrameCodec frameCodec, HelloMessage helloRemote, byte[] nodeId) throws IOException, InterruptedException {
        ctx.pipeline().addLast(Capability.P2P, p2pHandler);

        p2pHandler.setChannel(this);
        p2pHandler.setHandshake(helloRemote, ctx);

//        ctx.pipeline().addLast(Capability.ETH, getEthHandler());
//        ctx.pipeline().addLast(Capability.SHH, getShhHandler());
        getNodeStatistics().rlpxHandshake.add();
    }


    public void sendHelloMessage(ChannelHandlerContext ctx, FrameCodec frameCodec, String nodeId) throws IOException, InterruptedException {

        HelloMessage helloMessage = StaticMessages.createHelloMessage(nodeId);
        byte[] payload = helloMessage.getEncoded();

        ByteBuf byteBufMsg = ctx.alloc().buffer();
        frameCodec.writeFrame(new FrameCodec.Frame(helloMessage.getCode(), payload), byteBufMsg);
        ctx.writeAndFlush(byteBufMsg).sync();

        if (logger.isInfoEnabled())
            logger.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), helloMessage);
        getNodeStatistics().rlpxOutHello.add();
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

    public BzzHandler getBzzHandler() {
        return bzzHandler;
    }

    public MessageCodec getMessageCodec() {
        return messageCodec;
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

    public long getStartupTS() {
        return startupTS;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
        node = new Node(messageCodec.getRemoteId(),
                inetSocketAddress.getHostName(), inetSocketAddress.getPort());

    }

    public NodeStatistics getNodeStatistics() {
        if (nodeStatistics == null) {
            nodeStatistics = nodeManager.getNodeStatistics(node);
        }
        return nodeStatistics;
    }

    public MessageCodesResolver getMessageCodesResolver() {
        return messageCodesResolver;
    }


    public boolean hasInitPassed() {
        return ethHandler.hasInitPassed();
    }

    public boolean isUseful() {
        return ethHandler.hasStatusSucceeded();
    }
}
