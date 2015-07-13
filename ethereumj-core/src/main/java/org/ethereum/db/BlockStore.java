package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 08.01.2015
 */
public interface BlockStore {

    byte[] getBlockHashByNumber(long blockNumber);

    Block getBlockByNumber(long blockNumber);

    Block getBlockByHash(byte[] hash);

    List<byte[]> getListHashesEndWith(byte[] hash, long qty);

    void saveBlock(Block block, List<TransactionReceipt> receipts);

    BigInteger getTotalDifficulty();

    Block getBestBlock();

    void flush();
    void load();
    void setSessionFactory(SessionFactory sessionFactory);


}
