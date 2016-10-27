package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.core.*;
import org.ethereum.net.server.Channel;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
public abstract class BlockDownloader {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private int blockQueueLimit = 2000;
    private int headerQueueLimit = 10000;

    /**
     * Queue with validated blocks to be added to the blockchain
     */
    private BlockingQueue<BlockWrapper> blockQueue = new LinkedBlockingQueue<>();

    private BlockHeaderValidator headerValidator;

    private SyncPool pool;

    private SyncQueueIfc syncQueue;

    private CountDownLatch receivedHeadersLatch = new CountDownLatch(0);
    private CountDownLatch receivedBlocksLatch = new CountDownLatch(0);

    private Thread getHeadersThread;
    private Thread getBodiesThread;

    public BlockDownloader(BlockHeaderValidator headerValidator) {
        this.headerValidator = headerValidator;
    }

    protected abstract void pushBlocks(List<BlockWrapper> blockWrappers);

    public void init(SyncQueueIfc syncQueue, final SyncPool pool) {
        this.syncQueue = syncQueue;
        this.pool = pool;

        logger.info("Initializing SyncManager.");

        getHeadersThread = new Thread(new Runnable() {
            @Override
            public void run() {
                headerRetrieveLoop();
            }
        }, "SyncThreadHeaders");
        getHeadersThread.start();

        getBodiesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                blockRetrieveLoop();
            }
        }, "SyncThreadBlocks");
        getBodiesThread.start();
    }

    public void setHeaderQueueLimit(int headerQueueLimit) {
        this.headerQueueLimit = headerQueueLimit;
    }

    public void setBlockQueueLimit(int blockQueueLimit) {
        this.blockQueueLimit = blockQueueLimit;
    }

    private void headerRetrieveLoop() {
        while(!Thread.currentThread().isInterrupted()) {
            try {

                if (syncQueue.getHeadersCount() < headerQueueLimit) {
                    final Channel any = pool.getAnyIdle();

                    if (any != null) {
                        SyncQueueIfc.HeadersRequest hReq = syncQueue.requestHeaders();
                        logger.debug("headerRetrieveLoop: request headers (" + hReq.getStart() + ") from " + any.getNode());
                        ListenableFuture<List<BlockHeader>> futureHeaders =
                                any.getEthHandler().sendGetBlockHeaders(hReq.getStart(), hReq.getCount(), hReq.isReverse());
                        Futures.addCallback(futureHeaders, new FutureCallback<List<BlockHeader>>() {
                            @Override
                            public void onSuccess(List<BlockHeader> result) {
                                if (!validateAndAddHeaders(result, any.getNodeId())) {
                                    onFailure(new RuntimeException("Received headers validation failed"));
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                logger.debug("Error receiving headers. Dropping the peer.", t);
                                any.getEthHandler().dropConnection();
                            }
                        });
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
        class BlocksCallback implements FutureCallback<List<Block>> {
            private Channel peer;

            public BlocksCallback(Channel peer) {
                this.peer = peer;
            }

            @Override
            public void onSuccess(List<Block> result) {
                addBlocks(result, peer.getNodeId());
            }

            @Override
            public void onFailure(Throwable t) {
                logger.debug("Error receiving Blocks. Dropping the peer.", t);
                peer.getEthHandler().dropConnection();
            }
        }

        while(!Thread.currentThread().isInterrupted()) {
            try {

                if (blockQueue.size() < blockQueueLimit) {
                    SyncQueueIfc.BlocksRequest bReq = syncQueue.requestBlocks(1000);

                    if (bReq.getBlockHeaders().size() <= 3) {
                        // new blocks are better to request from the header senders first
                        // to get more chances to receive block body promptly
                        for (BlockHeaderWrapper blockHeaderWrapper : bReq.getBlockHeaders()) {
                            Channel channel = pool.getByNodeId(blockHeaderWrapper.getNodeId());
                            if (channel != null) {
                                ListenableFuture<List<Block>> futureBlocks =
                                        channel.getEthHandler().sendGetBlockBodies(singletonList(blockHeaderWrapper));
                                Futures.addCallback(futureBlocks, new BlocksCallback(channel));
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
                            ListenableFuture<List<Block>> futureBlocks =
                                    any.getEthHandler().sendGetBlockBodies(blocksRequest.getBlockHeaders());
                            Futures.addCallback(futureBlocks, new BlocksCallback(any));
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
     * Adds a list of blocks to the queue
     *
     * @param blocks block list received from remote peer and be added to the queue
     * @param nodeId nodeId of remote peer which these blocks are received from
     */
    private void addBlocks(List<Block> blocks, byte[] nodeId) {

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

            pushBlocks(wrappers);
        }

        receivedBlocksLatch.countDown();

        if (logger.isDebugEnabled()) logger.debug(
                "Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                blocks.get(blocks.size() - 1).getNumber()
        );
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
    private boolean validateAndAddHeaders(List<BlockHeader> headers, byte[] nodeId) {

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
    protected boolean isValid(BlockHeader header) {

        if (!headerValidator.validate(header)) {

            headerValidator.logErrors(logger);
            return false;
        }

        return true;
    }

    public boolean isSyncDone() {
        return false;
    }

    public void close() {
        pool.close();
        try {
            if (getHeadersThread != null) getHeadersThread.interrupt();
            if (getBodiesThread != null) getBodiesThread.interrupt();
        } catch (Exception e) {
            logger.warn("Problems closing SyncManager", e);
        }
    }

}
