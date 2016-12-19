package org.ethereum.longrun;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Bloom;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.facade.Ethereum;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        SecureTrie trie = new SecureTrie(new SourceCodec.BytesKey<>(stateDS, Serializers.TrieNodeSerializer));
        for (byte[] root : roots) {
            trie.scanTree(root, new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, Value node) {
                    ret.incrementAndGet();
                }

                @Override
                public void doOnValue(byte[] nodeHash, Value node, byte[] key, byte[] value) {
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
        } catch (Exception ex) {
            testLogger.error("Node validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkHeaders(Ethereum ethereum, AtomicInteger fatalErrors) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            byte[] lastParentHash = null;
            testLogger.info("Checking headers from best block: {}", blockNumber);

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
        } catch (Exception ex) {
            testLogger.error("Block header validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkBlocks(Ethereum ethereum, AtomicInteger fatalErrors) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            testLogger.info("Checking blocks from best block: {}", blockNumber);

            while (blockNumber > 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                // Validate uncles
                assert ((BlockchainImpl) ethereum.getBlockchain()).validateUncles(currentBlock);
                blockNumber--;
            }

            testLogger.info("Checking blocks successful, ended on block: {}", blockNumber + 1);
        } catch (Exception ex) {
            testLogger.error("Block validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkTransactions(Ethereum ethereum, AtomicInteger fatalErrors) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            testLogger.info("Checking block transactions from best block: {}", blockNumber);

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
        } catch (Exception ex) {
            testLogger.error("Transaction validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }
}
