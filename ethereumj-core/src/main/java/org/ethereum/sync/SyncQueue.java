package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.*;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.eth.handler.Eth62;
import org.ethereum.net.server.Channel;
import org.ethereum.sync.listener.CompositeSyncListener;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
    private static final int HEADER_QUEUE_LIMIT = 20000;

    /**
     * Queue with blocks to be validated and added to the blockchain
     */
    private BlockQueue blockQueue = new BlockQueueMem();

    private final ReentrantLock headersLock = new ReentrantLock();
    private final Condition headersNotEmpty = headersLock.newCondition();

    private final ReentrantLock blocksLock = new ReentrantLock();
    private final Condition blocksAdded = blocksLock.newCondition();

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

    @Autowired
    SyncPool pool;

    private SyncQueueIfc syncQueueNew;

    private CountDownLatch receivedHeadersLatch = new CountDownLatch(0);
    private CountDownLatch receivedBlocksLatch = new CountDownLatch(0);

    /**
     * Loads HashStore and BlockQueue from disk,
     * starts {@link #produceQueue()} thread
     */
    public void init() {

        logger.info("Start loading sync queue");

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

        try {
            Thread.sleep(5000); // TODO !!!!!!!!!!!!!!!!!!!!!! blockchain is not initialized here
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        syncQueueNew = new SyncQueueImpl(blockchain);

        new Thread(new Runnable() {
            @Override
            public void run() {
                headerRetrieveLoop();
            }
        }, "NewSyncThreadHeaders").start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                blockRetrieveLoop();
//            }
//        }, "NewSyncThreadBlocks").start();
    }

    private void headerRetrieveLoop() {
        while(true) {
            try {

                if (syncQueueNew.getHeadersCount() < HEADER_QUEUE_LIMIT) {
                    Channel any = pool.getAnyIdle();

                    if (any != null) {
                        Eth62 eth = (Eth62) any.getEthHandler();

                        SyncQueueIfc.HeadersRequest hReq = syncQueueNew.requestHeaders();
                        eth.sendGetBlockHeaders(hReq.getStart(), hReq.getCount(), hReq.isReverse());
                    }
                }
                receivedHeadersLatch = new CountDownLatch(1);
                receivedHeadersLatch.await(2000, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                logger.error("Unexpected: ", e);
            }
        }
    }

    private void blockRetrieveLoop() {
        while(true) {
            try {

                if (blockQueue.size() < BLOCK_QUEUE_LIMIT) {
                    SyncQueueIfc.BlocksRequest bReq = syncQueueNew.requestBlocks(1000);
                    int reqBlocksCounter = 0;
                    for (SyncQueueIfc.BlocksRequest blocksRequest : bReq.split(100)) {
                        Channel any = pool.getAnyIdle();
                        if (any == null) break;
                        Eth62 eth = (Eth62) any.getEthHandler();
                        eth.sendGetBlockBodies(blocksRequest.getBlockHeaders());
                        reqBlocksCounter ++;
                    }
                    receivedBlocksLatch = new CountDownLatch(reqBlocksCounter);
                }

                receivedBlocksLatch.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Unexpected: ", e);
            }
        }
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        while (true) {

            BlockWrapper wrapper = null;
            try {

                wrapper = blockQueue.take();

                logger.debug("BlockQueue size: {}, headers queue size: {}", blockQueue.size(), syncQueueNew.getHeadersCount());
                ImportResult importResult = blockchain.tryToConnect(wrapper.getBlock());

                if (importResult == IMPORTED_BEST)
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (longSyncDone && (importResult == IMPORTED_BEST || importResult == IMPORTED_NOT_BEST)) {
                    if (logger.isDebugEnabled()) logger.debug("Block dump: " + Hex.toHexString(wrapper.getBlock().getEncoded()));
                }

                // In case we don't have a parent on the chain
                // return the try and wait for more blocks to come.
                if (importResult == NO_PARENT) {
                    logger.error("No parent on the chain for block.number: {} block.hash: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash());
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

        List<Block> newBlocks = syncQueueNew.addBlocks(blocks);

        List<BlockWrapper> wrappers = new ArrayList<>();
        for (Block b : newBlocks) {
            wrappers.add(new BlockWrapper(b, nodeId));
        }

        blockQueue.addOrReplaceAll(wrappers);

        receivedBlocksLatch.countDown();

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

        syncQueueNew.addHeaders(singletonList(new BlockHeaderWrapper(block.getHeader(), nodeId)));
        List<Block> newBlocks = syncQueueNew.addBlocks(singletonList(block));

        List<BlockWrapper> wrappers = new ArrayList<>();
        for (Block b : newBlocks) {
            BlockWrapper wrapper = new BlockWrapper(block, true, nodeId);
            wrapper.setReceivedAt(System.currentTimeMillis());
            wrappers.add(new BlockWrapper(b, nodeId));
        }

        blockQueue.addOrReplaceAll(wrappers);

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                block.getNumber());

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

        syncQueueNew.addHeaders(wrappers);

        receivedHeadersLatch.countDown();

        logger.debug("{} headers added", headers.size());

        return true;
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
}
