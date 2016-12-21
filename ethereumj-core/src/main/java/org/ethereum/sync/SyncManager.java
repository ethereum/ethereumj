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
import org.ethereum.util.Functional;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.ethereum.core.ImportResult.*;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager extends BlockDownloader {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static AtomicLong blockQueueByteSize = new AtomicLong(0);
    private final static long BLOCK_BYTES_LIMIT = 32 * 1024 * 1024;
    private final static int BLOCK_BYTES_ADDON = 4;

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
            blockQueueByteSize.addAndGet(blockWrapper.getEncoded().length + BLOCK_BYTES_ADDON);
            blockQueue.add(blockWrapper);
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

    ChannelManager channelManager;

    private SystemProperties config;

    private SyncPool pool;

    private SyncQueueImpl syncQueue;

    private Thread syncQueueThread;

    private long lastKnownBlockNumber = 0;
    private boolean syncDone = false;
    private EthereumListener.SyncState syncDoneType = EthereumListener.SyncState.COMPLETE;
    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public SyncManager(final SystemProperties config, BlockHeaderValidator validator) {
        super(validator);
        this.config = config;
    }

    public void init(final ChannelManager channelManager, final SyncPool pool) {
        this.pool = pool;
        this.channelManager = channelManager;

        logExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                logger.info("Sync state: " + getSyncStatus());
            }
        }, 10, 10, TimeUnit.SECONDS);

        if (!config.isSyncEnabled()) {
            logger.info("Sync Manager: OFF");
            return;
        }
        logger.info("Sync Manager: ON");

        logger.info("Initializing SyncManager.");
        pool.init(channelManager);

        if (config.isFastSyncEnabled()) {
            fastSyncManager.init();
        } else {
            initRegularSync(EthereumListener.SyncState.COMPLETE);
        }
    }

    public void initRegularSync(EthereumListener.SyncState syncDoneType) {
        logger.info("Initializing SyncManager regular sync.");
        this.syncDoneType = syncDoneType;

        syncQueue = new SyncQueueImpl(blockchain);
        super.init(syncQueue, pool);

        Runnable queueProducer = new Runnable(){

            @Override
            public void run() {
                produceQueue();
            }
        };

        syncQueueThread = new Thread (queueProducer, "SyncQueueThread");
        syncQueueThread.start();
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
        exec1.pushAll(blockWrappers);
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueFreeSize() {
        int blockQueueSize = blockQueue.size();
        long blockByteSize = blockQueueByteSize.get();
        int availableBlockSpace = Math.max(0, getBlockQueueLimit() - blockQueueSize);
        long availableBytesSpace = Math.max(0, BLOCK_BYTES_LIMIT - blockByteSize);

        int bytesSpaceInBlocks;
        if (blockByteSize == 0 || blockQueueSize == 0) {
            bytesSpaceInBlocks = Integer.MAX_VALUE;
        } else {
            bytesSpaceInBlocks = (int) Math.floor(availableBytesSpace / (blockQueueByteSize.get() / blockQueue.size()));
        }

        return Math.min(bytesSpaceInBlocks, availableBlockSpace);
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

                wrapper = blockQueue.take();
                blockQueueByteSize.addAndGet(-wrapper.getEncoded().length - BLOCK_BYTES_ADDON);

                logger.debug("BlockQueue size: {}, headers queue size: {}", blockQueue.size(), syncQueue.getHeadersCount());

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
                        syncDone = true;
                        channelManager.onSyncDone(true);
                        compositeEthereumListener.onSyncDone(syncDoneType);
                    }
                }

                if (importResult == IMPORTED_NOT_BEST)
                    logger.info("Success importing NOT_BEST: block.number: {}, block.hash: {}, tx.size: {}, time: {}",
                            wrapper.getNumber(), wrapper.getBlock().getShortHash(),
                            wrapper.getBlock().getTransactionsList().size(), ts);

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
            logExecutor.shutdown();
            pool.close();
            if (syncQueueThread != null) syncQueueThread.interrupt();
            if (config.isFastSyncEnabled()) fastSyncManager.close();
        } catch (Exception e) {
            logger.warn("Problems closing SyncManager", e);
        }
        super.close();
    }
}
