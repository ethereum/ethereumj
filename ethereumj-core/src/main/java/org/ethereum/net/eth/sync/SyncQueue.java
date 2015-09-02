package org.ethereum.net.eth.sync;

import org.ethereum.core.*;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.*;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.Functional;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
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
    private BlockQueue blockQueue;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private SyncManager syncManager;

    @Autowired
    private BlockHeaderValidator headerValidator;

    /**
     * Loads HashStore and BlockQueue from disk,
     * starts {@link #produceQueue()} thread
     */
    public void init() {

        logger.info("Start loading sync queue");

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
     *
     * @param blockList - the blocks received from a peer to be added to the queue
     * @param nodeId of the remote peer which these blocks are received from
     */
    public void addBlocks(List<Block> blockList, final byte[] nodeId) {

        // run basic checks
        for (Block b : blockList) {
            if (!isValid(b.getHeader())) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid block RLP: {}", Hex.toHexString(b.getEncoded()));
                }

                syncManager.reportInvalidBlock(nodeId);
                return;
            }
        }

        List<BlockWrapper> wrappers = CollectionUtils.collectList(blockList, new Functional.Function<Block, BlockWrapper>() {
            @Override
            public BlockWrapper apply(Block block) {
                return new BlockWrapper(block, nodeId);
            }
        });
        blockQueue.addAll(wrappers);

        if (logger.isDebugEnabled()) logger.debug(
                "Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                blockList.get(blockList.size() - 1).getNumber()
        );
    }

    /**
     * Addi NEW block to the queue
     *
     * @param block new block
     * @param nodeId nodeId of the remote peer which this block is received from
     */
    public void addNewBlock(Block block, byte[] nodeId) {

        // run basic checks
        if (!isValid(block.getHeader())) {
            syncManager.reportInvalidBlock(nodeId);
            return;
        }

        BlockWrapper wrapper = new BlockWrapper(block, true, nodeId);
        wrapper.setReceivedAt(System.currentTimeMillis());

        blockQueue.add(wrapper);

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                wrapper.getNumber());
    }

    /**
     * Adds hash to the beginning of HashStore queue
     *
     * @param hash hash to be added
     */
    public void addHash(byte[] hash) {
        hashStore.addFirst(hash);
        if (logger.isTraceEnabled()) logger.trace(
                "Adding hash to a hashQueue: [{}], hash queue size: {} ",
                Hex.toHexString(hash).substring(0, 6),
                hashStore.size()
        );
    }

    /**
     * Adds list of hashes to the end of HashStore queue. <br>
     * Sorts out those hashes which blocks are already added to BlockQueue
     *
     * @param hashes hashes
     */
    public void addHashesLast(List<byte[]> hashes) {
        List<byte[]> filtered = blockQueue.filterExisting(hashes);

        hashStore.addBatch(filtered);

        if(logger.isDebugEnabled())
            logger.debug("{} hashes filtered out, {} added", hashes.size() - filtered.size(), filtered.size());
    }

    /**
     * Adds list of hashes to the beginning of HashStore queue. <br>
     * Sorts out those hashes which blocks are already added to BlockQueue
     *
     * @param hashes hashes
     */
    public void addHashes(List<byte[]> hashes) {
        List<byte[]> filtered = blockQueue.filterExisting(hashes);
        hashStore.addFirstBatch(filtered);

        if (logger.isDebugEnabled())
            logger.debug("{} hashes filtered out, {} added", hashes.size() - filtered.size(), filtered.size());
    }

    /**
     * Adds hashes received in NEW_BLOCK_HASHES message. <br>
     * Excludes hashes representing already imported blocks,
     * hashes are added to the end of HashStore queue
     *
     * @param hashes list of hashes
     */
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

    /**
     * Puts back given hashes. <br>
     * Hashes are added to the beginning of queue
     *
     * @param hashes returning hashes
     */
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

    /**
     * Return a list of hashes from blocks that still need to be downloaded.
     *
     * @return A list of hashes for which blocks need to be retrieved.
     */
    public List<byte[]> pollHashes() {
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

    public void clear() {
        this.hashStore.clear();
        this.blockQueue.clear();
    }

    public boolean isHashesEmpty() {
        return hashStore.isEmpty();
    }

    public boolean isBlocksEmpty() {
        return blockQueue.isEmpty();
    }

    public void clearHashes() {
        hashStore.clear();
    }

    public int hashStoreSize() {
        return hashStore.size();
    }

    /**
     * Checks whether BlockQueue contains solid blocks or not. <br>
     * Block is assumed to be solid in two cases:
     * <ul>
     *     <li>it was downloading during main sync</li>
     *     <li>NEW block with exceeded solid timeout</li>
     * </ul>
     *
     * @see BlockWrapper#SOLID_BLOCK_DURATION_THRESHOLD
     *
     * @return true if queue contains solid blocks, false otherwise
     */
    public boolean hasSolidBlocks() {
        BlockWrapper wrapper = blockQueue.peek();
        return wrapper != null && wrapper.isSolidBlock();
    }

    /**
     * Runs checks against block's header. <br>
     * All these checks make sense before block is added to queue
     * in front of checks running by {@link BlockchainImpl#isValid(BlockHeader)}
     *
     * @param header block header
     * @return true if block is valid, false otherwise
     */
    private boolean isValid(BlockHeader header) {

        if (!headerValidator.validate(header)) {

            if (logger.isErrorEnabled())
                headerValidator.logErrors(logger);

            return false;
        }

        return true;
    }

}
