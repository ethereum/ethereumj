package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.*;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.ProtocolHandler;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.sync.SyncQueue;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.sync.SyncStateName;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.util.ByteUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.sync.SyncStateName.*;

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
public abstract class EthHandler extends ProtocolHandler<EthMessage> implements Eth {

    protected static final int MAX_HASHES_TO_SEND = 65536;

    @Autowired
    protected Blockchain blockchain;

    @Autowired
    protected SyncQueue queue;

    @Autowired
    protected EthereumListener ethereumListener;

    @Autowired
    protected Wallet wallet;

    @Autowired
    protected PendingState pendingState;

    protected EthVersion version;
    protected EthState ethState = EthState.INIT;

    protected boolean peerDiscoveryMode = false;

    private static final int BLOCKS_LACK_MAX_HITS = 5;
    private int blocksLackHits = 0;

    protected SyncStateName syncState = IDLE;
    protected boolean syncDone = false;
    protected boolean processTransactions = false;

    protected byte[] bestHash;

    protected BigInteger totalDifficulty = BigInteger.ZERO;

    /**
     * Last block hash to be asked from the peer,
     * its usage depends on Eth version
     *
     * @see Eth60
     * @see Eth61
     * @see Eth62
     */
    protected byte[] lastHashToAsk;
    protected int maxHashesAsk = CONFIG.maxHashesAsk();

    protected final SyncStatistics syncStats = new SyncStatistics();

    /**
     * The number above which blocks are treated as NEW,
     * filled by data gained from NewBlockHashes and NewBlock messages
     */
    protected long newBlockLowerNumber = Long.MAX_VALUE;

    protected EthHandler(EthVersion version) {
        this.version = version;
        messageFactory = createEthMessageFactory(version);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte(), version))
            loggerNet.trace("EthHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        onMessageReceived(msg);

        messageQueue.receivedMessage(msg);

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

    @Override
    public boolean hasCommand(Enum msgCommand) {

        return msgCommand instanceof EthMessageCodes;
    }

    @Override
    public byte getCommandCode(Enum msgCommand) {

        return ((EthMessageCodes) msgCommand).asByte();
    }

    @Override
    public byte getMaxCommandCode() {

        return (byte)EthMessageCodes.max(version);
    }

    @Override
    public boolean hasCommandCode(byte code) {

        return EthMessageCodes.inRange(code, version);
    }

    @Override
    public void activate(String name) {
        super.activate(name);
        loggerNet.info("ETH protocol activated");
        ethereumListener.trace("ETH protocol activated");
        sendStatus();
    }

    private MessageFactory createEthMessageFactory(EthVersion version) {
        switch (version) {
            case V60:   return new Eth60MessageFactory();
            case V61:   return new Eth61MessageFactory();
            case V62:   return new Eth62MessageFactory();
            default:    throw new IllegalArgumentException("Eth " + version + " is not supported");
        }
    }

    protected void disconnect(ReasonCode reason) {
        messageQueue.disconnect(reason);
        onLocalDisconnect(reason);
    }

    /**
     * Checking if peer is using the same genesis, protocol and network</li>
     *
     * @param msg is the StatusMessage
     * @param ctx the ChannelHandlerContext
     */
    private void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        try {
            if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                    || msg.getProtocolVersion() != version.getCode()) {
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                return;
            } else if (msg.getNetworkId() != CONFIG.networkId()) {
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
        int networkId = CONFIG.networkId();

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
    @Override
    public void sendTransaction(Transaction transaction) {
        Set<Transaction> txs = Collections.singleton(transaction);
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMessage(msg);
    }

    private void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        Set<Transaction> txSet = msg.getTransactions();
        pendingState.addWireTransactions(txSet);

        for (Transaction tx : txSet) {
            wallet.addTransaction(tx);
        }
    }

    public void sendNewBlock(Block block) {
        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        sendMessage(msg);
    }

    private void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        loggerSync.info("New block received: block.index [{}]", newBlock.getNumber());

        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        queue.addNew(newBlock, getNodeId());

        if (newBlockLowerNumber == Long.MAX_VALUE) {
            newBlockLowerNumber = newBlock.getNumber();
        }
    }

    protected void sendMessage(EthMessage message) {
        messageQueue.sendMessage(message);
        onMessageSent(message);
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
                getPeerIdShort(),
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
                    getPeerIdShort(),
                    syncState,
                    syncStats.getBlocksCount()
            );
                break;
            case HASH_RETRIEVING: loggerSync.info(
                    "Peer {}: [ {}, state {}, hashes count {} ]",
                    version,
                    getPeerIdShort(),
                    syncState,
                    syncStats.getHashesCount()
            );
                break;
            default: loggerSync.info(
                    "Peer {}: [ {}, state {} ]",
                    version,
                    getPeerIdShort(),
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

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}