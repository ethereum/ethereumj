package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 08.01.2015
 */
public interface BlockStore {

    byte[] getBlockHashByNumber(long blockNumber);

    /**
     * Gets the block hash by its index.
     * When more than one block with the specified index exists (forks)
     * the select the block which is ancestor of the branchBlockHash
     */
    byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash);

    Block getChainBlockByNumber(long blockNumber);

    Block getBlockByHash(byte[] hash);
    boolean isBlockExist(byte[] hash);

    List<byte[]> getListHashesEndWith(byte[] hash, long qty);

    List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty);

    List<Block> getListBlocksEndWith(byte[] hash, long qty);

    void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain);

    BigInteger getTotalDifficultyForHash(byte[] hash);

    BigInteger getTotalDifficulty();

    Block getBestBlock();

    long getMaxNumber();


    void flush();

    void reBranch(Block forkBlock);

    void load();

    void close();
}
