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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.net.eth.handler.Eth63;
import org.ethereum.net.server.Channel;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Scope("prototype")
public class ReceiptsDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final long REQUEST_TIMEOUT = 5 * 1000;
    private static final int MAX_IN_REQUEST = 100;
    private static final int MIN_IN_REQUEST = 10;
    private int requestLimit = 2000;

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    TransactionStore txStore;

    long fromBlock, toBlock;
    LinkedHashMap<ByteArrayWrapper, QueuedBlock> queuedBlocks = new LinkedHashMap<>();
    AtomicInteger blocksInMem = new AtomicInteger(0);

    long t;
    int cnt;

    Thread retrieveThread;
    private CountDownLatch stopLatch = new CountDownLatch(1);

    private long blockBytesLimit = 32 * 1024 * 1024;
    private long estimatedBlockSize = 0;
    private final CircularFifoQueue<Long> lastBlockSizes = new CircularFifoQueue<>(requestLimit);

    public ReceiptsDownloader(long fromBlock, long toBlock) {
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }

    public void startImporting() {
        retrieveThread = new Thread(this::retrieveLoop, "FastsyncReceiptsFetchThread");
        retrieveThread.start();
    }

    private synchronized List<byte[]> getHashesForRequest(int maxSize) {
        List<byte[]> ret = new ArrayList<>();
        for (; fromBlock < toBlock && maxSize > 0; fromBlock++) {
            BlockHeader header = blockStore.getChainBlockByNumber(fromBlock).getHeader();

            // Skipping download for blocks with no transactions
            if (FastByteComparisons.equal(header.getReceiptsRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                finalizeBlock();
                continue;
            }

            ret.add(header.getHash());
            maxSize--;
        }
        return ret;
    }

    private synchronized void processQueue() {
        Iterator<QueuedBlock> it = queuedBlocks.values().iterator();
        while (it.hasNext()) {
            QueuedBlock queuedBlock = it.next();
            List<TransactionReceipt> receipts = queuedBlock.receipts;
            if (receipts != null) {
                Block block = blockStore.getBlockByHash(queuedBlock.hash);
                if (validate(block, receipts)) {
                    for (int i = 0; i < queuedBlock.receipts.size(); i++) {
                        TransactionReceipt receipt = receipts.get(i);
                        TransactionInfo txInfo = new TransactionInfo(receipt, block.getHash(), i);
                        txInfo.setTransaction(block.getTransactionsList().get(i));
                        txStore.put(txInfo);
                    }

                    estimateBlockSize(receipts, block.getNumber());

                    it.remove();
                    blocksInMem.decrementAndGet();

                    finalizeBlock();
                } else {
                    queuedBlock.reset();
                }
            }
        }
    }

    private synchronized void processDownloaded(byte[] blockHash, List<TransactionReceipt> receipts) {
        QueuedBlock block = queuedBlocks.get(new ByteArrayWrapper(blockHash));
        if (block != null) {
            block.receipts = receipts;
        }
    }

    private void finalizeBlock() {
        synchronized (this) {
            if (fromBlock >= toBlock && queuedBlocks.isEmpty())
                finishDownload();

            cnt++;
            if (cnt % 1000 == 0) logger.info("FastSync: downloaded receipts for " + cnt + " blocks.");
        }
        dbFlushManager.commit();
    }

    private boolean validate(Block block, List<TransactionReceipt> receipts) {
        byte[] receiptsRoot = BlockchainImpl.calcReceiptsTrie(receipts);
        return FastByteComparisons.equal(receiptsRoot, block.getReceiptsRoot());
    }

    private void retrieveLoop() {
        List<List<byte[]>> toDownload = Collections.emptyList();
        long t = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {

                if (toDownload.isEmpty()) {
                    if (fillBlockQueue() > 0 || System.currentTimeMillis() - t > REQUEST_TIMEOUT) {
                        toDownload = getToDownload();
                        t = System.currentTimeMillis();
                    }
                }

                Channel idle = getAnyPeer();
                if (idle != null && !toDownload.isEmpty()) {
                    List<byte[]> list = toDownload.remove(0);
                    ListenableFuture<List<List<TransactionReceipt>>> future =
                            ((Eth63) idle.getEthHandler()).requestReceipts(list);
                    if (future != null) {
                        Futures.addCallback(future, new FutureCallback<List<List<TransactionReceipt>>>() {
                            @Override
                            public void onSuccess(List<List<TransactionReceipt>> result) {
                                for (int i = 0; i < result.size(); i++) {
                                    processDownloaded(list.get(i), result.get(i));
                                }
                                processQueue();
                            }
                            @Override
                            public void onFailure(Throwable t) {}
                        });
                    }
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                logger.warn("Unexpected during receipts downloading", e);
            }
        }
    }

    private List<List<byte[]>> getToDownload() {
        List<List<byte[]>> ret = new ArrayList<>();

        int reqSize = getRequestSize();
        synchronized (this) {
            List<byte[]> req = new ArrayList<>();
            for (QueuedBlock b : queuedBlocks.values()) {
                if (!b.hasResponse()) {
                    req.add(b.hash);
                    if (req.size() >= reqSize) {
                        ret.add(req);
                        req = new ArrayList<>();
                    }
                }
            }
            if (!req.isEmpty()) {
                ret.add(req);
            }
        }

        logger.debug("ReceiptsDownloader: queue broke down to {} requests, {} blocks in each", ret.size(), reqSize);
        return ret;
    }

    private int getRequestSize() {
        int reqCnt = max(syncPool.getActivePeersCount() * 3 / 4, 1);
        int optimalReqSz = queuedBlocks.size() / reqCnt;
        if (optimalReqSz <= MIN_IN_REQUEST) {
            return MIN_IN_REQUEST;
        } else if (optimalReqSz >= MAX_IN_REQUEST) {
            return MAX_IN_REQUEST;
        } else {
            return optimalReqSz;
        }
    }
    
    private int fillBlockQueue() {
        int blocksToAdd = getTargetBlocksInMem() - blocksInMem.get();
        if (blocksToAdd < MAX_IN_REQUEST)
            return 0;

        List<byte[]> blockHashes = getHashesForRequest(blocksToAdd);
        synchronized (this) {
            blockHashes.forEach(hash -> queuedBlocks.put(new ByteArrayWrapper(hash), new QueuedBlock(hash)));
        }
        blocksInMem.addAndGet(blockHashes.size());

        logger.debug("ReceiptsDownloader: blocks added {}, in queue {}, in memory {} (~{}mb)",
                blockHashes.size(), queuedBlocks.size(), blocksInMem.get(),
                blocksInMem.get() * estimatedBlockSize / 1024 / 1024);

        return blockHashes.size();
    }

    private int getTargetBlocksInMem() {
        if (estimatedBlockSize == 0) {
            return requestLimit;
        }

        int slotsInMem = max((int) (blockBytesLimit / estimatedBlockSize), MAX_IN_REQUEST);
        return min(slotsInMem, requestLimit);
    }

    /**
     * Download could block chain synchronization occupying all peers
     * Prevents this by leaving one peer without work
     * Fallbacks to any peer when low number of active peers available
     */
    Channel getAnyPeer() {
        return syncPool.getActivePeersCount() > 2 ? syncPool.getNotLastIdle() : syncPool.getAnyIdle();
    }

    public int getDownloadedBlocksCount() {
        return cnt;
    }

    public void stop() {
        retrieveThread.interrupt();
        stopLatch.countDown();
    }

    public void waitForStop() {
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void finishDownload() {
        stop();
    }

    private void estimateBlockSize(List<TransactionReceipt> receipts, long number) {
        if (receipts.isEmpty())
            return;

        long blockSize = receipts.stream().mapToLong(TransactionReceipt::estimateMemSize).sum();
        synchronized (lastBlockSizes) {
            lastBlockSizes.add(blockSize);
            estimatedBlockSize = lastBlockSizes.stream().mapToLong(Long::longValue).sum() / lastBlockSizes.size();
        }

        if (number % 1000 == 0)
            logger.debug("ReceiptsDownloader: estimated block size: {}", estimatedBlockSize);
    }

    @Autowired
    public void setSystemProperties(final SystemProperties config) {
        this.blockBytesLimit = config.blockQueueSize();
    }

    private static class QueuedBlock {
        byte[] hash;
        List<TransactionReceipt> receipts;

        public QueuedBlock(byte[] hash) {
            this.hash = hash;
        }

        public boolean hasResponse() {
            return receipts != null;
        }

        public void reset() {
            receipts = null;
        }
    }
}
