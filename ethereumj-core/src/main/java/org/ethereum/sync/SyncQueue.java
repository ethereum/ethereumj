package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.*;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Thread.sleep;
import static org.ethereum.core.ImportResult.IMPORTED_NOT_BEST;
import static org.ethereum.core.ImportResult.NO_PARENT;
import static org.ethereum.core.ImportResult.IMPORTED_BEST;

/**
 * The processing queue for blocks to be validated and added to the blockchain.
 * This class also maintains the list of hashes from the peer with the heaviest sub-tree.
 * Based on these hashes, blocks are added to the queue.
 *
 * @author Roman Mandeleil
 * @author Mikhail Kalinin
 * @since 27.07.2014
 */
@Component
public class SyncQueue {

    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    private static final int SCAN_BLOCKS_LIMIT = 1000;
    private static final int BLOCK_QUEUE_LIMIT = 20000;

    /**
     * Store holding a list of block headers of the heaviest chain on the network,
     * for which this client doesn't have the blocks yet
     */
    private HeaderStore headerStore;

    /**
     * Queue with blocks to be validated and added to the blockchain
     */
    private BlockQueue blockQueue;

    public boolean noParent = false;

    @Autowired
    SystemProperties config;

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

        headerStore = new HeaderStoreMem();
        blockQueue = new BlockQueueMem();

        headerStore.open();
        blockQueue.open();

        if (!config.isSyncEnabled()) {
            return;
        }

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
                    noParent = true;
                    sleep(2000);
                } else {
                    noParent = false;
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

                if (importResult == IMPORTED_BEST || importResult == IMPORTED_NOT_BEST) {
                    if (logger.isDebugEnabled()) logger.debug(Hex.toHexString(wrapper.getBlock().getEncoded()));
                }

            } catch (Throwable e) {
                logger.error("Error: {} ", e);
            }

        }
    }

    /**
     * Adds a list of blocks to the queue
     *
     * @param blocks block list received from remote peer and be added to the queue
     * @param nodeId nodeId of remote peer which these blocks are received from
     */
    public void addList(List<Block> blocks, byte[] nodeId) {

        if (blocks.isEmpty()) {
            return;
        }

        List<BlockWrapper> wrappers = new ArrayList<>(blocks.size());
        for (Block b : blocks) {
            wrappers.add(new BlockWrapper(b, nodeId));
        }

        blockQueue.addOrReplaceAll(wrappers);

        if (logger.isDebugEnabled()) logger.debug(
                "Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                blocks.get(blocks.size() - 1).getNumber()
        );
    }

    /**
     * Adds NEW block to the queue
     *
     * @param block new block
     * @param nodeId nodeId of the remote peer which this block is received from
     */
    public void addNew(Block block, byte[] nodeId) {

        // run basic checks
        if (!isValid(block.getHeader())) {
            syncManager.reportBadAction(nodeId);
            return;
        }

        BlockWrapper wrapper = new BlockWrapper(block, true, nodeId);
        wrapper.setReceivedAt(System.currentTimeMillis());

        blockQueue.addOrReplace(wrapper);

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                wrapper.getNumber());
    }

    /**
     * Adds list of headers received from remote host <br>
     * Runs header validation before addition <br>
     * It also won't add headers of those blocks which are already presented in the queue
     *
     * @param headers list of headers got from remote host
     * @param nodeId remote host nodeId
     */
    public void addAndValidateHeaders(List<BlockHeader> headers, byte[] nodeId) {
        List<BlockHeader> filtered = blockQueue.filterExistingHeaders(headers);

        for (BlockHeader header : headers) {

            if (!isValid(header)) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid header RLP: {}", Hex.toHexString(header.getEncoded()));
                }

                syncManager.reportBadAction(nodeId);
                return;
            }

        }

        headerStore.addBatch(headers);

        if (logger.isDebugEnabled())
            logger.debug("{} headers filtered out, {} added", headers.size() - filtered.size(), filtered.size());
    }

    /**
     * Adds headers previously taken from the store <br>
     * Doesn't run any validations and checks
     *
     * @param headers list of headers
     */
    public void returnHeaders(List<BlockHeader> headers) {
        headerStore.addBatch(headers);
    }

    /**
     * Returns list of headers for blocks required to be downloaded
     *
     * @return list of headers
     */
    public List<BlockHeader> pollHeaders() {
        return headerStore.pollBatch(config.maxBlocksAsk());
    }

    public void logHeadersSize() {
        logger.debug("Headers list size: [{}]", headerStore.size());
    }

    public boolean isHeadersEmpty() {
        return headerStore.isEmpty();
    }

    public boolean isBlocksEmpty() {
        return blockQueue.isEmpty();
    }

    public boolean isMoreBlocksNeeded() {
        int blockCount = headerStore.size() + blockQueue.size();
        return blockCount < BLOCK_QUEUE_LIMIT;
    }

    public void clearHeaders() {
        if (!headerStore.isEmpty())
            headerStore.clear();
    }

    public int headerStoreSize() {
        return headerStore.size();
    }

    /**
     * Scans {@link #SCAN_BLOCKS_LIMIT} first blocks in the queue
     * and removes blocks sent by given peer
     *
     * @param nodeId peer's node id
     */
    public void dropBlocks(byte[] nodeId) {
        blockQueue.drop(nodeId, SCAN_BLOCKS_LIMIT);
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
     * Checks if block exists in the queue
     *
     * @param hash block hash
     *
     * @return true if block exists, false otherwise
     */
    public boolean isBlockExist(byte[] hash) {
        return blockQueue.isBlockExist(hash);
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
