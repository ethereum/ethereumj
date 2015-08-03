package org.ethereum.net.eth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.message.StaticMessages.GET_TRANSACTIONS_MESSAGE;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * Process the messages between peers with 'eth' capability on the network.
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

    private final static Logger loggerSync = LoggerFactory.getLogger("sync");
    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    public final static byte VERSION = 60;

    private static final int NO_MORE_BLOCKS_THRESHOLD = 5;
    private int noMoreBlocksHits = 0;

    private MessageQueue msgQueue = null;

    private String peerId;
    private SyncState syncState = SyncState.IDLE;
    private EthState peerState = EthState.INIT;
    private long blocksLoadedCnt = 0;
    private long hashesLoadedCnt = 0;
    private boolean processTransactions = false;

    private boolean peerDiscoveryMode = false;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private WorldManager worldManager;

    private Channel channel;

    private List<ByteArrayWrapper> sentHashes;
    private Block lastBlock = Genesis.getInstance();
    private byte[] lastHash = lastBlock.getHash();
    private byte[] bestHash;
    private int maxHashesAsk;

    public EthHandler() {
        this.peerDiscoveryMode = false;
    }

    public EthHandler(MessageQueue msgQueue, boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
        this.msgQueue = msgQueue;
    }

    public void activate() {
        loggerNet.info("ETH protocol activated");
        worldManager.getListener().trace("ETH protocol activated");
        sendStatus();
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
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
                processStatus((StatusMessage) msg, ctx);
                break;
            case GET_TRANSACTIONS:
                // todo: eventually get_transaction is going deprecated
//                msgQueue.receivedMessage(msg);
//                sendPendingTransactions();
                break;
            case TRANSACTIONS:
                msgQueue.receivedMessage(msg);
                processTransactions((TransactionsMessage) msg);
                // List<Transaction> txList = transactionsMessage.getTransactions();
                // for(Transaction tx : txList)
                // WorldManager.getInstance().getBlockchain().applyTransaction(null,
                // tx);
                // WorldManager.getInstance().getWallet().addTransaction(tx);
                break;
            case GET_BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processGetBlockHashes((GetBlockHashesMessage) msg);
                break;
            case BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processBlockHashes((BlockHashesMessage) msg);
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
            default:
                break;
        }
    }

    private void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        Set<Transaction> txSet = msg.getTransactions();
        worldManager.getBlockchain().
                addPendingTransactions(txSet);

        for (Transaction tx : txSet) {
            worldManager.getWallet().addTransaction(tx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        loggerNet.error("Eth handling failed", cause);
        returnHashes();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        loggerNet.debug("handlerRemoved: kill timers in EthHandler");
        returnHashes();
    }

    void disconnect(ReasonCode reason) {
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
    public void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        channel.getNodeStatistics().ethHandshake(msg);
        worldManager.getListener().onEthStatusUpdated(channel.getNode(), msg);

        this.bestHash = msg.getBestHash();

        try {
            if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                    || msg.getProtocolVersion() != VERSION) {
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);

                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                peerState = EthState.STATUS_FAILED;
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
            } else if (msg.getNetworkId() != CONFIG.networkId()) {
                peerState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.NULL_IDENTITY);
            } else if (peerDiscoveryMode) {
                loggerNet.debug("Peer discovery mode: STATUS received, disconnecting...");
                disconnect(ReasonCode.REQUESTED);
                ctx.close().sync();
                ctx.disconnect().sync();
            } else {
                peerState = EthState.STATUS_SUCCEEDED;
            }
        } catch (NoSuchElementException e) {
            loggerNet.debug("EthHandler already removed");
        }
    }

    private void processBlockHashes(BlockHashesMessage blockHashesMessage) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: processing block hashes, size [{}]",
                Utils.getNodeIdShort(peerId),
                blockHashesMessage.getBlockHashes().size()
        );

        if(syncState != SyncState.HASH_RETRIEVING) {
            return;
        }

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
        BlockQueue chainQueue = blockchain.getQueue();

        if (!receivedHashes.isEmpty()) {
            hashesLoadedCnt += receivedHashes.size();
            lastHash = receivedHashes.get(0);

            chainQueue.addHashes(receivedHashes);
            // store unknown hashes in queue until known hash is found
            final byte[] latestHash = blockchain.getBestBlockHash();
            byte[] foundHash = CollectionUtils.find(receivedHashes, new Predicate<byte[]>() {
                @Override
                public boolean evaluate(byte[] hash) {
                    return FastByteComparisons.compareTo(hash, 0, 32, latestHash, 0, 32) == 0;
                }
            });
            if (foundHash != null) {
                changeState(SyncState.DONE_HASH_RETRIEVING); // store unknown hashes in queue until known hash is found
                loggerSync.trace("Peer {}: got our best hash [{}]", Utils.getNodeIdShort(peerId), Hex.toHexString(foundHash));
            }
        }

        if(syncState == SyncState.DONE_HASH_RETRIEVING) {
            loggerSync.info("Block hashes sync completed: {} hashes in queue", chainQueue.getHashStore().size());
            chainQueue.addHash(blockchain.getBestBlockHash());
        } else {
            // no known hash has been reached
            chainQueue.logHashQueueSize();
            if(syncState == SyncState.HASH_RETRIEVING) {
                sendGetBlockHashes(); // another getBlockHashes with last received hash.
            }
        }
    }

    private void processBlocks(BlocksMessage blocksMessage) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: process blocks, size [{}]",
                Utils.getNodeIdShort(peerId),
                blocksMessage.getBlocks().size()
        );

        List<Block> blockList = blocksMessage.getBlocks();

        if (!blockList.isEmpty()) {
            Block block = blockList.get(blockList.size() - 1);
            if (block.getNumber() > lastBlock.getNumber())
                lastBlock = blockList.get(blockList.size() - 1);
        }

        // check if you got less blocks than you asked,
        // and keep the missing to ask again
        sentHashes.remove(wrap(Genesis.getInstance().getHash()));
        for (Block block : blockList){
            ByteArrayWrapper hash = wrap(block.getHash());
            sentHashes.remove(hash);
        }
        returnHashes();

        if(!blockList.isEmpty()) {
            blockchain.getQueue().addBlocks(blockList);
            blockchain.getQueue().logHashQueueSize();
            blocksLoadedCnt += blockList.size();
        } else {
            changeState(SyncState.NO_MORE_BLOCKS);
        }

        if(syncState == SyncState.BLOCK_RETRIEVING) {
            sendGetBlocks();
        }
    }

    private void returnHashes() {
        if(sentHashes != null) {
            if(loggerSync.isDebugEnabled()) {
                loggerSync.debug("Peer {}: return [{}] hashes back to store", Utils.getNodeIdShort(peerId), sentHashes.size());
            }
            blockchain.getQueue().returnHashes(sentHashes);
            sentHashes.clear();
        }
    }


    /**
     * Processing NEW_BLOCK announce message
     *
     * @param newBlockMessage - new block message
     */
    public void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        if (newBlock.getNumber() > this.lastBlock.getNumber())
            this.lastBlock = newBlock;

        loggerNet.info("New block received: block.index [{}]", newBlock.getNumber());

        channel.getNodeStatistics().setEthTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());
        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        blockchain.getQueue().addNewBlock(newBlock);
        blockchain.getQueue().logHashQueueSize();
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
        Set<Transaction> txs = new HashSet<>(Arrays.asList(transaction));
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMessage(msg);
    }

    public void sendNewBlock(Block block) {
        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        sendMessage(msg);
    }

    private void sendGetTransactions() {
        sendMessage(GET_TRANSACTIONS_MESSAGE);
    }

    private void sendGetBlockHashes() {
        byte[] bestHash = blockchain.getQueue().getBestHash();
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get block hashes, bestHash [{}], maxHashesAsk [{}]",
                Utils.getNodeIdShort(peerId),
                Hex.toHexString(bestHash),
                maxHashesAsk
        );
        GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, maxHashesAsk);
        sendMessage(msg);
    }

    // Parallel download blocks based on hashQueue
    public boolean sendGetBlocks() {
        BlockQueue queue = blockchain.getQueue();

        // retrieve list of block hashes from queue
        // save them locally in case the remote peer
        // will return less blocks than requested.
        List<byte[]> hashes = queue.getHashes();
        if (hashes.isEmpty()) {
            if(loggerSync.isInfoEnabled()) loggerSync.info(
                    "Peer {}: no more hashes in queue, idle",
                    Utils.getNodeIdShort(peerId)
            );
            changeState(SyncState.IDLE);
            return false;
        }

        this.sentHashes = new ArrayList<>();
        for (byte[] hash : hashes)
            this.sentHashes.add(wrap(hash));

        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get blocks hashes.count [{}]",
                Utils.getNodeIdShort(peerId),
                sentHashes.size()
        );

        Collections.shuffle(hashes);
        GetBlocksMessage msg = new GetBlocksMessage(hashes);

        if (loggerNet.isTraceEnabled())
            loggerNet.debug(msg.getDetailedString());

        sendMessage(msg);

        return true;
    }

    private void sendPendingTransactions() {
        Set<Transaction> pendingTxs =
                worldManager.getBlockchain()
                        .getPendingTransactions();
        TransactionsMessage msg = new TransactionsMessage(pendingTxs);
        sendMessage(msg);
    }

    private void processGetBlockHashes(GetBlockHashesMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFrom(msg.getBestHash(), msg.getMaxBlocks());

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }

    private void processGetBlocks(GetBlocksMessage msg) {

        List<byte[]> hashes = msg.getBlockHashes();

        Vector<Block> blocks = new Vector<>();
        for (byte[] hash : hashes) {
            Block block = blockchain.getBlockByHash(hash);
            blocks.add(block);
        }

        BlocksMessage bm = new BlocksMessage(blocks);
        sendMessage(bm);
    }

    private void sendMessage(EthMessage message) {
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

    synchronized void changeState(SyncState newState) {
        loggerSync.info(
                "Peer {}: changing state from {} to {}",
                getPeerIdShort(),
                syncState,
                newState
        );
        if(syncState == newState) {
            return;
        }
        if(newState == SyncState.HASH_RETRIEVING) {
            hashesLoadedCnt = 0;
            sendGetBlockHashes();
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            blocksLoadedCnt = 0;
            boolean sent = sendGetBlocks();
            if(!sent) {
                return;
            }
        }
        if(newState == SyncState.NO_MORE_BLOCKS) {
            if(++noMoreBlocksHits < NO_MORE_BLOCKS_THRESHOLD) {
                return;
            }
        }
        if(newState == SyncState.DONE_SYNC) {
            processTransactions = true;
            newState = SyncState.IDLE;
        }
        this.syncState = newState;
    }

    public String getPeerId() {
        return peerId;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public boolean isHashRetrievingDone() {
        return syncState == SyncState.DONE_HASH_RETRIEVING;
    }

    public boolean isHashRetrieving() {
        return syncState == SyncState.HASH_RETRIEVING;
    }

    public boolean hasNoMoreBlocks() {
        return syncState == SyncState.NO_MORE_BLOCKS;
    }

    public boolean hasInitPassed() {
        return peerState != EthState.INIT;
    }

    public boolean hasStatusSucceeded() {
        return peerState == EthState.STATUS_SUCCEEDED;
    }

    public boolean hasStatusFailed() {
        return peerState == EthState.STATUS_FAILED;
    }

    public void onDisconnect() {
        returnHashes();
    }

    void logSyncStats() {
        if(!loggerSync.isInfoEnabled()) {
            return;
        }
        switch (syncState) {
            case BLOCK_RETRIEVING: loggerSync.info(
                        "Peer {}: [state {}, blocks count {}, last block {}]",
                        Utils.getNodeIdShort(peerId),
                        syncState,
                        blocksLoadedCnt,
                        lastBlock.getNumber()
                );
                break;
            case HASH_RETRIEVING: loggerSync.info(
                    "Peer {}: [state {}, hashes count {}, last hash {}]",
                    Utils.getNodeIdShort(peerId),
                    syncState,
                    hashesLoadedCnt,
                    Hex.toHexString(lastHash)
                );
                break;
            default: loggerSync.info(
                    "Peer {}: [state {}]",
                    Utils.getNodeIdShort(peerId),
                    syncState
            );
        }
    }

    public boolean isIdle() {
        return syncState == SyncState.IDLE;
    }

    public boolean isSyncDone() {
        return syncState == SyncState.DONE_SYNC;
    }

    public NodeStatistics getNodeStatistics() {
        return channel.getNodeStatistics();
    }

    public byte[] getBestHash() {
        return bestHash;
    }

    public BigInteger getTotalDifficulty() {
        return channel.getNodeStatistics().getEthTotalDifficulty();
    }

    void setMaxHashesAsk(int maxHashesAsk) {
        this.maxHashesAsk = maxHashesAsk;
    }

    long getHashesLoadedCnt() {
        return hashesLoadedCnt;
    }

    String getPeerIdShort() {
        return Utils.getNodeIdShort(peerId);
    }

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}