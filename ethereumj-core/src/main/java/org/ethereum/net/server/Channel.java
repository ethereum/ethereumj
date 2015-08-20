package org.ethereum.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.handler.EthHandlerFactory;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.eth.sync.SyncStatistics;
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
    private MessageQueue msgQueue;

    @Autowired
    private P2pHandler p2pHandler;

    @Autowired
    private ShhHandler shhHandler;

    @Autowired
    private BzzHandler bzzHandler;

    @Autowired
    private MessageCodec messageCodec;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private EthHandlerFactory ethHandlerFactory;

    private EthHandler ethHandler;

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

    public void activateEth(ChannelHandlerContext ctx, EthVersion version) {
        ethHandler = ethHandlerFactory.create(version);

        logger.info("Peer [{} | {}]: Use Eth {}", inetSocketAddress, getPeerIdShort(), ethHandler.getVersion());

        ctx.pipeline().addLast(Capability.ETH, ethHandler);

        ethHandler.setMsgQueue(msgQueue);
        ethHandler.setChannel(this);
        ethHandler.setPeerDiscoveryMode(discoveryMode);

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
        return ethHandler != null && ethHandler.hasInitPassed();
    }

    public boolean isUseful() {
        return ethHandler != null && ethHandler.hasStatusSucceeded();
    }

    public void onDisconnect() {
        if (ethHandler != null) {
            ethHandler.onShutdown();
        }
    }

    public void onSyncDone() {
        if (ethHandler != null) {
            ethHandler.onSyncDone();
        }
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

    public boolean hasEthStatusSucceeded() {
        return ethHandler != null && ethHandler.hasStatusSucceeded();
    }

    public void logSyncStats() {
        if (ethHandler != null) {
            ethHandler.logSyncStats();
        }
    }

    public BigInteger getTotalDifficulty() {
        return nodeStatistics.getEthTotalDifficulty();
    }

    public void changeSyncState(SyncStateName newState) {
        if (ethHandler != null) {
            ethHandler.changeState(newState);
        }
    }

    public boolean hasBlocksLack() {
        return ethHandler != null && ethHandler.hasBlocksLack();
    }

    public void setMaxHashesAsk(int maxHashesAsk) {
        if (ethHandler != null) {
            ethHandler.setMaxHashesAsk(maxHashesAsk);
        }
    }

    public int getMaxHashesAsk() {
        if (ethHandler == null) {
            return 0;
        }

        return ethHandler.getMaxHashesAsk();
    }

    public byte[] getBestHash() {
        if (ethHandler == null) {
            return new byte[0];
        }

        return ethHandler.getBestHash();
    }

    public SyncStatistics getSyncStats() {
        if (ethHandler == null) {
            return new SyncStatistics();
        }

        return ethHandler.getStats();
    }

    public boolean isHashRetrievingDone() {
        return ethHandler != null && ethHandler.isHashRetrievingDone();
    }

    public boolean isHashRetrieving() {
        return ethHandler != null && ethHandler.isHashRetrieving();
    }

    public boolean isIdle() {
        return ethHandler == null || ethHandler.isIdle();
    }

    public void prohibitTransactionProcessing() {
        if (ethHandler != null) {
            ethHandler.prohibitTransactionProcessing();
        }
    }

    public void sendTransaction(Transaction tx) {
        if (ethHandler != null) {
            ethHandler.sendTransaction(tx);
        }
    }
}
