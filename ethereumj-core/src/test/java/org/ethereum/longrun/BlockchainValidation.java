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
package org.ethereum.longrun;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Bloom;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ethereum.core.BlockchainImpl.calcReceiptsTrie;

/**
 * Validation for all kind of blockchain data
 */
public class BlockchainValidation {

    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");

    private static Integer getReferencedTrieNodes(final Source<byte[], byte[]> stateDS, final boolean includeAccounts,
                                                  byte[] ... roots) {
        final AtomicInteger ret = new AtomicInteger(0);
        for (byte[] root : roots) {
            SecureTrie trie = new SecureTrie(stateDS, root);
            trie.scanTree(new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, TrieImpl.Node node) {
                    ret.incrementAndGet();
                }

                @Override
                public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
                    if (includeAccounts) {
                        AccountState accountState = new AccountState(value);
                        if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                            ret.incrementAndGet();
                        }
                        if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                            ret.addAndGet(getReferencedTrieNodes(stateDS, false, accountState.getStateRoot()));
                        }
                    }
                }
            });
        }
        return ret.get();
    }

    public static void checkNodes(Ethereum ethereum, CommonConfig commonConfig, AtomicInteger fatalErrors) {
        try {
            Source<byte[], byte[]> stateDS = commonConfig.stateSource();
            byte[] stateRoot = ethereum.getBlockchain().getBestBlock().getHeader().getStateRoot();
            Integer rootsSize = getReferencedTrieNodes(stateDS, true, stateRoot);
            testLogger.info("Node validation successful");
            testLogger.info("Non-unique node size: {}", rootsSize);
        } catch (Exception | AssertionError ex) {
            testLogger.error("Node validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkHeaders(Ethereum ethereum, AtomicInteger fatalErrors) {
        int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
        byte[] lastParentHash = null;
        testLogger.info("Checking headers from best block: {}", blockNumber);

        try {
            while (blockNumber >= 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                if (lastParentHash != null) {
                    assert FastByteComparisons.equal(currentBlock.getHash(), lastParentHash);
                }
                lastParentHash = currentBlock.getHeader().getParentHash();
                assert lastParentHash != null;
                blockNumber--;
            }

            testLogger.info("Checking headers successful, ended on block: {}", blockNumber + 1);
        } catch (Exception | AssertionError ex) {
            testLogger.error(String.format("Block header validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkFastHeaders(Ethereum ethereum, CommonConfig commonConfig, AtomicInteger fatalErrors) {
        DataSourceArray<BlockHeader> headerStore = commonConfig.headerSource();
        int blockNumber = headerStore.size() - 1;
        byte[] lastParentHash = null;

        try {
            testLogger.info("Checking fast headers from best block: {}", blockNumber);
            while (blockNumber > 0) {
                BlockHeader header = headerStore.get(blockNumber);
                if (lastParentHash != null) {
                    assert FastByteComparisons.equal(header.getHash(), lastParentHash);
                }
                lastParentHash = header.getParentHash();
                assert lastParentHash != null;
                blockNumber--;
            }

            Block genesis = ethereum.getBlockchain().getBlockByNumber(0);
            assert FastByteComparisons.equal(genesis.getHash(), lastParentHash);

            testLogger.info("Checking fast headers successful, ended on block: {}", blockNumber + 1);
        } catch (Exception | AssertionError ex) {
            testLogger.error(String.format("Fast header validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkBlocks(Ethereum ethereum, AtomicInteger fatalErrors) {
        Block currentBlock = ethereum.getBlockchain().getBestBlock();
        int blockNumber = (int) currentBlock.getHeader().getNumber();

        try {
            BlockStore blockStore = ethereum.getBlockchain().getBlockStore();
            testLogger.info("Checking blocks from best block: {}", blockNumber);
            BigInteger curTotalDiff = blockStore.getTotalDifficultyForHash(currentBlock.getHash());

            while (blockNumber > 0) {
                currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);

                // Validate uncles
                assert ((BlockchainImpl) ethereum.getBlockchain()).validateUncles(currentBlock);
                // Validate total difficulty
                Assert.assertTrue(String.format("Total difficulty, count %s == %s blockStore",
                        curTotalDiff, blockStore.getTotalDifficultyForHash(currentBlock.getHash())),
                        curTotalDiff.compareTo(blockStore.getTotalDifficultyForHash(currentBlock.getHash())) == 0);
                curTotalDiff = curTotalDiff.subtract(currentBlock.getDifficultyBI());

                blockNumber--;
            }

            // Checking total difficulty for genesis
            currentBlock = ethereum.getBlockchain().getBlockByNumber(0);
            Assert.assertTrue(String.format("Total difficulty for genesis, count %s == %s blockStore",
                    curTotalDiff, blockStore.getTotalDifficultyForHash(currentBlock.getHash())),
                    curTotalDiff.compareTo(blockStore.getTotalDifficultyForHash(currentBlock.getHash())) == 0);
            Assert.assertTrue(String.format("Total difficulty, count %s == %s genesis",
                    curTotalDiff, currentBlock.getDifficultyBI()),
                    curTotalDiff.compareTo(currentBlock.getDifficultyBI()) == 0);

            testLogger.info("Checking blocks successful, ended on block: {}", blockNumber + 1);
        } catch (Exception | AssertionError ex) {
            testLogger.error(String.format("Block validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkTransactions(Ethereum ethereum, AtomicInteger fatalErrors) {
        int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
        testLogger.info("Checking block transactions from best block: {}", blockNumber);

        try {
            while (blockNumber > 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);

                List<TransactionReceipt> receipts = new ArrayList<>();
                for (Transaction tx : currentBlock.getTransactionsList()) {
                    TransactionInfo txInfo = ((BlockchainImpl) ethereum.getBlockchain()).getTransactionInfo(tx.getHash());
                    assert txInfo != null;
                    receipts.add(txInfo.getReceipt());
                }

                Bloom logBloom = new Bloom();
                for (TransactionReceipt receipt : receipts) {
                    logBloom.or(receipt.getBloomFilter());
                }
                assert FastByteComparisons.equal(currentBlock.getLogBloom(), logBloom.getData());
                assert FastByteComparisons.equal(currentBlock.getReceiptsRoot(), calcReceiptsTrie(receipts));

                blockNumber--;
            }

            testLogger.info("Checking block transactions successful, ended on block: {}", blockNumber + 1);
        } catch (Exception | AssertionError ex) {
            testLogger.error(String.format("Transaction validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void fullCheck(Ethereum ethereum, CommonConfig commonConfig, AtomicInteger fatalErrors) {

        // nodes
        testLogger.info("Validating nodes: Start");
        BlockchainValidation.checkNodes(ethereum, commonConfig, fatalErrors);
        testLogger.info("Validating nodes: End");

        // headers
        testLogger.info("Validating block headers: Start");
        BlockchainValidation.checkHeaders(ethereum, fatalErrors);
        testLogger.info("Validating block headers: End");

        // blocks
        testLogger.info("Validating blocks: Start");
        BlockchainValidation.checkBlocks(ethereum, fatalErrors);
        testLogger.info("Validating blocks: End");

        // receipts
        testLogger.info("Validating transaction receipts: Start");
        BlockchainValidation.checkTransactions(ethereum, fatalErrors);
        testLogger.info("Validating transaction receipts: End");
    }
}
