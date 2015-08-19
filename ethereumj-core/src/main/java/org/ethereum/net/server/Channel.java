package org.ethereum.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.Eth;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.rlpx.FrameCodec;
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
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
@Scope("prototype")
public class Channel {

    private final static Logger logger = LoggerFactory.getLogger("net");

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

    private InetSocketAddress inetSocketAddress;

    private Node node;
    private NodeStatistics nodeStatistics;

    private boolean discoveryMode;

    public void init(ChannelPipeline pipeline, String remoteId, boolean discoveryMode) {

        pipeline.addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
        pipeline.addLast("initiator", messageCodec.getInitiator());
        pipeline.addLast("messageCodec", messageCodec);

        this.discoveryMode = discoveryMode;

        if (discoveryMode) {
            // temporary key/nodeId to not accidentally smear our reputation with
            // unexpected disconnect
            messageCodec.generateTempKey();
        }

        messageCodec.setRemoteId(remoteId, this);

        p2pHandler.setMsgQueue(msgQueue);

        ethHandler.setMsgQueue(msgQueue);
        ethHandler.setChannel(this);
        ethHandler.setPeerDiscoveryMode(discoveryMode);

        shhHandler.setMsgQueue(msgQueue);
        shhHandler.setPrivKey(ECKey.fromPrivate(CONFIG.privateKey().getBytes()).decompress());

        bzzHandler.setMsgQueue(msgQueue);
    }

    public void publicRLPxHandshakeFinished(ChannelHandlerContext ctx, HelloMessage helloRemote) throws IOException, InterruptedException {
        ctx.pipeline().addLast(Capability.P2P, p2pHandler);

        p2pHandler.setChannel(this);
        p2pHandler.setHandshake(helloRemote, ctx);

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

    public void activateEth(ChannelHandlerContext ctx) {
        ethHandler.setPeerId(node.getHexId());
        ctx.pipeline().addLast(Capability.ETH, ethHandler);
        ethHandler.activate();
    }

    public void activateShh(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(Capability.SHH, shhHandler);
        shhHandler.activate();
    }

    public void activateBzz(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(Capability.BZZ, bzzHandler);
        bzzHandler.activate();
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public NodeStatistics getNodeStatistics() {
        return nodeStatistics;
    }

    public void setNode(byte[] nodeId) {
        node = new Node(nodeId, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        nodeStatistics = nodeManager.getNodeStatistics(node);
    }

    public Node getNode() {
        return node;
    }

    public void initMessageCodes(List<Capability> caps) {
        messageCodec.initMessageCodes(caps);
    }

    public boolean isProtocolsInitiated() {
        return ethHandler.hasInitPassed();
    }

    public boolean isUseful() {
        return ethHandler.hasStatusSucceeded();
    }

    public void onDisconnect() {
        ethHandler.onDisconnect();
    }

    public boolean isDiscoveryMode() {
        return discoveryMode;
    }

    public String getPeerId() {
        return node.getHexId();
    }

    public String getPeerIdShort() {
        return node.getHexIdShort();
    }

    // ETH sub protocol

    public void logSyncStats() {
        ethHandler.logSyncStats();
    }

    public BigInteger getTotalDifficulty() {
        return nodeStatistics.getEthTotalDifficulty();
    }

    public void changeSyncState(SyncStateName newState) {
        ethHandler.changeState(newState);
    }

    public boolean hasBlocksLack() {
        return ethHandler.hasBlocksLack();
    }

    public void setMaxHashesAsk(int maxHashesAsk) {
        ethHandler.setMaxHashesAsk(maxHashesAsk);
    }

    public int getMaxHashesAsk() {
        return ethHandler.getMaxHashesAsk();
    }

    public byte[] getBestHash() {
        return ethHandler.getBestHash();
    }

    public Eth.SyncStats getSyncStats() {
        return ethHandler.getStats();
    }

    public boolean isHashRetrievingDone() {
        return ethHandler.isHashRetrievingDone();
    }

    public boolean isHashRetrieving() {
        return ethHandler.isHashRetrieving();
    }

    public boolean isIdle() {
        return ethHandler.isIdle();
    }

    public void sendTransaction(Transaction tx) {
        ethHandler.sendTransaction(tx);
    }
}
