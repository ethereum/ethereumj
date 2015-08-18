package org.ethereum.net.eth;

import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.net.eth.sync.SyncStateName.*;
import static org.ethereum.net.eth.sync.SyncStateName.BLOCK_RETRIEVING;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * Encapsulates Eth protocol implementation<br>
 * {@link EthHandler} delegates handling of all messages except {@code STATUS} to this class and its hierarchy<br>
 * Derived classes represent different versions of Eth protocol
 *
 * @author Mikhail Kalinin
 * @since 17.08.2015
 */
public abstract class Eth {

    private final static Logger loggerSync = LoggerFactory.getLogger("sync");
    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    private static final int BLOCKS_LACK_MAX_HITS = 5;
    private int blocksLackHits = 0;

    protected EthVersion version;

    private SyncStateName state = IDLE;
    private boolean processTransactions = true;

    protected EthHandler handler;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private BlockQueue queue;

    @Autowired
    private WorldManager worldManager;

    private List<ByteArrayWrapper> sentHashes;
    private Block lastBlock = Genesis.getInstance();
    private byte[] lastHash = lastBlock.getHash();
    private byte[] bestHash;
    private int maxHashesAsk;

    protected SyncStats syncStats = new SyncStats();
    protected NodeStatistics nodeStats;

    protected Eth(EthVersion version) {
        this.version = version;
    }

    public void doOnShutdown() {
        returnHashes();
    }

    public synchronized void changeState(SyncStateName newState) {
        if (state == newState) {
            return;
        }

        loggerSync.trace(
                "Peer {}: changing state from {} to {}",
                handler.getPeerIdShort(),
                state,
                newState
        );

        if (newState == HASH_RETRIEVING) {
            syncStats.reset();
            sendGetBlockHashes();
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
        state = newState;
    }

    public void logSyncStats() {
        if(!loggerSync.isInfoEnabled()) {
            return;
        }
        switch (state) {
            case BLOCK_RETRIEVING: loggerSync.info(
                    "Peer {}: [state {}, blocks count {}, last block {}]",
                    handler.getPeerIdShort(),
                    state,
                    syncStats.getBlocksCount(),
                    lastBlock.getNumber()
            );
                break;
            case HASH_RETRIEVING: loggerSync.info(
                    "Peer {}: [state {}, hashes count {}, last hash {}]",
                    handler.getPeerIdShort(),
                    state,
                    syncStats.getHashesCount(),
                    Hex.toHexString(lastHash)
            );
                break;
            default: loggerSync.info(
                    "Peer {}: [state {}]",
                    handler.getPeerIdShort(),
                    state
            );
        }
    }

    void prohibitTransactionProcessing() {
        processTransactions = false;
    }

    void doOnSyncDone() {
        processTransactions = true;
    }

    void processBlockHashes(BlockHashesMessage blockHashesMessage) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: processing block hashes, size [{}]",
                handler.getPeerIdShort(),
                blockHashesMessage.getBlockHashes().size()
        );

        if (state != HASH_RETRIEVING) {
            return;
        }

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();

        syncStats.addHashes(receivedHashes.size());

        if (!receivedHashes.isEmpty()) {
            byte[] foundHash = null;
            boolean foundExisting = false;
            List<byte[]> newHashes = null;
            for(int i = 0; i < receivedHashes.size(); i++) {
                byte[] hash = receivedHashes.get(i);
                if(blockchain.isBlockExist(hash)) {
                    foundExisting = true;
                    newHashes = org.ethereum.util.CollectionUtils.truncate(receivedHashes, i);
                    foundHash = hash;
                    break;
                }
            }
            if(newHashes == null) {
                newHashes = receivedHashes;
            }

            queue.addHashes(newHashes);

            lastHash = newHashes.get(newHashes.size() - 1);

            if (foundExisting) {
                changeState(DONE_HASH_RETRIEVING); // store unknown hashes in queue until known hash is found
                loggerSync.trace(
                        "Peer {}: got existing hash [{}]",
                        handler.getPeerIdShort(),
                        Hex.toHexString(foundHash)
                );
            }
        }

