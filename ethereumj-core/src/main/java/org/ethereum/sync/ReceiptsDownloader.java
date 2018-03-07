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

    private final static long REQUEST_TIMEOUT = 15 * 1000;
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

    long fromBlock, toBlock, queueBlock;
    Set<Long> completedBlocks = new HashSet<>();
    Deque<ReceiptsRequest> queue = new LinkedBlockingDeque<>();
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
        this.queueBlock = this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }

    public void startImporting() {
        retrieveThread = new Thread(this::retrieveLoop, "FastsyncReceiptsFetchThread");
        retrieveThread.start();
    }

    private List<ReceiptsRequest> getToDownload(int maxSize) {
        List<byte[]> toDownload = getHashesForRequest(maxSize);
        List<ReceiptsRequest> ret = new ArrayList<>();
        for (int i = 0; i < toDownload.size(); i += MAX_IN_REQUEST) {
            List<byte[]> payload = toDownload.subList(i, Math.min(toDownload.size(), i + MAX_IN_REQUEST));
            ret.add(new ReceiptsRequest(payload));
        }
        return ret;
    }

    private List<byte[]> getHashesForRequest(int maxSize) {
        List<byte[]> ret = new ArrayList<>();
        for (; queueBlock < toBlock && maxSize > 0; queueBlock++) {
            BlockHeader header = headerStore.get((int) queueBlock);

            // Skipping download for blocks with no transactions
            if (FastByteComparisons.equal(header.getReceiptsRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                finalizeBlock(header.getNumber());
                continue;
            }

            ret.add(header.getHash());
            maxSize--;
        }
        return ret;
    }

    private boolean processDownloaded(byte[] blockHash, List<TransactionReceipt> receipts) {
        Block block = blockStore.getBlockByHash(blockHash);
        if (validate(block, receipts)) {
            for (int i = 0; i < receipts.size(); i++) {
                TransactionReceipt receipt = receipts.get(i);
                TransactionInfo txInfo = new TransactionInfo(receipt, block.getHash(), i);
                txInfo.setTransaction(block.getTransactionsList().get(i));
                txStore.put(txInfo);
            }

            finalizeBlock(block.getNumber());
            estimateBlockSize(receipts, block.getNumber());
            return true;
        }

        return false;
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

                if (queue.isEmpty()) {
                    int slotsLeft = getRequestSize() - MAX_IN_REQUEST * (queue.size() + pending.size());
                    if (slotsLeft >= MAX_IN_REQUEST) {
                        List<ReceiptsRequest> toQueue = getToDownload(slotsLeft);
                        queue.addAll(toQueue);
                        logger.debug("ReceiptsDownloader: {} new requests added, queue grew up to {}, pending {}",
                                toQueue.size(), queue.size(), pending.size());
                    }
                }

                Channel idle = getAnyPeer();
                if (idle != null) {
                    ReceiptsRequest req = queue.poll();

                    if (req != null) {

                        int requestId = ++requests;
                        synchronized (this) {
                            req.sentAt = System.currentTimeMillis();
                            pending.put(requestId, req);
                        }

                        ListenableFuture<List<List<TransactionReceipt>>> future =
                                ((Eth63) idle.getEthHandler()).requestReceipts(req.payload);
                        if (future != null) {
                            if (requestId % 10 == 0) {
                                logger.debug("ReceiptsDownloader: queue size {}, pending {}, total ~{}mb",
                                        queue.size(), pending.size(),
                                        (queue.size() + pending.size()) * MAX_IN_REQUEST * estimatedBlockSize / 1024 / 1024);
                            }

                            Futures.addCallback(future, new FutureCallback<List<List<TransactionReceipt>>>() {
                                @Override
                                public void onSuccess(List<List<TransactionReceipt>> result) {
                                    try {
                                        if (!processResponse(requestId, result)) {
                                            idle.dropConnection();
                                        }
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
                } else {
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

    private boolean processResponse(int requestId, List<List<TransactionReceipt>> response) {
        ReceiptsRequest req;
        synchronized (this) {
            req = pending.get(requestId);
        }
        if (req != null) {
            for (int i = 0; i < response.size(); i++) {
                if (!processDownloaded(req.payload.get(i), response.get(i))) {
                    queueBack(requestId);
                    return false;
                }
            }

            synchronized (this) {
                pending.remove(requestId);
            }
        }

        return true;
    }

    private synchronized void queueBack(int requestId) {
        ReceiptsRequest req = pending.remove(requestId);
        if (req != null) queue.addFirst(req);
    }

    private synchronized void processTimeouts() {
        Iterator<ReceiptsRequest> iter = pending.values().iterator();
        while (iter.hasNext()) {
            ReceiptsRequest req = iter.next();
            if (System.currentTimeMillis() - req.sentAt > REQUEST_TIMEOUT) {
                iter.remove();
                queue.addFirst(req);
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
        long sentAt = 0;
        List<byte[]> payload;

        public ReceiptsRequest(List<byte[]> payload) {
            this.payload = payload;
        }
    }
}
