package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.eth.sync.SyncQueue;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.eth.sync.SyncStatistics;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.sync.SyncStateName.*;
import static org.ethereum.util.ByteUtil.wrap;

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

    @Autowired
    protected Blockchain blockchain;

    @Autowired
    protected SyncQueue queue;

    @Autowired
    protected WorldManager worldManager;

    protected Channel channel;

    private MessageQueue msgQueue = null;

    protected EthVersion version;
    protected EthState ethState = EthState.INIT;

    protected boolean peerDiscoveryMode = false;

    private static final int BLOCKS_LACK_MAX_HITS = 5;
    private int blocksLackHits = 0;

    protected SyncStateName syncState = IDLE;
    protected boolean processTransactions = true;

    protected byte[] bestHash;

    /**
     * Last block hash to be asked from the peer,
     * its usage depends on Eth version
     *
     * @see Eth60
     * @see Eth61
     */
    protected byte[] lastHashToAsk;
    protected int maxHashesAsk;

    /**
     * Hash list sent in GET_BLOCKS message,
     * useful if returned BLOCKS msg doesn't cover all sent hashes
     * or in case when peer is disconnected
     */
    protected final List<ByteArrayWrapper> sentHashes = Collections.synchronizedList(new ArrayList<ByteArrayWrapper>());

    protected final SyncStatistics syncStats = new SyncStatistics();

    protected EthHandler(EthVersion version) {
        this.version = version;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte()))
            loggerNet.trace("EthHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        channel.getNodeStatistics().ethInbound.add();

        switch (msg.getCommand()) {
            case STATUS:
                msgQueue.receivedMessage(msg);
                onStatusReceived((StatusMessage) msg, ctx);
                break;
            case NEW_BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processNewBlockHashes((NewBlockHashesMessage) msg);
                break;
            case TRANSACTIONS:
                msgQueue.receivedMessage(msg);
                processTransactions((TransactionsMessage) msg);
                break;
            case GET_BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processGetBlockHashes((GetBlockHashesMessage) msg);
                break;
            case BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                onBlockHashes((BlockHashesMessage) msg);
                break;
            case GET_BLOCKS:
                msgQueue.receivedMessage(msg);
                processGetBlocks((GetBlocksMessage) msg);
                break;
            case BLOCKS:
                msgQueue.receivedMessage(msg);
                processBlocks((BlocksMessage) msg);
                break;
            case NEW_BLOCK:
                msgQueue.receivedMessage(msg);
                processNewBlock((NewBlockMessage) msg);
                break;
            case GET_BLOCK_HASHES_BY_NUMBER:
                msgQueue.receivedMessage(msg);
                processGetBlockHashesByNumber((GetBlockHashesByNumberMessage) msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        loggerNet.error("Eth handling failed", cause);
        onShutdown();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        loggerNet.debug("handlerRemoved: kill timers in EthHandler");
        onShutdown();
    }

    public void activate() {
        loggerNet.info("ETH protocol activated");
        worldManager.getListener().trace("ETH protocol activated");
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
    private void onStatusReceived(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {
        channel.getNodeStatistics().ethHandshake(msg);
        worldManager.getListener().onEthStatusUpdated(channel.getNode(), msg);

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

        processStatus(msg);
    }

    protected void processStatus(StatusMessage msg) {
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

    protected void processNewBlockHashes(NewBlockHashesMessage msg) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: processing NEW block hashes, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHashes().size()
        );

        List<byte[]> hashes = msg.getBlockHashes();
        if (hashes.isEmpty()) {
            return;
        }

        this.bestHash = hashes.get(hashes.size() - 1);

        queue.addNewBlockHashes(hashes);
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

    protected void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        Set<Transaction> txSet = msg.getTransactions();
        blockchain.addPendingTransactions(txSet);

        for (Transaction tx : txSet) {
            worldManager.getWallet().addTransaction(tx);
        }
    }

    protected void sendGetBlockHashes() {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get block hashes, hash [{}], maxHashesAsk [{}]",
                channel.getPeerIdShort(),
                Hex.toHexString(lastHashToAsk),
                maxHashesAsk
        );
        GetBlockHashesMessage msg = new GetBlockHashesMessage(lastHashToAsk, maxHashesAsk);
        sendMessage(msg);
    }

    protected void processGetBlockHashes(GetBlockHashesMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFrom(
                msg.getBestHash(),
                Math.max(msg.getMaxBlocks(), CONFIG.maxHashesAsk())
        );

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }

    abstract protected void processBlockHashes(List<byte[]> received);

    protected void onBlockHashes(BlockHashesMessage blockHashesMessage) {
        if (loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: processing block hashes, size [{}]",
                channel.getPeerIdShort(),
                blockHashesMessage.getBlockHashes().size()
        );

        if (syncState != HASH_RETRIEVING) {
            return;
        }

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
        syncStats.addHashes(receivedHashes.size());

        processBlockHashes(receivedHashes);

        if (loggerSync.isInfoEnabled()) {
            if (syncState == DONE_HASH_RETRIEVING) {
                loggerSync.info(
                        "Peer {}: hashes sync completed, [{}] hashes in queue",
                        channel.getPeerIdShort(),
                        queue.hashStoreSize()
                );
            } else {
                queue.logHashQueueSize();
            }
        }
    }

    // Parallel download blocks based on hashQueue
    protected boolean sendGetBlocks() {
        // retrieve list of block hashes from queue
        // save them locally in case the remote peer
        // will return less blocks than requested.
        List<byte[]> hashes = queue.pollHashes();
        if (hashes.isEmpty()) {
            if(loggerSync.isInfoEnabled()) loggerSync.info(
                    "Peer {}: no more hashes in queue, idle",
                    channel.getPeerIdShort()
            );
            changeState(IDLE);
            return false;
        }

        sentHashes.clear();
        for (byte[] hash : hashes)
            this.sentHashes.add(wrap(hash));

        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get blocks, hashes.count [{}]",
                channel.getPeerIdShort(),
                sentHashes.size()
        );

        Collections.shuffle(hashes);
        GetBlocksMessage msg = new GetBlocksMessage(hashes);

        sendMessage(msg);

        return true;
    }

    protected void processGetBlocks(GetBlocksMessage msg) {

        List<byte[]> hashes = msg.getBlockHashes();

        Vector<Block> blocks = new Vector<>();
        for (byte[] hash : hashes) {
            Block block = blockchain.getBlockByHash(hash);
            blocks.add(block);
        }

        BlocksMessage bm = new BlocksMessage(blocks);
        sendMessage(bm);
    }

    protected void processBlocks(BlocksMessage blocksMessage) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: process blocks, size [{}]",
                channel.getPeerIdShort(),
                blocksMessage.getBlocks().size()
        );

        List<Block> blockList = blocksMessage.getBlocks();

        syncStats.addBlocks(blockList.size());

        // check if you got less blocks than you asked,
        // and keep the missing to ask again
        sentHashes.remove(wrap(Genesis.getInstance().getHash()));
        for (Block block : blockList){
            ByteArrayWrapper hash = wrap(block.getHash());
            sentHashes.remove(hash);
        }
        returnHashes();

        if(!blockList.isEmpty()) {
            queue.addBlocks(blockList, channel.getNodeId());
            queue.logHashQueueSize();
        } else {
            changeState(BLOCKS_LACK);
        }

        if (syncState == BLOCK_RETRIEVING) {
            sendGetBlocks();
        }
    }

    public void sendNewBlock(Block block) {
        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        sendMessage(msg);
    }

    protected void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        loggerSync.info("New block received: block.index [{}]", newBlock.getNumber());

        channel.getNodeStatistics().setEthTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());
        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        queue.addNewBlock(newBlock, channel.getNodeId());
        queue.logHashQueueSize();
    }

    abstract protected void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg);

    protected void sendMessage(EthMessage message) {
        msgQueue.sendMessage(message);
        channel.getNodeStatistics().ethOutbound.add();
    }

    abstract protected void startHashRetrieving();

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
            boolean sent = sendGetBlocks();
            if (!sent) {
                newState = IDLE;
            }
        }
        if (newState == BLOCKS_LACK) {
            if(++blocksLackHits < BLOCKS_LACK_MAX_HITS) {
                return;
            }
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
        returnHashes();
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

    protected void returnHashes() {
        if(loggerSync.isDebugEnabled()) loggerSync.debug(
                "Peer {}: return [{}] hashes back to store",
                channel.getPeerIdShort(),
                sentHashes.size()
        );

        synchronized (sentHashes) {
            queue.returnHashes(sentHashes);
        }

        sentHashes.clear();
    }

    public EthVersion getVersion() {
        return version;
    }

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}