        if (state == DONE_HASH_RETRIEVING) {
            loggerSync.info(
                    "Peer {}: hashes sync completed, [{}] hashes in queue",
                    handler.getPeerIdShort(),
                    queue.getHashStore().size()
            );
        } else {
            // no known hash has been reached
            queue.logHashQueueSize();
            if(state == HASH_RETRIEVING) {
                sendGetBlockHashes(); // another getBlockHashes with last received hash.
            }
        }
    }

    void processBlocks(BlocksMessage blocksMessage) {
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: process blocks, size [{}]",
                handler.getPeerIdShort(),
                blocksMessage.getBlocks().size()
        );

        List<Block> blockList = blocksMessage.getBlocks();

        syncStats.addBlocks(blockList.size());

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
            queue.addBlocks(blockList);
            queue.logHashQueueSize();
        } else {
            changeState(BLOCKS_LACK);
        }

        if (state == BLOCK_RETRIEVING) {
            sendGetBlocks();
        }
    }

    void processGetBlockHashes(GetBlockHashesMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFrom(msg.getBestHash(), msg.getMaxBlocks());

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        handler.sendMessage(msgHashes);
    }

    void processGetBlocks(GetBlocksMessage msg) {

        List<byte[]> hashes = msg.getBlockHashes();

        Vector<Block> blocks = new Vector<>();
        for (byte[] hash : hashes) {
            Block block = blockchain.getBlockByHash(hash);
            blocks.add(block);
        }

        BlocksMessage bm = new BlocksMessage(blocks);
        handler.sendMessage(bm);
    }

    void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        if (newBlock.getNumber() > this.lastBlock.getNumber())
            this.lastBlock = newBlock;

        loggerNet.info("New block received: block.index [{}]", newBlock.getNumber());

        nodeStats.setEthTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());
        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        queue.addNewBlock(newBlock);
        queue.logHashQueueSize();
    }

    void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        Set<Transaction> txSet = msg.getTransactions();
        blockchain.addPendingTransactions(txSet);

        for (Transaction tx : txSet) {
            worldManager.getWallet().addTransaction(tx);
        }
    }

    void sendTransaction(Transaction transaction) {
        Set<Transaction> txs = Collections.singleton(transaction);
        TransactionsMessage msg = new TransactionsMessage(txs);
        handler.sendMessage(msg);
    }

    void sendNewBlock(Block block) {
        NewBlockMessage msg = new NewBlockMessage(block, block.getDifficulty());
        handler.sendMessage(msg);
    }

    void sendGetBlockHashes() {
        byte[] bestHash = queue.getBestHash();
        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get block hashes, bestHash [{}], maxHashesAsk [{}]",
                handler.getPeerIdShort(),
                Hex.toHexString(bestHash),
                maxHashesAsk
        );
        GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, maxHashesAsk);
        handler.sendMessage(msg);
    }

    // Parallel download blocks based on hashQueue
    boolean sendGetBlocks() {
        // retrieve list of block hashes from queue
        // save them locally in case the remote peer
        // will return less blocks than requested.
        List<byte[]> hashes = queue.getHashes();
        if (hashes.isEmpty()) {
            if(loggerSync.isInfoEnabled()) loggerSync.info(
                    "Peer {}: no more hashes in queue, idle",
                    handler.getPeerIdShort()
            );
            changeState(IDLE);
            return false;
        }

        this.sentHashes = new ArrayList<>();
        for (byte[] hash : hashes)
            this.sentHashes.add(wrap(hash));

        if(loggerSync.isTraceEnabled()) loggerSync.trace(
                "Peer {}: send get blocks, hashes.count [{}]",
                handler.getPeerIdShort(),
                sentHashes.size()
        );

        Collections.shuffle(hashes);
        GetBlocksMessage msg = new GetBlocksMessage(hashes);

        if (loggerNet.isTraceEnabled())
            loggerNet.debug(msg.getDetailedString());

        handler.sendMessage(msg);

        return true;
    }

    protected void returnHashes() {
        if(sentHashes != null) {

            if(loggerSync.isDebugEnabled()) loggerSync.debug(
                    "Peer {}: return [{}] hashes back to store",
                    handler.getPeerIdShort(),
                    sentHashes.size()
            );

            queue.returnHashes(sentHashes);
            sentHashes.clear();
        }
    }

    public static Eth create(EthVersion v, EthHandler handler, NodeStatistics statistics, ApplicationContext ctx) {
        switch (v) {
            default:    return ctx.getBean(Eth60.class, handler, statistics);
        }
    }

    boolean isHashRetrievingDone() {
        return state == DONE_HASH_RETRIEVING;
    }

    boolean isHashRetrieving() {
        return state == HASH_RETRIEVING;
    }

    boolean hasBlocksLack() {
        return state == BLOCKS_LACK;
    }

    boolean isIdle() {
        return state == IDLE;
    }

    public void setHandler(EthHandler handler) {
        this.handler = handler;
    }

    public BigInteger getTotalDifficulty() {
        return nodeStats.getEthTotalDifficulty();
    }

    public void setBestHash(byte[] bestHash) {
        this.bestHash = bestHash;
    }

    public byte[] getBestHash() {
        return bestHash;
    }

    public int getMaxHashesAsk() {
        return maxHashesAsk;
    }

    public void setMaxHashesAsk(int maxHashesAsk) {
        this.maxHashesAsk = maxHashesAsk;
    }

    public SyncStats getSyncStats() {
        return syncStats;
    }

    public EthVersion getVersion() {
        return version;
    }

    public static class SyncStats {
        private long updatedAt;
        private long blocksCount;
        private long hashesCount;
        private int emptyResponsesCount;

        SyncStats() {
            reset();
        }

        void reset() {
            updatedAt = System.currentTimeMillis();
            blocksCount = 0;
            hashesCount = 0;
            emptyResponsesCount = 0;
        }

        void addBlocks(long cnt) {
            blocksCount += cnt;
            fixCommon(cnt);
        }

        void addHashes(long cnt) {
            hashesCount += cnt;
            fixCommon(cnt);
        }

        private void fixCommon(long cnt) {
            if (cnt == 0) {
                emptyResponsesCount += 1;
            }
            updatedAt = System.currentTimeMillis();
        }

        public long getBlocksCount() {
            return blocksCount;
        }

        public long getHashesCount() {
            return hashesCount;
        }

        public long millisSinceLastUpdate() {
            return System.currentTimeMillis() - updatedAt;
        }

        public int getEmptyResponsesCount() {
            return emptyResponsesCount;
        }
    }
}
