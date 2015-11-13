package org.ethereum.net.p2p;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.ProtocolHandler;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.handler.Eth;
import org.ethereum.net.eth.handler.EthAdapter;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.handler.EthHandlerFactory;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.rlpx.HandshakeHelper;

import io.netty.channel.ChannelHandlerContext;

import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.ethereum.net.message.StaticMessages.*;
import static org.ethereum.net.eth.EthVersion.fromCode;

/**
 * Process the basic protocol messages between every peer on the network.
 *
 * Peers can send/receive
 * <ul>
 *  <li>HELLO       :   Announce themselves to the network</li>
 *  <li>DISCONNECT  :   Disconnect themselves from the network</li>
 *  <li>GET_PEERS   :   Request a list of other knows peers</li>
 *  <li>PEERS       :   Send a list of known peers</li>
 *  <li>PING        :   Check if another peer is still alive</li>
 *  <li>PONG        :   Confirm that they themselves are still alive</li>
 * </ul>
 */
@Component
@Scope("prototype")
public class P2pHandler extends ProtocolHandler<P2pMessage> {

    public final static byte VERSION = 4;

    private final static Logger logger = LoggerFactory.getLogger("net");

    private static ScheduledExecutorService pingTimer =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, "P2pPingTimer");
        }
    });

    private boolean peerDiscoveryMode = false;

    private HelloMessage handshakeHelloMessage = null;
    private Set<PeerInfo> lastPeersSent;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private ShhHandler shhHandler;

    @Autowired
    private BzzHandler bzzHandler;

    @Autowired
    private EthHandlerFactory ethHandlerFactory;

    @Autowired
    private PeerDiscovery peerDiscovery;

    private ScheduledFuture<?> pingTask;

    public P2pHandler() {

        this.peerDiscoveryMode = false;
    }

    public P2pHandler(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        try {
            logger.info("P2P protocol activated");
            messageQueue.activate(ctx);
            ethereumListener.trace("P2P protocol activated");
            startTimers();
        } catch (Exception e) {
            logger.error("P2p handler adding failed: {}", e);
            throw e;
        }
    }


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, P2pMessage msg) throws InterruptedException {

        if (P2pMessageCodes.inRange(msg.getCommand().asByte()))
            logger.trace("P2PHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("P2PHandler invoke: [%s]", msg.getCommand()));

        switch (msg.getCommand()) {
            case HELLO:
                messageQueue.receivedMessage(msg);
                setHandshake(ctx, (HelloMessage) msg);
//                sendGetPeers();
                break;
            case DISCONNECT:
                messageQueue.receivedMessage(msg);
                onRemoteDisconnect(msg);
                break;
            case PING:
                messageQueue.receivedMessage(msg);
                ctx.writeAndFlush(PONG_MESSAGE);
                break;
            case PONG:
                messageQueue.receivedMessage(msg);
                break;
            case GET_PEERS:
                messageQueue.receivedMessage(msg);
                sendPeers(); // todo: implement session management for peer request
                break;
            case PEERS:
                messageQueue.receivedMessage(msg);
                processPeers(ctx, (PeersMessage) msg);

                if (peerDiscoveryMode ||
                        !handshakeHelloMessage.getCapabilities().contains(Capability.ETH)) {
                    disconnect(ReasonCode.REQUESTED);
                    killTimers();
                    ctx.close().sync();
                    ctx.disconnect().sync();
                }
                break;
            default:
                ctx.fireChannelRead(msg);
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel inactive: ", ctx.toString());
        this.killTimers();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("P2p handling failed", cause);
        ctx.close();
        killTimers();
    }

    @Override
    public boolean hasCommand(Enum msgCommand) {

        return msgCommand instanceof P2pMessageCodes;
    }

    @Override
    public byte getCommandCode(Enum msgCommand) {

        return ((P2pMessageCodes) msgCommand).asByte();
    }

    @Override
    public boolean hasCommandCode(byte code) {

        return P2pMessageCodes.inRange(code);
    }

    @Override
    public byte getMaxCommandCode() {

        return (byte)P2pMessageCodes.max();
    }

    @Override
    public void activate(String name) {
        super.activate(name);
        messageFactory = new P2pMessageFactory();
    }

    public void setHandshake(ChannelHandlerContext ctx, HelloMessage msg) {

        this.handshakeHelloMessage = msg;
        List<Capability> capInCommon = HandshakeHelper.getSupportedCapabilities(msg);
        for (Capability capability : capInCommon) {
            if (capability.getName().equals(Capability.ETH)) {

                // Activate EthHandler for this peer
                EthVersion version = fromCode(capability.getVersion());
                EthHandler handler = ethHandlerFactory.create(version);
                handler.setPeerDiscoveryMode(peerDiscoveryMode);
                activateSubProtocol(ctx, Capability.ETH, handler);
            } else if
                    (capability.getName().equals(Capability.SHH) &&
                            capability.getVersion() == ShhHandler.VERSION) {

                // Activate ShhHandler for this peer
                activateSubProtocol(ctx, Capability.SHH, shhHandler);
            } else if
                    (capability.getName().equals(Capability.BZZ) &&
                            capability.getVersion() == BzzHandler.VERSION) {

                // Activate ShhHandler for this peer
                activateSubProtocol(ctx, Capability.BZZ, bzzHandler);
            }
        }

        InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
        int port = msg.getListenPort();
        PeerInfo confirmedPeer = new PeerInfo(address, port, msg.getPeerId());
        confirmedPeer.setOnline(false);
        confirmedPeer.getCapabilities().addAll(msg.getCapabilities());

        //todo calculate the Offsets
        peerDiscovery.getPeers().add(confirmedPeer);

    }

    public Eth getEth() {
        Eth handler = (Eth)getSubProtocol(Capability.ETH);
        if (handler == null) {
            handler = new EthAdapter();
        }
        return handler ;
    }

    public EthHandler getEthHandler() {
        return (EthHandler)getSubProtocol(Capability.ETH);
    }

    private void processPeers(ChannelHandlerContext ctx, PeersMessage peersMessage) {
        peerDiscovery.addPeers(peersMessage.getPeers());
    }

    private void sendGetPeers() {
        messageQueue.sendMessage(StaticMessages.GET_PEERS_MESSAGE);
    }

    private void sendPeers() {

        Set<PeerInfo> peers = peerDiscovery.getPeers();

        if (lastPeersSent != null && peers.equals(lastPeersSent)) {
            logger.info("No new peers discovered don't answer for GetPeers");
            return;
        }

        Set<Peer> peerSet = new HashSet<>();
        for (PeerInfo peer : peers) {
            new Peer(peer.getAddress(), peer.getPort(), peer.getPeerId());
        }

        PeersMessage msg = new PeersMessage(peerSet);
        lastPeersSent = peers;
        messageQueue.sendMessage(msg);
    }

    /**
     * submit transaction to the network
     *
     * @param tx - fresh transaction object
     */
    public void sendTransaction(Transaction tx) {

        TransactionsMessage msg = new TransactionsMessage(tx);
        messageQueue.sendMessage(msg);
    }

    public void sendNewBlock(Block block) {

        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        messageQueue.sendMessage(msg);
    }

    public HelloMessage getHandshakeHelloMessage() {
        return handshakeHelloMessage;
    }

    public void disconnect(ReasonCode reason) {
        messageQueue.disconnect(reason);
        onLocalDisconnect(reason);
    }

    private void startTimers() {
        // sample for pinging in background
        pingTask = pingTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                messageQueue.sendMessage(PING_MESSAGE);
            }
        }, 2, 5, TimeUnit.SECONDS);
    }

    public void killTimers() {
        pingTask.cancel(false);
        messageQueue.close();
    }

    public void onSyncDone() {
        getEth().enableTransactions();
        getEth().onSyncDone();
    }
}