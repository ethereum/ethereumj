package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.SyncQueue;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.sync.SyncStateName;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.server.Channel;
import org.ethereum.util.BIUtil;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

import static org.ethereum.sync.SyncStateName.*;
import static org.ethereum.util.BIUtil.isLessThan;

/**
 * Process the messages between peers with 'eth' capability on the network<br>
 * Contains common logic to all supported versions
 * delegating version specific stuff to its descendants
 *
 * Peers with 'eth' capability can send/receive:
 * <ul>
 * <li>STATUS                           :   Announce their status to the peer</li>
 * <li>NEW_BLOCK_HASHES                 :   Send a list of NEW block hashes</li>
 * <li>TRANSACTIONS                     :   Send a list of pending transactions</li>
 * <li>GET_BLOCK_HASHES                 :   Request a list of known block hashes</li>
 * <li>BLOCK_HASHES                     :   Send a list of known block hashes</li>
 * <li>GET_BLOCKS                       :   Request a list of blocks</li>
 * <li>BLOCKS                           :   Send a list of blocks</li>
 * <li>GET_BLOCK_HASHES_BY_NUMBER       :   Request list of know block hashes starting from the block</li>
 * </ul>
 */
public abstract class EthHandler extends SimpleChannelInboundHandler<EthMessage> implements Eth {

    private final static Logger loggerNet = LoggerFactory.getLogger("net");
    private final static Logger loggerSync = LoggerFactory.getLogger("sync");

    protected static final int MAX_HASHES_TO_SEND = 65536;

    @Autowired
    protected SystemProperties config;

    @Autowired
    protected Blockchain blockchain;

    @Autowired
    protected BlockStore blockstore;

    @Autowired
    protected SyncManager syncManager;

    @Autowired
    protected SyncQueue queue;

    @Autowired
    protected CompositeEthereumListener ethereumListener;

    @Autowired
    protected Wallet wallet;

    @Autowired
    protected PendingState pendingState;

    @Autowired
    protected ChannelManager channelManager;

    protected Channel channel;

    private MessageQueue msgQueue = null;

    protected EthVersion version;
    protected EthState ethState = EthState.INIT;

    protected boolean peerDiscoveryMode = false;

    private static final int BLOCKS_LACK_MAX_HITS = 5;
    private int blocksLackHits = 0;

    protected SyncStateName syncState = IDLE;
    protected boolean syncDone = false;
    protected boolean processTransactions = false;

    protected byte[] bestHash;

    protected Block bestBlock;

    /**
     * Last block hash to be asked from the peer,
     * its usage depends on Eth version
     *
     * @see Eth60
     * @see Eth61
     * @see Eth62
     */
    protected byte[] lastHashToAsk;
    protected int maxHashesAsk;

    protected final SyncStatistics syncStats = new SyncStatistics();

    /**
     * The number above which blocks are treated as NEW,
     * filled by data gained from NewBlockHashes and NewBlock messages
     */
    protected long newBlockLowerNumber = Long.MAX_VALUE;

    protected EthHandler(EthVersion version) {
        this.version = version;
    }

