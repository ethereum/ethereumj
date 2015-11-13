package org.ethereum.net;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.message.EthMessage;
import org.ethereum.net.eth.message.NewBlockMessage;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.rlpx.AuthInitiateMessage;
import org.ethereum.net.rlpx.MessageCodec;
import org.ethereum.net.rlpx.MessageCodecListener;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;


/**
 * Common channel functionality
 *
 * @author Tiberius Iliescu
 */
@Component
@Scope("prototype")
public class ChannelBase implements MessageCodecListener, ProtocolHandlerListener {

    protected final static Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    protected NodeManager nodeManager;

    @Autowired
    protected P2pHandler p2pHandler;

    @Autowired
    protected MessageCodec messageCodec;

    @Autowired
    protected EthereumListener ethereumListener;

    protected InetSocketAddress inetSocketAddress;

    protected NioSocketChannel channel;

    protected boolean discoveryMode = false;

    protected byte[] myId;
    protected ECKey myKey;

    protected byte[] remoteId;
    protected Node node;
    protected NodeStatistics nodeStatistics;

    protected final InitiateHandler initiator = new InitiateHandler();

    public class InitiateHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            try {
                initiate(ctx);
            } catch (Exception e) {
                logger.error("Exception initiating channel: {}", e);
                throw e;
            }
        }
    }


    public ChannelBase() {
        setMyKey(CONFIG.getMyKey());
        messageCodec.setListener(this);
    }

    protected void initiate(ChannelHandlerContext ctx) throws Exception {

        inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        setRemoteId(remoteId);
        messageCodec.sendInitiateMessage(ctx, myKey, getRemotePublic());
        nodeStatistics.rlpxAuthMessagesSent.add();
    }

    public void init(NioSocketChannel channel, String remoteId, boolean discoveryMode) {

        this.channel = channel;
        this.discoveryMode = discoveryMode;

        if (discoveryMode) {
            // temporary key/nodeId to not accidentally smear our reputation with
            // unexpected disconnect
            setMyKey(new ECKey().decompress());
        }
        this.remoteId = Hex.decode(remoteId);
        config();
        channel.pipeline().addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
        channel.pipeline().addLast("initiator", initiator);
        channel.pipeline().addLast("messageCodec", messageCodec);
    }

    public void config() {

    }

    public void setMyKey(ECKey key) {

        myKey = key;
        myId = myKey.getNodeId();
        refreshHelloMessage();
    }

    protected void refreshHelloMessage() {

        HelloMessage helloMessage = discoveryMode ? StaticMessages.createHelloMessage(Hex.toHexString(myId), 9) :
                StaticMessages.createHelloMessage(Hex.toHexString(myId));
        messageCodec.setHelloMessage(helloMessage);
    }

    public void setRemoteId(byte[] nodeId) {
        remoteId = nodeId;
        node = new Node(nodeId, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        nodeStatistics = nodeManager.getNodeStatistics(node);
    }

    protected ECPoint getRemotePublic() {

        ECPoint remotePublic = null;
        if (remoteId.length == 64) {
            logger.info("RLPX protocol activated");
            byte[] remotePublicBytes = new byte[remoteId.length + 1];
            System.arraycopy(remoteId, 0, remotePublicBytes, 1, remoteId.length);
            remotePublicBytes[0] = 0x04; // uncompressed
            remotePublic = ECKey.fromPublicOnly(remotePublicBytes).getPubKeyPoint();
        }
        return remotePublic;
    }

    public NodeStatistics getNodeStatistics() {
        return nodeStatistics;
    }

    public void disconnect(ReasonCode reason) {
        p2pHandler.disconnect(reason);
    }

    public Node getNode() {
        return node;
    }

    public String getPeerId() {
        return node == null ? "<null>" : node.getHexId();
    }

    public String getPeerIdShort() {
        return node == null ? "<null>" : node.getHexIdShort();
    }

    public byte[] getNodeId() {
        return node == null ? null : node.getId();
    }

    public ByteArrayWrapper getNodeIdWrapper() {
        return node == null ? null : new ByteArrayWrapper(node.getId());
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void onSyncDone() {
        p2pHandler.onSyncDone();
    }

    public void onDisconnect() {
    }

    // MessageCodecListener

    @Override
    public void onMessageDecoded(Message message) {

        if (logger.isInfoEnabled())
            logger.info("From: \t{} \tRecv: \t{}", channel, message);
        ethereumListener.onRecvMessage(message);
        nodeStatistics.rlpxInMessages.add();
    }

    @Override
    public void onMessageEncoded(Message message) {

        if (logger.isInfoEnabled())
            logger.info("To: \t{} \tSend: \t{}", channel, message);
        nodeStatistics.rlpxOutMessages.add();
    }

    @Override
    public void onRLPxHandshakeFinished(ChannelHandlerContext ctx, HelloMessage helloMessage) {

        nodeStatistics.rlpxHandshake.add();
        nodeStatistics.setClientId(helloMessage.getClientId());

        if (helloMessage.getP2PVersion() != P2pHandler.VERSION) {
            p2pHandler.disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
        } else {
            p2pHandler.addListener(this);
            messageCodec.addProtocol(p2pHandler);
            p2pHandler.setNode(getNode());
            p2pHandler.activate(Capability.P2P);
            channel.pipeline().addLast(Capability.P2P, p2pHandler);
            p2pHandler.setHandshake(ctx, helloMessage);
            ethereumListener.onHandShakePeer(node, helloMessage);
        }
    }

    @Override
    public void onRLPxDisconnect(DisconnectMessage message) {
        if (logger.isInfoEnabled())
            logger.info("From: \t{} \tRecv: \t{}", channel, message);
        nodeStatistics.nodeDisconnectedRemote(message.getReason());
    }

    @Override
    public void onMessageCodecException(ChannelHandlerContext ctx, Throwable cause) {
        if (discoveryMode) {
            logger.debug("MessageCodec handling failed", cause);
        } else {
            logger.error("MessageCodec handling failed", cause);
        }
    }

    @Override
    public void onHelloMessageSent(HelloMessage message) {

        nodeStatistics.rlpxOutHello.add();
    }

    @Override
    public void onHelloMessageReceived(HelloMessage message) {

        nodeStatistics.rlpxInHello.add();
    }

    @Override
    public void onAuthentificationRequest(AuthInitiateMessage initiateMessage, ECPoint remotePubKey) {

        byte[] compressed = remotePubKey.getEncoded();
        byte[] remoteId = new byte[compressed.length - 1];
        System.arraycopy(compressed, 1, remoteId, 0, remoteId.length);
        setRemoteId(remoteId);
    }

    // ProtocolHandlerListener

    @Override
    public void onRemoteDisconnect(String protocol, Message message) {

        DisconnectMessage disconnectMessage = (DisconnectMessage)message;
        nodeStatistics.nodeDisconnectedRemote(disconnectMessage.getReason());

        if (!logger.isInfoEnabled() || disconnectMessage.getReason() != ReasonCode.USELESS_PEER) {
            return;
        }
    }

    @Override
    public void onLocalDisconnect(String protocol, ReasonCode reason) {
        nodeStatistics.nodeDisconnectedLocal(reason);
    }

    @Override
    public void onProtocolActivated(String protocolName, ProtocolHandler protocolHandler) {
        if (protocolName != Capability.P2P) {
            messageCodec.addProtocol(protocolHandler);
        }
    }

    @Override
    public void onProtocolMessageReceived(String protocolName, Message message) {

        switch(protocolName) {
            case Capability.ETH:
                nodeStatistics.ethInbound.add();
                EthMessage ethMessage = (EthMessage)message;
                switch (ethMessage.getCommand()) {
                    case STATUS:
                        nodeStatistics.ethHandshake((StatusMessage)ethMessage);
                        ethereumListener.onEthStatusUpdated(getNode(), (StatusMessage)ethMessage);
                        break;
                    case NEW_BLOCK:
                        nodeStatistics.setEthTotalDifficulty(((NewBlockMessage)message).getDifficultyAsBigInt());
                        break;
                }
                break;
        }
    }

    @Override
    public void onProtocolMessageSent(String protocolName, Message message) {
        switch(protocolName) {
            case Capability.ETH:
                nodeStatistics.ethOutbound.add();
        }
    }

    //

    @Override
    public int hashCode() {

        int result = inetSocketAddress != null ? inetSocketAddress.hashCode() : 0;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", getPeerIdShort(), inetSocketAddress);
    }
}
