package org.ethereum.db;

import org.ethereum.core.Block;
import org.hibernate.SessionFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 08.01.2015
 */
public interface BlockStore {

    byte[] getBlockHashByNumber(long blockNumber);

    Block getChainBlockByNumber(long blockNumber);

    Block getBlockByHash(byte[] hash);
    boolean isBlockExist(byte[] hash);

    List<byte[]> getListHashesEndWith(byte[] hash, long qty);

    void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain);

    BigInteger getTotalDifficultyForHash(byte[] hash);

    BigInteger getTotalDifficulty();

    Block getBestBlock();

    long getMaxNumber();


    void flush();

    void reBranch(Block forkBlock);

    void load();
    void setSessionFactory(SessionFactory sessionFactory);


}
