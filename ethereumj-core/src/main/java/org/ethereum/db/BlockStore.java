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

    Block getBlockByNumber(long blockNumber);

    Block getBlockByHash(byte[] hash);

    @SuppressWarnings("unchecked")
    List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);

    void deleteBlocksSince(long number);

    void saveBlock(Block block, List<TransactionReceipt> receipts);

    BigInteger getTotalDifficultySince(long number);

    BigInteger getTotalDifficulty();

    Block getBestBlock();

    @SuppressWarnings("unchecked")
    List<Block> getAllBlocks();

    void reset();

    TransactionReceipt getTransactionReceiptByHash(byte[] hash);
}
