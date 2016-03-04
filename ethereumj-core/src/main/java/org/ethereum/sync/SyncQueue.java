package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.*;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sync.listener.CompositeSyncListener;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;
import static java.util.Collections.emptyList;
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

    private static final int BLOCK_QUEUE_LIMIT = 20000;

    /**
     * Store holding a list of block headers of the heaviest chain on the network,
     * for which this client doesn't have the blocks yet
     */
    private HeaderStore headerStore = new HeaderStoreMem();

    /**
     * Queue with blocks to be validated and added to the blockchain
     */
    private BlockQueue blockQueue = new BlockQueueMem();

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private boolean longSyncDone = false;

    @Autowired
    SystemProperties config;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private BlockHeaderValidator headerValidator;

    @Autowired
    private CompositeSyncListener compositeSyncListener;

    @Autowired
    private CompositeEthereumListener compositeEthereumListener;

    /**
     * Loads HashStore and BlockQueue from disk,
     * starts {@link #produceQueue()} thread
     */
    public void init() {

        logger.info("Start loading sync queue");

        headerStore.open();
        blockQueue.open();

        compositeEthereumListener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onLongSyncDone() {
                longSyncDone = true;
            }
            @Override
            public void onLongSyncStarted() {
                longSyncDone = false;
            }
        });

        Runnable queueProducer = new Runnable(){

            @Override
            public void run() {
                produceQueue();
            }
        };

        Thread t=new Thread (queueProducer, "SyncQueueThread");
        t.start();
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        while (1==1) {

            BlockWrapper wrapper = null;
            try {

                wrapper = blockQueue.take();

                logger.debug("BlockQueue size: {}", blockQueue.size());
                ImportResult importResult = blockchain.tryToConnect(wrapper.getBlock());

                // In case we don't have a parent on the chain
                // return the try and wait for more blocks to come.
                if (importResult == NO_PARENT) {
                    logger.debug("No parent on the chain for block.number: {} block.hash: {}", wrapper.getNumber(), wrapper.getBlock().getShortHash());

                    blockQueue.returnBlock(wrapper);
                    compositeSyncListener.onNoParent(wrapper);

                    if (!longSyncDone) {
                        sleep(100);
                    } else {
                        sleep(2000);
                    }
                }

                if (importResult == IMPORTED_BEST)
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (importResult == IMPORTED_BEST || importResult == IMPORTED_NOT_BEST) {
                    if (logger.isTraceEnabled()) logger.trace(Hex.toHexString(wrapper.getBlock().getEncoded()));
                }

            } catch (Throwable e) {
                logger.error("Error processing block {}: ", wrapper.getBlock().getShortDescr(), e);
                logger.error("Block dump: {}", Hex.toHexString(wrapper.getBlock().getEncoded()));
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
     *
     * @return true if block passed validations and was added to the queue,
     *         otherwise it returns false
     */
    public boolean validateAndAddNewBlock(Block block, byte[] nodeId) {

        // run basic checks
        if (!isValid(block.getHeader())) {
            return false;
        }

        BlockWrapper wrapper = new BlockWrapper(block, true, nodeId);
        wrapper.setReceivedAt(System.currentTimeMillis());

        blockQueue.addOrReplace(wrapper);

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                wrapper.getNumber());

        return true;
    }

    /**
     * Adds list of headers received from remote host <br>
     * Runs header validation before addition <br>
     * It also won't add headers of those blocks which are already presented in the queue
     *
     * @param headers list of headers got from remote host
     * @param nodeId remote host nodeId
     *
     * @return true if blocks passed validation and were added to the queue,
     *          otherwise it returns false
     */
    public boolean validateAndAddHeaders(List<BlockHeader> headers, byte[] nodeId) {

        List<BlockHeaderWrapper> wrappers = new ArrayList<>(headers.size());

        for (BlockHeader header : headers) {

            if (!isValid(header)) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Invalid header RLP: {}", Hex.toHexString(header.getEncoded()));
                }

                return false;
            }

            wrappers.add(new BlockHeaderWrapper(header, nodeId));
        }

        headerStore.addBatch(wrappers);
        fireHeadersNotEmpty();

        logger.debug("{} headers added", headers.size());

        return true;
    }

    /**
     * Adds headers previously taken from the store <br>
     * Doesn't run any validations and checks
     *
     * @param headers list of headers
     */
    public void returnHeaders(List<BlockHeaderWrapper> headers) {
        headerStore.addBatch(headers);
        fireHeadersNotEmpty();
    }

    /**
     * Returns list of headers for blocks required to be downloaded
     *
     * @return list of headers
     */
    public List<BlockHeaderWrapper> pollHeaders() {
        if (logger.isDebugEnabled() && !headerStore.isEmpty())
            logger.debug("Headers list size: {}", headerStore.size());
        return headerStore.pollBatch(config.maxBlocksAsk());
    }

    /**
     * Performs the same work as {@link #pollHeaders()} does. <br>
     * Blocks thread if header store is empty until new headers appears in the store
     *
     * @return list of headers
     */
    public List<BlockHeaderWrapper> takeHeaders() {

        takeLock.lock();
        try {
            List<BlockHeaderWrapper> headers;
            while ((headers = headerStore.pollBatch(config.maxBlocksAsk())).isEmpty()) {
                notEmpty.awaitUninterruptibly();
                if (longSyncDone) return emptyList();
            }
            if (logger.isDebugEnabled() && !headerStore.isEmpty())
                logger.debug("Headers list size: {}", headerStore.size());
            return headers;
        } finally {
            takeLock.unlock();
        }
    }

    public boolean isHeadersEmpty() {
        return headerStore.isEmpty();
    }

    public boolean isBlocksEmpty() {
        return blockQueue.isEmpty();
    }

    public boolean isLimitExceeded() {
        return expectedBlocksCount() > BLOCK_QUEUE_LIMIT;
    }

    public boolean isMoreBlocksNeeded() {
        return expectedBlocksCount() < BLOCK_QUEUE_LIMIT;
    }

    private int expectedBlocksCount() {
        return headerStore.size() + blockQueue.size();
    }

    public int headerStoreSize() {
        return headerStore.size();
    }

    /**
     * Removes blocks sent by given peer
     *
     * @param nodeId peer's node id
     */
    public void dropBlocks(byte[] nodeId) {
        blockQueue.drop(nodeId, 0);
    }

    /**
     * Removes headers sent by given peer
     *
     * @param nodeId peer's node id
     */
    public void dropHeaders(byte[] nodeId) {
        headerStore.drop(nodeId);
    }

    /**
     * @return latest block in the queue
     */
    public BlockWrapper peekLastBlock() {
        return blockQueue.peekLast();
    }

    /**
     * @return latest block in the queue
     */
    public BlockWrapper peekFirstBlock() {
        return blockQueue.peek();
    }

    /**
     * Removes block if it's still on the queue
     *
     * @param block block to be removed
     */
    public void removeBlock(BlockWrapper block) {
        blockQueue.remove(block);
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

            headerValidator.logErrors(logger);
            return false;
        }

        return true;
    }

    private void fireHeadersNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            takeLock.unlock();
        }
    }
}
