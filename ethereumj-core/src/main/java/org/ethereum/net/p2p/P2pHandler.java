package org.ethereum.net.p2p;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.NewBlockMessage;
import org.ethereum.net.eth.message.TransactionsMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.rlpx.HandshakeHelper;
import org.ethereum.net.server.Channel;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.shh.ShhMessageCodes;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.net.swarm.bzz.BzzMessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.ethereum.net.eth.EthVersion.*;
import static org.ethereum.net.message.StaticMessages.*;

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
public class P2pHandler extends SimpleChannelInboundHandler<P2pMessage> {

    public final static byte VERSION = 4;

    private final static Logger logger = LoggerFactory.getLogger("net");

    private static ScheduledExecutorService pingTimer =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, "P2pPingTimer");
        }
    });

    private MessageQueue msgQueue;

    private boolean peerDiscoveryMode = false;

    private HelloMessage handshakeHelloMessage = null;
    private Set<PeerInfo> lastPeersSent;

    @Autowired
    WorldManager worldManager;
    private Channel channel;
    private ScheduledFuture<?> pingTask;

    public P2pHandler() {

        this.peerDiscoveryMode = false;
    }

    public P2pHandler(MessageQueue msgQueue, boolean peerDiscoveryMode) {
        this.msgQueue = msgQueue;
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("P2P protocol activated");
        msgQueue.activate(ctx);
        worldManager.getListener().trace("P2P protocol activated");
        startTimers();
    }


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, P2pMessage msg) throws InterruptedException {

        if (P2pMessageCodes.inRange(msg.getCommand().asByte()))
            logger.trace("P2PHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("P2PHandler invoke: [%s]", msg.getCommand()));

        switch (msg.getCommand()) {
            case HELLO:
                msgQueue.receivedMessage(msg);
                setHandshake((HelloMessage) msg, ctx);
//                sendGetPeers();
                break;
            case DISCONNECT:
                msgQueue.receivedMessage(msg);
                channel.getNodeStatistics().nodeDisconnectedRemote(((DisconnectMessage) msg).getReason());
                break;
            case PING:
                msgQueue.receivedMessage(msg);
                ctx.writeAndFlush(PONG_MESSAGE);
                break;
            case PONG:
                msgQueue.receivedMessage(msg);
                break;
            case GET_PEERS:
                msgQueue.receivedMessage(msg);
                sendPeers(); // todo: implement session management for peer request
                break;
            case PEERS:
                msgQueue.receivedMessage(msg);
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

    private void disconnect(ReasonCode reasonCode) {
        msgQueue.sendMessage(new DisconnectMessage(reasonCode));
        channel.getNodeStatistics().nodeDisconnectedLocal(reasonCode);
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

    private void processPeers(ChannelHandlerContext ctx, PeersMessage peersMessage) {
        worldManager.getPeerDiscovery().addPeers(peersMessage.getPeers());
    }

    private void sendGetPeers() {
        msgQueue.sendMessage(StaticMessages.GET_PEERS_MESSAGE);
    }

    private void sendPeers() {

        Set<PeerInfo> peers = worldManager.getPeerDiscovery().getPeers();

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
        msgQueue.sendMessage(msg);
    }



    public void setHandshake(HelloMessage msg, ChannelHandlerContext ctx) {

        channel.getNodeStatistics().setClientId(msg.getClientId());

        this.handshakeHelloMessage = msg;
        if (msg.getP2PVersion() != VERSION) {
            disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
        }
        else {
            List<Capability> capInCommon = HandshakeHelper.getSupportedCapabilities(msg);
            channel.initMessageCodes(capInCommon);
            for (Capability capability : capInCommon) {
                if (capability.getName().equals(Capability.ETH) &&
                    EthVersion.isSupported(capability.getVersion())) {

                    // Activate EthHandler for this peer
                    channel.activateEth(ctx, fromCode(capability.getVersion()));
                } else if
                   (capability.getName().equals(Capability.SHH) &&
                    capability.getVersion() == ShhHandler.VERSION) {

                    // Activate ShhHandler for this peer
                    channel.activateShh(ctx);
                } else if
                   (capability.getName().equals(Capability.BZZ) &&
                    capability.getVersion() == BzzHandler.VERSION) {

                    // Activate ShhHandler for this peer
                    channel.activateBzz(ctx);
                }
            }

            InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
            int port = msg.getListenPort();
            PeerInfo confirmedPeer = new PeerInfo(address, port, msg.getPeerId());
            confirmedPeer.setOnline(false);
            confirmedPeer.getCapabilities().addAll(msg.getCapabilities());

            //todo calculate the Offsets
            worldManager.getPeerDiscovery().getPeers().add(confirmedPeer);
            worldManager.getListener().onHandShakePeer(channel.getNode(), msg);

        }
    }

    /**
     * submit transaction to the network
     *
     * @param tx - fresh transaction object
     */
    public void sendTransaction(Transaction tx) {

        TransactionsMessage msg = new TransactionsMessage(tx);
        msgQueue.sendMessage(msg);
    }

    public void sendNewBlock(Block block) {

        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        msgQueue.sendMessage(msg);
    }

    public void sendDisconnect() {
        msgQueue.disconnect();
    }

    public void adaptMessageIds(List<Capability> capabilities) {

        Collections.sort(capabilities);
        int offset = P2pMessageCodes.USER.asByte() + 1;

        for (Capability capability : capabilities) {

            if (capability.getName().equals(Capability.ETH)) {
                EthMessageCodes.setOffset((byte)offset);
                offset += EthMessageCodes.values().length;
            }

            if (capability.getName().equals(Capability.SHH)) {
                ShhMessageCodes.setOffset((byte)offset);
                offset += ShhMessageCodes.values().length;
            }

            if (capability.getName().equals(Capability.BZZ)) {
                BzzMessageCodes.setOffset((byte) offset);
                offset += BzzMessageCodes.values().length + 4;
                // FIXME: for some reason Go left 4 codes between BZZ and ETH message codes
            }
        }
    }

    public HelloMessage getHandshakeHelloMessage() {
        return handshakeHelloMessage;
    }

    private void startTimers() {
        // sample for pinging in background
        pingTask = pingTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                msgQueue.sendMessage(PING_MESSAGE);
            }
        }, 2, 5, TimeUnit.SECONDS);
    }

    public void killTimers() {
        pingTask.cancel(false);
        msgQueue.close();
    }


    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}