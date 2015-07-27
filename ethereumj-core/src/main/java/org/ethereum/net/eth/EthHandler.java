package org.ethereum.net.eth;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public final static byte VERSION = 61;

    private static final int NO_MORE_BLOCKS_THRESHOLD = 5;
    private int noMoreBlocksHits = 0;

    private final static Logger loggerSync = LoggerFactory.getLogger("sync");
    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    private MessageQueue msgQueue = null;

    private String peerId;
    private SyncState syncState = SyncState.IDLE;
    private EthState peerState = EthState.INIT;
    private long blocksLoadedCnt = 0;

    private StatusMessage handshakeStatusMessage = null;

    private boolean peerDiscoveryMode = false;

    private Timer getBlocksTimer = new Timer("GetBlocksTimer");
    private Timer getTxTimer = new Timer("GetTransactionsTimer");

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private WorldManager worldManager;

    private Channel channel;

    private List<ByteArrayWrapper> sentHashes;
    private Block lastBlock = Genesis.getInstance();

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
            loggerNet.info("EthHandler invoke: [{}]", msg.getCommand());

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
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        loggerNet.debug("handlerRemoved: kill timers in EthHandler");
        returnHashes();
        this.killTimers();
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
    public void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        this.handshakeStatusMessage = msg;

        channel.getNodeStatistics().ethHandshake(msg);

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
            killTimers();
            ctx.close().sync();
            ctx.disconnect().sync();
        } else {
            peerState = EthState.STATUS_SUCCEEDED;
        }
    }

    private void processBlockHashes(BlockHashesMessage blockHashesMessage) {
        if(syncState != SyncState.HASH_RETRIEVING) {
            return;
        }

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
        BlockQueue chainQueue = blockchain.getQueue();

        // result is empty, peer has no more hashes
        // or peer doesn't have the best hash anymore
        if (receivedHashes.isEmpty()) {
            changeState(SyncState.DONE_HASH_RETRIEVING);
        } else {
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
                loggerSync.trace("Catch up with the hashes until: {[]}", foundHash);
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
            if (loggerSync.isInfoEnabled()) {
                blocksLoadedCnt += blockList.size();
            }

            if (blockchain.getQueue().isHashesEmpty()) {
                loggerSync.info("Peer {}: no more hashes in queue, idle", Utils.getNodeIdShort(peerId));
                changeState(SyncState.IDLE);
            }
        } else {
            changeState(SyncState.NO_MORE_BLOCKS);
        }

        if (syncState == SyncState.BLOCK_RETRIEVING) {
            sendGetBlocks();
        }
    }

    private void returnHashes() {
        if(sentHashes != null) {
            if(loggerSync.isDebugEnabled()) {
                loggerSync.debug("Peer {}: [{}] hashes returned", Utils.getNodeIdShort(peerId), sentHashes.size());
            }
            blockchain.getQueue().returnHashes(sentHashes);
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

        ImportResult result = blockchain.tryToConnect(newBlock);
        if(result == ImportResult.NO_PARENT) {
            // adding block to the queue
            // there will be decided how to
            // connect it to the chain
            blockchain.getQueue().addNewBlock(newBlockMessage.getBlock());
            blockchain.getQueue().logHashQueueSize();
        } else {
            changeState(SyncState.DONE_SYNC);
            loggerSync.info("Peer {}: new block successfully imported", Utils.getNodeIdShort(peerId));
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
        GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, CONFIG.maxHashesAsk());
        sendMessage(msg);
    }

    // Parallel download blocks based on hashQueue
    public void sendGetBlocks() {
        BlockQueue queue = blockchain.getQueue();

        // retrieve list of block hashes from queue
        // save them locally in case the remote peer
        // will return less blocks than requested.
        List<byte[]> hashes = queue.getHashes();
        this.sentHashes = new ArrayList<>();
        for (byte[] hash : hashes)
            this.sentHashes.add(wrap(hash));

        if(loggerSync.isDebugEnabled()) {
            loggerSync.debug("Peer {}: [{}] hashes sent", Utils.getNodeIdShort(peerId), sentHashes.size());
        }

        if (hashes.isEmpty()) {
            return;
        }

        Collections.shuffle(hashes);
        GetBlocksMessage msg = new GetBlocksMessage(hashes);

        if (loggerNet.isDebugEnabled())
            loggerNet.debug(msg.getDetailedString());

        sendMessage(msg);
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


    private void startTxTimer() {
        getTxTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetTransactions();
            }
        }, 2000, 10000);
    }

    private void stopGetBlocksTimer() {
        getBlocksTimer.cancel();
        getBlocksTimer.purge();
    }

    private void stopGetTxTimer() {
        getTxTimer.cancel();
        getTxTimer.purge();
    }

    public void killTimers() {
        stopGetBlocksTimer();
        stopGetTxTimer();
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public StatusMessage getHandshakeStatusMessage() {
        return handshakeStatusMessage;
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
        if(newState == SyncState.HASH_RETRIEVING) {
            sendGetBlockHashes();
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            blocksLoadedCnt = 0;
            sendGetBlocks();
        }
        if(newState == SyncState.NO_MORE_BLOCKS) {
            if(++noMoreBlocksHits < NO_MORE_BLOCKS_THRESHOLD) {
                return;
            }
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
        loggerSync.info(
                "Peer {}: [state {}, blocks count {}, last block {}]",
                Utils.getNodeIdShort(peerId),
                syncState,
                blocksLoadedCnt,
                lastBlock.getNumber()
        );
    }

    public boolean isIdle() {
        return syncState == SyncState.IDLE;
    }

    public boolean isSyncDone() {
        return syncState == SyncState.DONE_SYNC;
    }

    enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}