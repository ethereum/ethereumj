package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.ExecutorPipeline;
import org.ethereum.util.Functional;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.ethereum.core.ImportResult.*;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");


    private static final int BLOCK_QUEUE_LIMIT = 20000;
    private static final int HEADER_QUEUE_LIMIT = 20000;

    // Transaction.getSender() is quite heavy operation so we are prefetching this value on several threads
    // to unload the main block importing cycle
    private ExecutorPipeline<BlockWrapper,BlockWrapper> exec1 = new ExecutorPipeline<>
            (4, 1000, true, new Functional.Function<BlockWrapper,BlockWrapper>() {
                public BlockWrapper apply(BlockWrapper blockWrapper) {
                    for (Transaction tx : blockWrapper.getBlock().getTransactionsList()) {
                        tx.getSender();
                    }
                    return blockWrapper;
                }
            }, new Functional.Consumer<Throwable>() {
                public void accept(Throwable throwable) {
                    logger.error("Unexpected exception: ", throwable);
                }
            });

    private ExecutorPipeline<BlockWrapper, Void> exec2 = exec1.add(1, 1, new Functional.Consumer<BlockWrapper>() {
        @Override
        public void accept(BlockWrapper blockWrapper) {
            blockQueue.add(blockWrapper);
        }
    });

    /**
     * Queue with validated blocks to be added to the blockchain
     */
    private BlockingQueue<BlockWrapper> blockQueue = new LinkedBlockingQueue<>();

    private long lastKnownBlockNumber = 0;
    private boolean syncDone = false;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private BlockHeaderValidator headerValidator;

    @Autowired
    private CompositeEthereumListener compositeEthereumListener;

    @Autowired
    EthereumListener ethereumListener;

    ChannelManager channelManager;

    private SystemProperties config;

    private SyncPool pool;

    private SyncQueueIfc syncQueue;

    private CountDownLatch receivedHeadersLatch = new CountDownLatch(0);
    private CountDownLatch receivedBlocksLatch = new CountDownLatch(0);

    private Thread syncQueueThread;
    private Thread getHeadersThread;
    private Thread getBodiesThread;
    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();

    public SyncManager() {
    }

    @Autowired
    public SyncManager(final SystemProperties config) {
        this.config = config;
    }

    public void init(final ChannelManager channelManager, final SyncPool pool) {
        this.pool = pool;
        this.channelManager = channelManager;
        if (!config.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }
        logger.info("Sync Manager: ON");

        logger.info("Initializing SyncManager.");
        pool.init(channelManager);

        Runnable queueProducer = new Runnable(){

            @Override
            public void run() {
                produceQueue();
            }
        };

        syncQueueThread = new Thread (queueProducer, "SyncQueueThread");
        syncQueueThread.start();

        syncQueue = new SyncQueueImpl(blockchain);

        getHeadersThread = new Thread(new Runnable() {
            @Override
            public void run() {
                headerRetrieveLoop();
            }
        }, "NewSyncThreadHeaders");
        getHeadersThread.start();

        getBodiesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                blockRetrieveLoop();
            }
        }, "NewSyncThreadBlocks");
        getBodiesThread.start();

        if (logger.isInfoEnabled()) {
            startLogWorker();
        }
    }

    private void headerRetrieveLoop() {
        while(!Thread.currentThread().isInterrupted()) {
            try {

                if (syncQueue.getHeadersCount() < HEADER_QUEUE_LIMIT) {
                    Channel any = pool.getAnyIdle();

                    if (any != null) {
                        SyncQueueIfc.HeadersRequest hReq = syncQueue.requestHeaders();
                        logger.debug("headerRetrieveLoop: request headers (" + hReq.getStart() + ") from " + any.getNode());
                        any.getEthHandler().sendGetBlockHeaders(hReq.getStart(), hReq.getCount(), hReq.isReverse());
                    } else {
                        logger.debug("headerRetrieveLoop: No IDLE peers found");
                    }
                } else {
                    logger.debug("headerRetrieveLoop: HeaderQueue is full");
                }
                receivedHeadersLatch = new CountDownLatch(1);
                receivedHeadersLatch.await(isSyncDone() ? 10000 : 2000, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                logger.error("Unexpected: ", e);
            }
        }
    }

    private void blockRetrieveLoop() {
        while(!Thread.currentThread().isInterrupted()) {
            try {

                if (blockQueue.size() < BLOCK_QUEUE_LIMIT) {
                    SyncQueueIfc.BlocksRequest bReq = syncQueue.requestBlocks(1000);

                    if (bReq.getBlockHeaders().size() <= 3) {
                        // new blocks are better to request from the header senders first
                        // to get more chances to receive block body promptly
                        for (BlockHeaderWrapper blockHeaderWrapper : bReq.getBlockHeaders()) {
                            Channel channel = pool.getByNodeId(blockHeaderWrapper.getNodeId());
                            if (channel != null && channel.isIdle()) {
                                channel.getEthHandler().sendGetBlockBodies(singletonList(blockHeaderWrapper));
                            }
                        }
                    }

                    int reqBlocksCounter = 0;
                    for (SyncQueueIfc.BlocksRequest blocksRequest : bReq.split(100)) {
                        Channel any = pool.getAnyIdle();
                        if (any == null) {
                            logger.debug("blockRetrieveLoop: No IDLE peers found");
                            break;
                        } else {
                            logger.debug("blockRetrieveLoop: Requesting " + blocksRequest.getBlockHeaders().size() + " blocks from " + any.getNode());
                            any.getEthHandler().sendGetBlockBodies(blocksRequest.getBlockHeaders());
                            reqBlocksCounter++;
                        }
                    }
                    receivedBlocksLatch = new CountDownLatch(max(reqBlocksCounter, 1));
                } else {
                    logger.debug("blockRetrieveLoop: BlockQueue is full");
                    receivedBlocksLatch = new CountDownLatch(1);
                }

                receivedBlocksLatch.await(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                logger.error("Unexpected: ", e);
            }
        }
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        while (!Thread.currentThread().isInterrupted()) {

            BlockWrapper wrapper = null;
            try {

                wrapper = blockQueue.take();

                logger.debug("BlockQueue size: {}, headers queue size: {}", blockQueue.size(), syncQueue.getHeadersCount());
                ImportResult importResult = blockchain.tryToConnect(wrapper.getBlock());

                if (importResult == IMPORTED_BEST) {
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                    if (wrapper.isNewBlock() && !syncDone) {
                        syncDone = true;
                        channelManager.onSyncDone(true);
                        compositeEthereumListener.onSyncDone();
                    }
                }

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {} ",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size());

                if (syncDone && (importResult == IMPORTED_BEST || importResult == IMPORTED_NOT_BEST)) {
                    if (logger.isDebugEnabled()) logger.debug("Block dump: " + Hex.toHexString(wrapper.getBlock().getEncoded()));
                    // Propagate block to the net after successful import asynchronously
                    if (wrapper.isNewBlock()) channelManager.onNewForeignBlock(wrapper);
                }

                // In case we don't have a parent on the chain
                // return the try and wait for more blocks to come.
                if (importResult == NO_PARENT) {
                    logger.error("No parent on the chain for block.number: {} block.hash: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash());
                }

            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                if (wrapper != null) {
                    logger.error("Error processing block {}: ", wrapper.getBlock().getShortDescr(), e);
                    logger.error("Block dump: {}", Hex.toHexString(wrapper.getBlock().getEncoded()));
                } else {
                    logger.error("Error processing unknown block", e);
                }
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

        synchronized (this) {
            logger.debug("Adding new " + blocks.size() + " blocks to sync queue: " +
                    blocks.get(0).getShortDescr() + " ... " + blocks.get(blocks.size() - 1).getShortDescr());

            List<Block> newBlocks = syncQueue.addBlocks(blocks);

            List<BlockWrapper> wrappers = new ArrayList<>();
            for (Block b : newBlocks) {
                wrappers.add(new BlockWrapper(b, nodeId));
            }


            logger.debug("Pushing " + wrappers.size() + " blocks to import queue: " + (wrappers.isEmpty() ? "" :
                    wrappers.get(0).getBlock().getShortDescr() + " ... " + wrappers.get(wrappers.size() - 1).getBlock().getShortDescr()));

            exec1.pushAll(wrappers);
        }

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

        lastKnownBlockNumber = block.getNumber();

        logger.debug("Adding new block to sync queue: " + block.getShortDescr());
        syncQueue.addHeaders(singletonList(new BlockHeaderWrapper(block.getHeader(), nodeId)));

        synchronized (this) {
            List<Block> newBlocks = syncQueue.addBlocks(singletonList(block));

            List<BlockWrapper> wrappers = new ArrayList<>();
            for (Block b : newBlocks) {
                boolean newBlock = Arrays.equals(block.getHash(), b.getHash());
                BlockWrapper wrapper = new BlockWrapper(b, newBlock, nodeId);
                wrapper.setReceivedAt(System.currentTimeMillis());
                wrappers.add(wrapper);
            }

            logger.debug("Pushing " + wrappers.size() + " new blocks to import queue: " + (wrappers.isEmpty() ? "" :
                    wrappers.get(0).getBlock().getShortDescr() + " ... " + wrappers.get(wrappers.size() - 1).getBlock().getShortDescr()));
            exec1.pushAll(wrappers);
        }

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

        if (headers.isEmpty()) return true;

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

        syncQueue.addHeaders(wrappers);

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

    public boolean isSyncDone() {
        return syncDone;
    }

    public long getLastKnownBlockNumber() {
        return lastKnownBlockNumber;
    }

    private void startLogWorker() {
        logExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    pool.logActivePeers();
                    logger.info("\n");
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Exception in log worker", t);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void close() {
        pool.close();
        try {
            exec1.shutdown();
            if (getHeadersThread != null) getHeadersThread.interrupt();
            if (getBodiesThread != null) getBodiesThread.interrupt();
            if (syncQueueThread != null) syncQueueThread.interrupt();
            logExecutor.shutdown();
        } catch (Exception e) {
            logger.warn("Problems closing SyncManager", e);
        }
    }
}
