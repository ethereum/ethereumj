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
package org.ethereum.listener;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.publish.event.BlockAdded;
import org.ethereum.sync.BlockDownloader;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADDED;

/**
 * Class capable of replaying stored blocks prior to 'going online' and
 * notifying on newly imported blocks
 * <p>
 * For example of usage, look at {@link org.ethereum.samples.BlockReplaySample}
 * <p>
 * Created by Eugene Shevchenko on 07.10.2018.
 */
public class BlockReplayer {

    private static final Logger logger = LoggerFactory.getLogger("events");
    private static final int HALF_BUFFER = BlockDownloader.MAX_IN_REQUEST;

    private final BlockStore blockStore;
    private final TransactionStore transactionStore;
    private final long firstBlock;
    private final Consumer<BlockAdded.Data> handler;

    private final CircularFifoQueue<BlockAdded.Data> blocksCache = new CircularFifoQueue<>(HALF_BUFFER * 2);
    private boolean completed = false;
    private byte[] lastReplayedBlockHash;

    public BlockReplayer(long firstBlock, BlockStore blockStore, TransactionStore transactionStore, Consumer<BlockAdded.Data> handler) {
        if (firstBlock < 0L) {
            throw new IllegalArgumentException("Initial block number should be positive value or zero.");
        }
        requireNonNull(blockStore, "Blocks store is not defined.");
        requireNonNull(transactionStore, "Transactions store is not defined.");
        requireNonNull(handler, "BlockAdded event handler is not defined.");

        this.blockStore = blockStore;
        this.transactionStore = transactionStore;
        this.firstBlock = firstBlock;
        this.handler = handler;
    }

    private void replayBlock(long num) {
        Block block = blockStore.getChainBlockByNumber(num);
        List<TransactionReceipt> receipts = block.getTransactionsList().stream()
                .map(tx -> {
                    TransactionInfo info = transactionStore.get(tx.getHash(), block.getHash());
                    TransactionReceipt receipt = info.getReceipt();
                    receipt.setTransaction(tx);
                    return receipt;
                })
                .collect(toList());

        BlockSummary blockSummary = new BlockSummary(block, null, receipts, null);
        blockSummary.setTotalDifficulty(BigInteger.valueOf(num));

        lastReplayedBlockHash = block.getHash();

        handler.accept(new BlockAdded.Data(blockSummary, true));
    }

    /**
     * Replay blocks synchronously
     */
    public void replay() {
        long lastBlock = blockStore.getMaxNumber();
        long currentBlock = firstBlock;
        int replayedBlocksCount = 0;

        logger.info("Replaying blocks from {}, current best block: {}", firstBlock, lastBlock);
        while (!completed) {
            for (; currentBlock <= lastBlock; currentBlock++, replayedBlocksCount++) {
                replayBlock(currentBlock);

                if (replayedBlocksCount % 1000 == 0) {
                    logger.info("Replayed " + replayedBlocksCount + " blocks so far. Current block: " + currentBlock);
                }
            }

            synchronized (this) {
                if (blocksCache.size() < blocksCache.maxSize()) {
                    completed = true;
                } else {
                    // So we'll have half of the buffer for new blocks until not synchronized replay finish
                    long newLastBlock = blockStore.getMaxNumber() - HALF_BUFFER;
                    if (lastBlock < newLastBlock) {
                        lastBlock = newLastBlock;
                    } else {
                        completed = true;
                    }
                }
            }
        }
        logger.info("Replay complete.");
    }

    private synchronized void onBlock(BlockAdded.Data data) {
        if (completed) {
            if (!blocksCache.isEmpty()) {
                replayCachedBlocks();
                logger.info("Cache replay complete. Switching to online mode.");
            }

            handler.accept(data);
        } else {
            blocksCache.add(data);
        }
    }

    private void replayCachedBlocks() {
        logger.info("Replaying cached {} blocks...", blocksCache.size());

        boolean lastBlockFound = (lastReplayedBlockHash == null) || (blocksCache.size() < blocksCache.maxSize());
        for (BlockAdded.Data cachedBlock : blocksCache) {
            if (lastBlockFound) {
                handler.accept(cachedBlock);
            } else {
                byte[] blockHash = cachedBlock.getBlockSummary().getBlock().getHash();
                lastBlockFound = FastByteComparisons.equal(blockHash, lastReplayedBlockHash);
            }
        }

        blocksCache.clear();
    }

    public boolean isDone() {
        return completed && blocksCache.isEmpty();
    }

    public static Builder startFrom(long blockNumber) {
        return new Builder(blockNumber);
    }

    public static class Builder {

        private long startBlockNumber;
        private BlockStore blockStore;
        private TransactionStore transactionStore;
        private Consumer<BlockAdded.Data> handler;

        public Builder(long startBlockNumber) {
            this.startBlockNumber = startBlockNumber;
        }

        public Builder withStores(BlockStore blockStore, TransactionStore transactionStore) {
            this.blockStore =  blockStore;
            this.transactionStore = transactionStore;
            return this;
        }

        public Builder withHandler(Consumer<BlockAdded.Data> handler) {
            this.handler = handler;
            return this;
        }

        public BlockReplayer build() {
            return new BlockReplayer(startBlockNumber, blockStore, transactionStore, handler);
        }

        public BlockReplayer replayAsyncAt(Ethereum ethereum) {
            if (startBlockNumber > ethereum.getBlockchain().getBestBlock().getNumber()) {
                logger.info("Nothing to replay: start replay block is greater than blockchain's best block.");
            }

            BlockReplayer blockReplay = build();
            ethereum.subscribe(BLOCK_ADDED, blockReplay::onBlock);
            new Thread(() -> blockReplay.replay()).start();

            return blockReplay;
        }
    }
}
