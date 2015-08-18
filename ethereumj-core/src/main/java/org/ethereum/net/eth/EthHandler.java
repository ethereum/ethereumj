package org.ethereum.net.eth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.EthVersion.*;

/**
 * Process the messages between peers with 'eth' capability on the network<br>
 * Message handling is delegated to {@link Eth} class and its children,
 * except {@code STATUS} message
 *
 * <p>
 * Peers with 'eth' capability can send/receive:
 * <ul>
 * <li>STATUS               :   Announce their status to the peer</li>
 * <li>GET_TRANSACTIONS     :   Request a list of pending transactions</li>
 * <li>TRANSACTIONS         :   Send a list of pending transactions</li>
 * <li>GET_BLOCK_HASHES     :   Request a list of known block hashes</li>
 * <li>BLOCK_HASHES         :   Send a list of known block hashes</li>
 * <li>GET_BLOCKS           :   Request a list of blocks</li>
 * <li>BLOCKS               :   Send a list of blocks</li>
 * </ul>
 */
@Component
@Scope("prototype")
public class EthHandler extends SimpleChannelInboundHandler<EthMessage> {

    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    public final static byte VERSION = V60.getCode();

    private Eth eth;

    private MessageQueue msgQueue = null;

    private String peerId;
    private EthState state = EthState.INIT;

    private boolean peerDiscoveryMode = false;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private ApplicationContext applicationContext;

    private Channel channel;

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte()))
            loggerNet.trace("EthHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        channel.getNodeStatistics().ethInbound.add();

        switch (msg.getCommand()) {
            case STATUS:
                msgQueue.receivedMessage(msg);
                processStatus((StatusMessage) msg, ctx);
                break;
            case GET_TRANSACTIONS:
                // todo: eventually get_transaction is going deprecated
                break;
            case TRANSACTIONS:
                msgQueue.receivedMessage(msg);
                eth.processTransactions((TransactionsMessage) msg);
                break;
            case GET_BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                eth.processGetBlockHashes((GetBlockHashesMessage) msg);
                break;
            case BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                eth.processBlockHashes((BlockHashesMessage) msg);
                break;
            case GET_BLOCKS:
                msgQueue.receivedMessage(msg);
                eth.processGetBlocks((GetBlocksMessage) msg);
                break;
            case BLOCKS:
                msgQueue.receivedMessage(msg);
                eth.processBlocks((BlocksMessage) msg);
                break;
            case NEW_BLOCK:
                msgQueue.receivedMessage(msg);
                eth.processNewBlock((NewBlockMessage) msg);
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        loggerNet.error("Eth handling failed", cause);
        if (eth != null) {
            eth.doOnShutdown();
        }
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        loggerNet.debug("handlerRemoved: kill timers in EthHandler");
        if (eth != null) {
            eth.doOnShutdown();
        }
    }

    public void activate() {
        loggerNet.info("ETH protocol activated");
        worldManager.getListener().trace("ETH protocol activated");
        sendStatus();
    }

    private void disconnect(ReasonCode reason) {
        msgQueue.disconnect(reason);
        channel.getNodeStatistics().nodeDisconnectedLocal(reason);
    }

    /**
     * Processing:
     * <ul>
     *   <li>checking if peer is using the same genesis, protocol and network</li>
     *   <li>seeing if total difficulty is higher than total difficulty from all other peers</li>
     *   <li>send GET_BLOCK_HASHES to this peer based on bestHash</li>
     * </ul>
     *
     * @param msg is the StatusMessage
     * @param ctx the ChannelHandlerContext
     */
    private void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        channel.getNodeStatistics().ethHandshake(msg);
        worldManager.getListener().onEthStatusUpdated(channel.getNode(), msg);

        try {
            if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                    || !isSupported(msg.getProtocolVersion())) {
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);

                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                state = EthState.STATUS_FAILED;
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
            } else if (msg.getNetworkId() != CONFIG.networkId()) {
                state = EthState.STATUS_FAILED;
                disconnect(ReasonCode.NULL_IDENTITY);
            } else if (peerDiscoveryMode) {
                loggerNet.debug("Peer discovery mode: STATUS received, disconnecting...");
                disconnect(ReasonCode.REQUESTED);
                ctx.close().sync();
                ctx.disconnect().sync();
            } else {
                eth = Eth.create(
                        fromCode(msg.getProtocolVersion()),
                        this,
                        channel.getNodeStatistics(),
                        applicationContext
                );
                eth.setBestHash(msg.getBestHash());
                state = EthState.STATUS_SUCCEEDED;
                loggerNet.info(
                        "Use Eth {} for {}, peerId {}",
                        eth.getVersion(),
                        ctx.channel().remoteAddress(),
                        getPeerIdShort()
                );
            }
        } catch (NoSuchElementException e) {
            loggerNet.debug("EthHandler already removed");
        }
    }

    private void sendStatus() {
        byte protocolVersion = EthHandler.VERSION, networkId = (byte) CONFIG.networkId();
        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
        StatusMessage msg = new StatusMessage(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMessage(msg);
    }

    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     */
    public void sendTransaction(Transaction transaction) {
        eth.sendTransaction(transaction);
    }

    public void sendNewBlock(Block block) {
        eth.sendNewBlock(block);
    }

    void sendMessage(EthMessage message) {
        msgQueue.sendMessage(message);
        channel.getNodeStatistics().ethOutbound.add();
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public StatusMessage getHandshakeStatusMessage() {
        return channel.getNodeStatistics().getEthLastInboundStatusMsg();
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void changeState(SyncStateName newState) {
        if (eth != null) {
            eth.changeState(newState);
        }
    }

    public void onSyncDone() {
        eth.doOnSyncDone();
    }

    public String getPeerId() {
        return peerId;
    }

    public String getPeerIdShort() {
        return Utils.getNodeIdShort(peerId);
    }

    public boolean isHashRetrievingDone() {
        return eth.isHashRetrievingDone();
    }

    public boolean isHashRetrieving() {
        return eth.isHashRetrieving();
    }

    public boolean hasBlocksLack() {
        return eth.hasBlocksLack();
    }

    public boolean hasInitPassed() {
        return state != EthState.INIT;
    }

    public boolean hasStatusSucceeded() {
        return state == EthState.STATUS_SUCCEEDED;
    }

    public boolean hasStatusFailed() {
        return state == EthState.STATUS_FAILED;
    }

    public void onDisconnect() {
        if (eth != null) {
            eth.doOnShutdown();
        }
    }

    public void logSyncStats() {
        eth.logSyncStats();
    }

    public boolean isIdle() {
        return eth.isIdle();
    }

    public byte[] getBestHash() {
        return eth.getBestHash();
    }

    public BigInteger getTotalDifficulty() {
        return channel.getNodeStatistics().getEthTotalDifficulty();
    }

    public void setMaxHashesAsk(int maxHashesAsk) {
        eth.setMaxHashesAsk(maxHashesAsk);
    }

    public int getMaxHashesAsk() {
        return eth.getMaxHashesAsk();
    }

    public void prohibitTransactionProcessing() {
        eth.prohibitTransactionProcessing();
    }

    public Eth.SyncStats getStats() {
        return eth.getSyncStats();
    }

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}