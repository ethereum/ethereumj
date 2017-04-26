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

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Scope("prototype")
public class ReceiptsDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

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

    long t;
    int cnt;

    Thread retrieveThread;
    private CountDownLatch stopLatch = new CountDownLatch(1);

    public ReceiptsDownloader(long fromBlock, long toBlock) {
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }

    public void startImporting() {
        retrieveThread = new Thread("FastsyncReceiptsFetchThread") {
            @Override
            public void run() {
                retrieveLoop();
            }
        };
        retrieveThread.start();
    }

    private List<List<byte[]>> getToDownload(int maxAskSize, int maxAsks) {
        List<byte[]> toDownload = getToDownload(maxAskSize * maxAsks);
        List<List<byte[]>> ret = new ArrayList<>();
        for (int i = 0; i < toDownload.size(); i += maxAskSize) {
            ret.add(toDownload.subList(i, Math.min(toDownload.size(), i + maxAskSize)));
        }
        return ret;
    }

    private synchronized List<byte[]> getToDownload(int maxSize) {
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
        List<List<byte[]>> toDownload = Collections.emptyList();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (toDownload.isEmpty()) {
                    toDownload = getToDownload(100, 20);
                }

                Channel idle = getAnyPeer();
                if (idle != null) {
                    final List<byte[]> list = toDownload.remove(0);
                    ListenableFuture<List<List<TransactionReceipt>>> future =
                            ((Eth63) idle.getEthHandler()).requestReceipts(list);
                    if (future != null) {
                        Futures.addCallback(future, new FutureCallback<List<List<TransactionReceipt>>>() {
                            @Override
                            public void onSuccess(List<List<TransactionReceipt>> result) {
                                for (int i = 0; i < result.size(); i++) {
                                    processDownloaded(list.get(i), result.get(i));
                                }
                            }
                            @Override
                            public void onFailure(Throwable t) {}
                        });
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Unexpected during receipts downloading", e);
            }
        }
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
}
