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
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.net.eth.handler.Eth63;
import org.ethereum.net.server.Channel;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Scope("prototype")
public class ReceiptsDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static long REQUEST_TIMEOUT = 5 * 1000;
    private static final int MAX_IN_REQUEST = 100;
    private int requestLimit = 2000;

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    TransactionStore txStore;

    @Autowired @Qualifier("headerSource")
    DataSourceArray<BlockHeader> headerStore;

    long fromBlock, toBlock;
    Set<Long> completedBlocks = new HashSet<>();
    Deque<List<byte[]>> toDownload = new LinkedBlockingDeque<>();
    Map<Integer, ReceiptsRequest> pending = new HashMap<>();
    int requests = 0;

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

    private List<List<byte[]>> getToDownload(int maxSize) {
        List<byte[]> toDownload = getHashesForRequest(maxSize);
        List<List<byte[]>> ret = new ArrayList<>();
        for (int i = 0; i < toDownload.size(); i += MAX_IN_REQUEST) {
            ret.add(toDownload.subList(i, Math.min(toDownload.size(), i + MAX_IN_REQUEST)));
        }
        return ret;
    }

    private synchronized List<byte[]> getHashesForRequest(int maxSize) {
        List<byte[]> ret = new ArrayList<>();
        for (long i = fromBlock; i < toBlock && maxSize > 0; i++) {
            if (!completedBlocks.contains(i)) {
                BlockHeader header = headerStore.get((int) i);

                // Skipping download for blocks with no transactions
                if (FastByteComparisons.equal(header.getReceiptsRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                    finalizeBlock(header.getNumber());
                    continue;
                }

                ret.add(header.getHash());
                maxSize--;
            }
        }
        return ret;
    }

    private void processDownloaded(byte[] blockHash, List<TransactionReceipt> receipts) {
        Block block = blockStore.getBlockByHash(blockHash);
        if (block.getNumber() >= fromBlock && validate(block, receipts) && !completedBlocks.contains(block.getNumber())) {
            for (int i = 0; i < receipts.size(); i++) {
                TransactionReceipt receipt = receipts.get(i);
                TransactionInfo txInfo = new TransactionInfo(receipt, block.getHash(), i);
                txInfo.setTransaction(block.getTransactionsList().get(i));
                txStore.put(txInfo);
            }

            finalizeBlock(block.getNumber());
        }

        estimateBlockSize(receipts, block.getNumber());
    }

    private void finalizeBlock(Long blockNumber) {
        synchronized (this) {
            completedBlocks.add(blockNumber);

            while (fromBlock < toBlock && completedBlocks.remove(fromBlock)) fromBlock++;

            if (fromBlock >= toBlock) finishDownload();

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
        while (!Thread.currentThread().isInterrupted()) {
            try {

                processTimeouts();

                if (toDownload.isEmpty()) {
                    int slotsLeft = getRequestSize() - MAX_IN_REQUEST * (toDownload.size() + pending.size());
                    if (slotsLeft >= MAX_IN_REQUEST) {
                        List<List<byte[]>> toQueue = getToDownload(slotsLeft);
                        toDownload.addAll(toQueue);
                        logger.debug("ReceiptsDownloader: {} new requests added, queue grew up to {}, pending {}",
                                toQueue.size(), toDownload.size(), pending.size());
                    }
                }

                Channel idle = getAnyPeer();
                List<byte[]> list = null;
                if (idle != null) {
                    list = toDownload.poll();

                    if (list != null) {

                        int requestId;
                        synchronized (this) {
                            requestId = ++requests;
                            pending.put(requestId, new ReceiptsRequest(list));
                        }

                        ListenableFuture<List<List<TransactionReceipt>>> future =
                                ((Eth63) idle.getEthHandler()).requestReceipts(list);
                        if (future != null) {
                            if (requestId % 10 == 0) {
                                logger.debug("ReceiptsDownloader: queue size {}, pending {}",
                                        toDownload.size(), pending.size());
                            }

                            Futures.addCallback(future, new FutureCallback<List<List<TransactionReceipt>>>() {
                                @Override
                                public void onSuccess(List<List<TransactionReceipt>> result) {
                                    try {
                                        processResponse(requestId, result);
                                    } catch (Throwable t) {
                                        queueBack(requestId);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    queueBack(requestId);
                                }
                            });
                        } else {
                            queueBack(requestId);
                        }
                    }
                }

                if (idle == null || list == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Unexpected during receipts downloading", e);
            }
        }
    }

    private synchronized void processResponse(int requestId, List<List<TransactionReceipt>> response) {
        ReceiptsRequest req = pending.get(requestId);
        if (req != null) {
            for (int i = 0; i < response.size(); i++) {
                processDownloaded(req.payload.get(i), response.get(i));
            }
            pending.remove(requestId);
        }
    }

    private synchronized void queueBack(int requestId) {
        ReceiptsRequest req = pending.remove(requestId);
        if (req != null) toDownload.addFirst(req.payload);
    }

    private synchronized void processTimeouts() {
        Iterator<ReceiptsRequest> iter = pending.values().iterator();
        while (iter.hasNext()) {
            ReceiptsRequest req = iter.next();
            if (System.currentTimeMillis() - req.sentAt > REQUEST_TIMEOUT) {
                iter.remove();
                toDownload.addFirst(req.payload);
            }
        }
    }

    private int getRequestSize() {
        if (estimatedBlockSize == 0) {
            return requestLimit;
        }

        int slotsLeft = Math.max((int) (blockBytesLimit / estimatedBlockSize), 2 * MAX_IN_REQUEST);
        return Math.min(slotsLeft, requestLimit);
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

    private static class ReceiptsRequest {
        long sentAt;
        List<byte[]> payload;

        public ReceiptsRequest(List<byte[]> payload) {
            this.sentAt = System.currentTimeMillis();
            this.payload = payload;
        }
    }
}
