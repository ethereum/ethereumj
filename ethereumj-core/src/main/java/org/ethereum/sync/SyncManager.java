/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.core.Blockchain;
import org.ethereum.facade.SyncStatus;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.ExecutorPipeline;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.DependentBlockHeaderRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.ethereum.core.ImportResult.*;
import static org.ethereum.util.Utils.longToTimePeriod;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager extends BlockDownloader {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    // Transaction.getSender() is quite heavy operation so we are prefetching this value on several threads
    // to unload the main block importing cycle
    private ExecutorPipeline<BlockWrapper,BlockWrapper> exec1 = new ExecutorPipeline<>
            (4, 1000, true, blockWrapper -> {
                for (Transaction tx : blockWrapper.getBlock().getTransactionsList()) {
                    tx.getSender();
                }
                return blockWrapper;
            }, throwable -> logger.error("Unexpected exception: ", throwable));

    private ExecutorPipeline<BlockWrapper, Void> exec2 = exec1.add(1, 1, new Consumer<BlockWrapper>() {
        @Override
        public void accept(BlockWrapper blockWrapper) {
            blockQueue.add(blockWrapper);
            estimateBlockSize(blockWrapper);
        }
    });

    /**
     * Queue with validated blocks to be added to the blockchain
     */
    private BlockingQueue<BlockWrapper> blockQueue = new LinkedBlockingQueue<>();

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private CompositeEthereumListener compositeEthereumListener;

    @Autowired
    private FastSyncManager fastSyncManager;

    @Autowired
    private DependentBlockHeaderRule parentHeaderValidator;

    ChannelManager channelManager;

    private SystemProperties config;

    private SyncPool pool;

    private SyncQueueImpl syncQueue;

    private Thread syncQueueThread;

    private long blockBytesLimit = 32 * 1024 * 1024;
    private long lastKnownBlockNumber = 0;
    private boolean syncDone = false;
    private AtomicLong importIdleTime = new AtomicLong();
    private long importStart;
    private EthereumListener.SyncState syncDoneType = EthereumListener.SyncState.COMPLETE;
    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();
    private LocalDateTime initRegularTime;

    private AtomicInteger blocksInMem = new AtomicInteger(0);

    public SyncManager() {
        super(null);
    }

    @Autowired
    public SyncManager(final SystemProperties config, BlockHeaderValidator validator) {
        super(validator);
        this.config = config;
        blockBytesLimit = config.blockQueueSize();
        setHeaderQueueLimit(config.headerQueueSize() / BlockHeader.MAX_HEADER_SIZE);
    }

    public void init(final ChannelManager channelManager, final SyncPool pool) {
        if (this.channelManager == null) {  // First init
            this.pool = pool;
            this.channelManager = channelManager;
            logExecutor.scheduleAtFixedRate(() -> {
                try {
                    logger.info("Sync state: " + getSyncStatus() +
                            (isSyncDone() || importStart == 0 ? "" : "; Import idle time " +
                            longToTimePeriod(importIdleTime.get()) + " of total " + longToTimePeriod(System.currentTimeMillis() - importStart)));
                } catch (Exception e) {
                    logger.error("Unexpected", e);
                }
            }, 10, 10, TimeUnit.SECONDS);
        }

        if (!config.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }
        logger.info("Sync Manager: ON");

        if (pool.getChannelManager() == null) {  // Never were on this stage of init
            logger.info("Initializing SyncManager.");
            pool.init(channelManager, blockchain);

            if (config.isFastSyncEnabled()) {
                fastSyncManager.init();
            } else {
                initRegularSync(EthereumListener.SyncState.COMPLETE);
            }
        }
    }

    void initRegularSync(EthereumListener.SyncState syncDoneType) {
        logger.info("Initializing SyncManager regular sync.");
        this.syncDoneType = syncDoneType;

        syncQueue = new SyncQueueImpl(blockchain)
                .withParentHeaderValidator(parentHeaderValidator);
        super.init(syncQueue, pool, "RegularSync");

        Runnable queueProducer = this::produceQueue;

        syncQueueThread = new Thread (queueProducer, "SyncQueueThread");
        syncQueueThread.start();

        if (config.makeDoneByTimeout() >= 0) {
            logger.info("Custom long sync done timeout set to {} second(s)", config.makeDoneByTimeout());
            this.initRegularTime = LocalDateTime.now();
            ScheduledExecutorService shortSyncAwait = Executors.newSingleThreadScheduledExecutor();
            shortSyncAwait.scheduleAtFixedRate(() -> {
                try {
                    if (LocalDateTime.now().minusSeconds(config.makeDoneByTimeout()).isAfter(initRegularTime) &&
                            getLastKnownBlockNumber() == blockchain.getBestBlock().getNumber()) {
                        logger.info("Sync done triggered by timeout");
                        makeSyncDone();
                        shortSyncAwait.shutdown();
                    } else if (syncDone) {
                        shortSyncAwait.shutdown();
                    }
                } catch (Exception e) {
                    logger.error("Unexpected", e);
                }
            }, 0, 2, TimeUnit.SECONDS);
        }
    }

    void setSyncDoneType(EthereumListener.SyncState syncDoneType) {
        this.syncDoneType = syncDoneType;
    }

    public SyncStatus getSyncStatus() {
        if (config.isFastSyncEnabled()) {
            SyncStatus syncStatus = fastSyncManager.getSyncState();
            if (syncStatus.getStage() == SyncStatus.SyncStage.Complete) {
                return getSyncStateImpl();
            } else {
                return new SyncStatus(syncStatus, blockchain.getBestBlock().getNumber(), getLastKnownBlockNumber());
            }
        } else {
            return getSyncStateImpl();
        }
    }

    private SyncStatus getSyncStateImpl() {
        if (!config.isSyncEnabled())
            return new SyncStatus(SyncStatus.SyncStage.Off, 0, 0, blockchain.getBestBlock().getNumber(),
                    blockchain.getBestBlock().getNumber());

        return new SyncStatus(isSyncDone() ? SyncStatus.SyncStage.Complete : SyncStatus.SyncStage.Regular,
                0, 0, blockchain.getBestBlock().getNumber(), getLastKnownBlockNumber());
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!exec1.isShutdown()) {
            exec1.pushAll(blockWrappers);
            blocksInMem.addAndGet(blockWrappers.size());
        }
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueFreeSize() {
        return getBlockQueueLimit();
    }

    @Override
    protected int getMaxHeadersInQueue() {
        if (getEstimatedBlockSize() == 0) {
            // accurately exploring the net
            if (syncQueue.getHeadersCount() < 2 * MAX_IN_REQUEST) {
                return 2 * MAX_IN_REQUEST;
            } else {
                return 0;
            }
        }

        int inMem = blocksInMem.get();
        int slotsLeft = Math.max(0, (int) (blockBytesLimit / getEstimatedBlockSize()) - inMem);

        if (slotsLeft + inMem < MAX_IN_REQUEST) {
            slotsLeft = MAX_IN_REQUEST;
        }

        // adding 2 * MAX_IN_REQUEST to overcome dark zone buffer
        return Math.min(slotsLeft + 2 * MAX_IN_REQUEST, getHeaderQueueLimit());
    }

    /**
     * Processing the queue adding blocks to the chain.
     */
    private void produceQueue() {

        DecimalFormat timeFormat = new DecimalFormat("0.000");
        timeFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));

        while (!Thread.currentThread().isInterrupted()) {

            BlockWrapper wrapper = null;
            try {

                long stale = !isSyncDone() && importStart > 0 && blockQueue.isEmpty() ? System.nanoTime() : 0;
                wrapper = blockQueue.take();

                blocksInMem.decrementAndGet();

                if (stale > 0) {
                    importIdleTime.addAndGet((System.nanoTime() - stale) / 1_000_000);
                }
                if (importStart == 0) importStart = System.currentTimeMillis();

                logger.debug("BlockQueue size: {}, headers queue size: {}, blocks in mem: {} (~{}mb)",
                        blockQueue.size(), syncQueue.getHeadersCount(), blocksInMem.get(),
                        blocksInMem.get() * getEstimatedBlockSize() / 1024 / 1024);

                long s = System.nanoTime();
                long sl;
                ImportResult importResult;
                synchronized (blockchain) {
                    sl = System.nanoTime();
                    importResult = blockchain.tryToConnect(wrapper.getBlock());
                }
                long f = System.nanoTime();
                long t = (f - s) / 1_000_000;
                String ts = timeFormat.format(t / 1000d) + "s";
                t = (sl - s) / 1_000_000;
                ts += t < 10 ? "" : " (lock: " + timeFormat.format(t / 1000d) + "s)";

                if (importResult == IMPORTED_BEST) {
                    logger.info("Success importing BEST: block.number: {}, block.hash: {}, tx.size: {}, time: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size(), ts);

                    if (wrapper.isNewBlock() && !syncDone) {
                        makeSyncDone();
                    }
                }

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {}, time: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size(), ts);

                if (syncDone && (importResult == IMPORTED_BEST || importResult == IMPORTED_NOT_BEST)) {
                    if (logger.isDebugEnabled()) logger.debug("Block dump: " + toHexString(wrapper.getBlock().getEncoded()));
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
                    logger.error("Block dump: {}", toHexString(wrapper.getBlock().getEncoded()));
                } else {
                    logger.error("Error processing unknown block", e);
                }
            }
        }
    }

    private synchronized void makeSyncDone() {
        if (syncDone) return;
        syncDone = true;
        channelManager.onSyncDone(true);
        compositeEthereumListener.onSyncDone(syncDoneType);
    }

    public CompletableFuture<Void> switchToShortSync() {
        final CompletableFuture<Void> syncDoneF = new CompletableFuture<>();
        if(!syncDone && config.isSyncEnabled()) {
            new Thread(() -> {
                while(!blockQueue.isEmpty() && !syncDone) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        syncDoneF.completeExceptionally(e);
                    }
                }
                makeSyncDone();
                syncDoneF.complete(null);
            }).start();
        } else {
            syncDoneF.complete(null);
        }

        return syncDoneF;
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

        if (syncQueue == null) return true;

        // run basic checks
        if (!isValid(block.getHeader())) {
            return false;
        }

        lastKnownBlockNumber = block.getNumber();

        // skip too distant blocks
        if (block.getNumber() > syncQueue.maxNum + MAX_IN_REQUEST * 2) {
            return true;
        }
        // skip if memory limit is already hit
        if ((blocksInMem.get() * getEstimatedBlockSize()) > blockBytesLimit) {
            return true;
        }

        logger.debug("Adding new block to sync queue: " + block.getShortDescr());
        SyncQueueIfc.ValidatedHeaders res = syncQueue.addHeadersAndValidate(
                singletonList(new BlockHeaderWrapper(block.getHeader(), nodeId)));
        dropIfValidationFailed(res);

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
            pushBlocks(wrappers);
        }

        logger.debug("Blocks waiting to be proceed:  queue.size: [{}] lastBlock.number: [{}]",
                blockQueue.size(),
                block.getNumber());

        return true;
    }

    public boolean isSyncDone() {
        return syncDone;
    }

    public boolean isFastSyncRunning() {
        return fastSyncManager.isFastSyncInProgress();
    }

    public long getLastKnownBlockNumber() {
        long ret = max(blockchain.getBestBlock().getNumber(), lastKnownBlockNumber);
        for (Channel channel : pool.getActivePeers()) {
            BlockIdentifier bestKnownBlock = channel.getEthHandler().getBestKnownBlock();
            if (bestKnownBlock != null) {
                ret = max(bestKnownBlock.getNumber(), ret);
            }
        }
        return ret;
    }

    public void close() {
        try {
            logger.info("Shutting down SyncManager");
            exec1.shutdown();
            exec1.join();
            logExecutor.shutdown();
            pool.close();
            if (syncQueueThread != null) {
                syncQueueThread.interrupt();
                syncQueueThread.join(10 * 1000);
            }
            if (config.isFastSyncEnabled()) fastSyncManager.close();
        } catch (Exception e) {
            logger.warn("Problems closing SyncManager", e);
        }
        super.close();
    }
}
