package org.ethereum.net.eth.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.ImportResult;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.BlockQueueImpl;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.HashStore;
import org.ethereum.db.HashStoreImpl;
import org.ethereum.core.Blockchain;
import org.ethereum.net.eth.sync.SyncManager;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Thread.sleep;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.core.ImportResult.IMPORTED_NOT_BEST;
import static org.ethereum.core.ImportResult.NO_PARENT;
import static org.ethereum.core.ImportResult.IMPORTED_BEST;

/**
 * The processing queue for blocks to be validated and added to the blockchain.
 * This class also maintains the list of hashes from the peer with the heaviest sub-tree.
 * Based on these hashes, blocks are added to the queue.
 *
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
@Component
@DependsOn("worldManager")
public class SyncQueue {

    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    /**
     * Store holding a list of hashes of the heaviest chain on the network,
     * for which this client doesn't have the blocks yet
     */
    private HashStore hashStore;

    /**
     * Queue with blocks to be validated and added to the blockchain
     */
    private org.ethereum.db.BlockQueue blockQueue;

    /**
     * Last block in the queue to be processed
     */
    private Block lastBlock;

    private byte[] bestHash;

    @Autowired
    Blockchain blockchain;

    @Autowired
    SyncManager syncManager;

    public void init() {
        MapDBFactory mapDBFactory = new MapDBFactoryImpl();
        hashStore = new HashStoreImpl();
        ((HashStoreImpl)hashStore).setMapDBFactory(mapDBFactory);
        hashStore.open();

        blockQueue = new BlockQueueImpl();
        ((BlockQueueImpl)blockQueue).setMapDBFactory(mapDBFactory);
        blockQueue.open();

        Runnable queueProducer = new Runnable(){

            @Override
            public void run() {
                produceQueue();
            }
        };

        Thread t=new Thread (queueProducer);
        t.start();
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        while (1==1){

            try {
                BlockWrapper wrapper = blockQueue.take();
                logger.info("BlockQueue size: {}", blockQueue.size());
                ImportResult importResult = blockchain.tryToConnect(wrapper.getBlock());

                // In case we don't have a parent on the chain
                // return the try and wait for more blocks to come.
                if (importResult == NO_PARENT) {
                    logger.info("No parent on the chain for block.number: {} block.hash: {}", wrapper.getNumber(), wrapper.getBlock().getShortHash());
                    wrapper.importFailed();
                    syncManager.tryGapRecovery(wrapper);
                    blockQueue.add(wrapper);
                    sleep(2000);
                }

                if (wrapper.isNewBlock() && importResult.isSuccessful())
                    syncManager.notifyNewBlockImported(wrapper);

                if (importResult == IMPORTED_BEST)
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());


            } catch (Throwable e) {
                logger.error("Error: {} ", e);
            }

        }
    }

    /**
     * Add a list of blocks to the processing queue.
     * The list is validated by making sure the first block in the received list of blocks
     * is the next expected block number of the queue.
     *
     * The queue is configured to contain a maximum number of blocks to avoid memory issues
     * If the list exceeds that, the rest of the received blocks in the list are discarded.
     *
     * @param blockList - the blocks received from a peer to be added to the queue
     */
    public void addBlocks(List<Block> blockList) {

        List<BlockWrapper> wrappers = CollectionUtils.collectList(blockList, new Functional.Function<Block, BlockWrapper>() {
            @Override
            public BlockWrapper apply(Block block) {
                return new BlockWrapper(block);
            }
        });
        blockQueue.addAll(wrappers);

        lastBlock = blockList.get(blockList.size() - 1);

        logger.info("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                lastBlock.getNumber());
    }

    /**
     * adding single block to the queue
     *
     * @param block block
     */
    public void addBlock(Block block) {
        addBlock(new BlockWrapper(block));
    }

    /**
     * adding new block to the queue
     *
     * @param block new block
     */
    public void addNewBlock(Block block) {
        BlockWrapper wrapper = new BlockWrapper(block, true);
        wrapper.setReceivedAt(System.currentTimeMillis());
        addBlock(wrapper);
    }

    /**
     * adding single block to the queue usually
     * a result of a NEW_BLOCK message announce.
     *
     * @param wrapper block wrapper
     */
    public void addBlock(BlockWrapper wrapper) {

        blockQueue.add(wrapper);
        lastBlock = wrapper.getBlock();

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                lastBlock.getNumber());
    }

    /**
     * Returns the last block in the queue. If the queue is empty,
     * this will return the last block added to the blockchain.
     *
     * @return The last known block this client on the network
     * and will never return <code>null</code> as there is
     * always the Genesis block at the start of the chain.
     */
    public Block getLastBlock() {
        if (blockQueue.isEmpty())
            return blockchain.getBestBlock();
        return lastBlock;
    }

    /**
     * Reset the queue of hashes of blocks to be retrieved
     * and add the best hash to the top of the queue
     *
     * @param hash - the best hash
     */
    public void setBestHash(byte[] hash) {
        hashStore.addFirst(hash);
        this.bestHash = hash;
    }

    /**
     * Returns the last added hash to the queue representing
     * the latest known block on the network
     *
     * @return The best hash on the network known to the client
     */
    public byte[] getBestHash() {
        return bestHash;
    }

    public void addHash(byte[] hash) {
        hashStore.addFirst(hash);
        if (logger.isTraceEnabled()) logger.trace(
                "Adding hash to a hashQueue: [{}], hash queue size: {} ",
                Hex.toHexString(hash).substring(0, 6),
                hashStore.size()
        );
    }

    public void addHashes(List<byte[]> hashes) {
        if(hashes.isEmpty()) {
            return;
        }

        this.bestHash = hashes.listIterator(hashes.size()).previous();
        List<byte[]> filtered = blockQueue.filterExisting(hashes);
        hashStore.addFirstBatch(filtered);

        if(logger.isDebugEnabled())
            logger.debug("{} hashes filtered out, {} added", hashes.size() - filtered.size(), filtered.size());
    }

    public void addNewBlockHashes(List<byte[]> hashes) {
        List<byte[]> notInQueue = blockQueue.filterExisting(hashes);

        List<byte[]> notInChain = new ArrayList<>();
        for (byte[] hash : notInQueue) {
            if (blockchain.isBlockExist(hash)) {
                notInChain.add(hash);
            }
        }

        hashStore.addBatch(notInChain);
    }

    public void returnHashes(List<ByteArrayWrapper> hashes) {

        if (hashes.isEmpty()) return;

        logger.info("Hashes remained uncovered: hashes.size: [{}]", hashes.size());

        ListIterator iterator = hashes.listIterator(hashes.size());
        while (iterator.hasPrevious()) {

            byte[] hash = ((ByteArrayWrapper) iterator.previous()).getData();

            if (logger.isDebugEnabled())
                logger.debug("Return hash: [{}]", Hex.toHexString(hash));
            hashStore.addFirst(hash);
        }
    }

    public void addNewBlockHash(byte[] hash) {
        hashStore.add(hash);
    }

    /**
     * Return a list of hashes from blocks that still need to be downloaded.
     *
     * @return A list of hashes for which blocks need to be retrieved.
     */
    public List<byte[]> getHashes() {
        return hashStore.pollBatch(CONFIG.maxBlocksAsk());
    }

    // a bit ugly but really gives
    // good result
    public void logHashQueueSize() {
        logger.info("Block hashes list size: [{}]", hashStore.size());
    }

    /**
     * Returns the current number of blocks in the queue
     *
     * @return the current number of blocks in the queue
     */
    public int size() {
        return blockQueue.size();
    }

    public boolean isHashesEmpty() {
        return hashStore.isEmpty();
    }

    public void clearHashes() {
        hashStore.clear();
    }

    public void clear() {
        this.hashStore.clear();
        this.blockQueue.clear();
    }

    public HashStore getHashStore() {
        return hashStore;
    }

    //TODO we need more robust solution for this check
    public boolean hasSolidBlocks() {
        BlockWrapper wrapper = blockQueue.peek();
        return wrapper != null && wrapper.isSolidBlock();
    }
}