    @PostConstruct
    private void init() {
        maxHashesAsk = config.maxHashesAsk();
        bestBlock = blockchain.getBestBlock();
        ethereumListener.addListener(new EthereumListenerAdapter() {
             @Override
             public void onBlock(Block block, List<TransactionReceipt> receipts) {
                 bestBlock = block;
             }
         });
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte(), version))
            loggerNet.trace("EthHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        channel.getNodeStatistics().ethInbound.add();

        msgQueue.receivedMessage(msg);

        switch (msg.getCommand()) {
            case STATUS:
                processStatus((StatusMessage) msg, ctx);
                break;
            case TRANSACTIONS:
                processTransactions((TransactionsMessage) msg);
                break;
            case NEW_BLOCK:
                processNewBlock((NewBlockMessage) msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        loggerNet.error("Eth handling failed", cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        loggerNet.debug("handlerRemoved: kill timers in EthHandler");
        onShutdown();
    }

    public void activate() {
        loggerNet.info("ETH protocol activated");
        ethereumListener.trace("ETH protocol activated");
        sendStatus();
    }

    protected void disconnect(ReasonCode reason) {
        msgQueue.disconnect(reason);
        channel.getNodeStatistics().nodeDisconnectedLocal(reason);
    }

    /**
     * Checking if peer is using the same genesis, protocol and network</li>
     *
     * @param msg is the StatusMessage
     * @param ctx the ChannelHandlerContext
     */
    private void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {
        channel.getNodeStatistics().ethHandshake(msg);
        ethereumListener.onEthStatusUpdated(channel, msg);

        try {
            if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                    || msg.getProtocolVersion() != version.getCode()) {
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                return;
            } else if (msg.getNetworkId() != config.networkId()) {
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.NULL_IDENTITY);
                return;
            } else if (peerDiscoveryMode) {
                loggerNet.debug("Peer discovery mode: STATUS received, disconnecting...");
                disconnect(ReasonCode.REQUESTED);
                ctx.close().sync();
                ctx.disconnect().sync();
                return;
            }
        } catch (NoSuchElementException e) {
            loggerNet.debug("EthHandler already removed");
            return;
        }

        ethState = EthState.STATUS_SUCCEEDED;

        bestHash = msg.getBestHash();
    }

    protected void sendStatus() {
        byte protocolVersion = version.getCode();
        int networkId = config.networkId();

        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = bestBlock.getHash();
        StatusMessage msg = new StatusMessage(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMessage(msg);
    }

    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     */
    @Override
    public void sendTransaction(List<Transaction> txs) {
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMessage(msg);
    }

    private void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        List<Transaction> txSet = msg.getTransactions();
        pendingState.addWireTransactions(txSet);
    }

    public void sendNewBlock(Block block) {
        BigInteger parentTD = blockstore.getTotalDifficultyForHash(block.getParentHash());
        byte[] td = ByteUtil.bigIntegerToBytes(parentTD.add(new BigInteger(1, block.getDifficulty())));
        NewBlockMessage msg = new NewBlockMessage(block, td);
        sendMessage(msg);
    }

    public abstract void sendNewBlockHashes(Block block);

    private void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        loggerSync.info("New block received: block.index [{}]", newBlock.getNumber());

        // skip new block if TD is lower than ours
        if (isLessThan(newBlockMessage.getDifficultyAsBigInt(), blockchain.getTotalDifficulty())) {
            loggerSync.trace(
                    "New block difficulty lower than ours: [{}] vs [{}], skip",
                    newBlockMessage.getDifficultyAsBigInt(),
                    blockchain.getTotalDifficulty()
            );
            return;
        }

        channel.getNodeStatistics().setEthTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());
        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        queue.addNew(newBlock, channel.getNodeId());

        if (newBlockLowerNumber == Long.MAX_VALUE) {
            newBlockLowerNumber = newBlock.getNumber();
        }
    }

    protected void sendMessage(EthMessage message) {
        msgQueue.sendMessage(message);
        channel.getNodeStatistics().ethOutbound.add();
    }

    abstract protected void startHashRetrieving();

    abstract protected boolean startBlockRetrieving();

    @Override
    public void changeState(SyncStateName newState) {
        if (syncState == newState) {
            return;
        }

        loggerSync.trace(
                "Peer {}: changing state from {} to {}",
                channel.getPeerIdShort(),
                syncState,
                newState
        );

        if (newState == HASH_RETRIEVING) {
            syncStats.reset();
            startHashRetrieving();
        }
        if (newState == BLOCK_RETRIEVING) {
            syncStats.reset();
            boolean started = startBlockRetrieving();
            if (!started) {
                newState = IDLE;
            }
        }
        if (newState == BLOCKS_LACK) {
            if (syncDone || ++blocksLackHits < BLOCKS_LACK_MAX_HITS) {
                return;
            }
            blocksLackHits = 0; // reset
        }
        syncState = newState;
    }

    @Override
    public boolean isHashRetrievingDone() {
        return syncState == DONE_HASH_RETRIEVING;
    }

    @Override
    public boolean isHashRetrieving() {
        return syncState == HASH_RETRIEVING;
    }

    @Override
    public boolean hasBlocksLack() {
        return syncState == BLOCKS_LACK;
    }

    @Override
    public boolean hasStatusPassed() {
        return ethState != EthState.INIT;
    }

    @Override
    public boolean hasStatusSucceeded() {
        return ethState == EthState.STATUS_SUCCEEDED;
    }

    @Override
    public void onShutdown() {
        changeState(IDLE);
    }

    @Override
    public void logSyncStats() {
        if(!loggerSync.isInfoEnabled()) {
            return;
        }
        switch (syncState) {
            case BLOCK_RETRIEVING: loggerSync.info(
                    "Peer {}: [ {}, state {}, blocks count {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getBlocksCount()
            );
                break;
            case HASH_RETRIEVING: loggerSync.info(
                    "Peer {}: [ {}, state {}, hashes count {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getHashesCount()
            );
                break;
            default: loggerSync.info(
                    "Peer {}: [ {}, state {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState
            );
        }
    }

    @Override
    public boolean isIdle() {
        return syncState == IDLE;
    }

    @Override
    public byte[] getBestKnownHash() {
        return bestHash;
    }

    @Override
    public void setMaxHashesAsk(int maxHashesAsk) {
        this.maxHashesAsk = maxHashesAsk;
    }

    @Override
    public int getMaxHashesAsk() {
        return maxHashesAsk;
    }

    @Override
    public void setLastHashToAsk(byte[] lastHashToAsk) {
        this.lastHashToAsk = lastHashToAsk;
    }

    @Override
    public byte[] getLastHashToAsk() {
        return lastHashToAsk;
    }

    @Override
    public void enableTransactions() {
        processTransactions = true;
    }

    @Override
    public void disableTransactions() {
        processTransactions = false;
    }

    @Override
    public SyncStatistics getStats() {
        return syncStats;
    }

    @Override
    public EthVersion getVersion() {
        return version;
    }

    @Override
    public void onSyncDone() {
        syncDone = true;
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

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}