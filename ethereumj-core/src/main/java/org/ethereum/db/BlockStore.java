package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 08.01.2015
 */
public interface BlockStore {

    public byte[] getBlockHashByNumber(long blockNumber);

    @Transactional(readOnly = true)
    Block getBlockByNumber(long blockNumber);

    @Transactional(readOnly = true)
    Block getBlockByHash(byte[] hash);

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);

    @Transactional
    void deleteBlocksSince(long number);

    @Transactional
    void saveBlock(Block block, List<TransactionReceipt> receipts);

    @Transactional(readOnly = true)
    BigInteger getTotalDifficultySince(long number);

    @Transactional(readOnly = true)
    BigInteger getTotalDifficulty();

    @Transactional(readOnly = true)
    Block getBestBlock();

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    List<Block> getAllBlocks();

    @Transactional
    void reset();

    TransactionReceipt getTransactionReceiptByHash(byte[] hash);
}
