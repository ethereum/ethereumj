package org.ethereum.core;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public interface Blockchain {

    public static final byte[] GENESIS_HASH = Genesis.getInstance().getHash();

    public long getSize();

    public void add(Block block);

    public ImportResult tryToConnect(Block block);

    public void storeBlock(Block block, List<TransactionReceipt> receipts);

    public Block getBlockByNumber(long blockNr);

    public void setBestBlock(Block block);

    public Block getBestBlock();

    public boolean hasParentOnTheChain(Block block);

    void close();

    public void updateTotalDifficulty(Block block);

    public BigInteger getTotalDifficulty();

    public void setTotalDifficulty(BigInteger totalDifficulty);

    public byte[] getBestBlockHash();

    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);

    public List<byte[]> getListOfHashesStartFromBlock(long blockNumber, int qty);

    TransactionReceipt getTransactionReceiptByHash(byte[] hash);

    public Block getBlockByHash(byte[] hash);

    public List<Chain> getAltChains();

    public List<Block> getGarbage();

    public Set<Transaction> getPendingTransactions();
    public void addPendingTransactions(Set<Transaction> transactions);
    public void clearPendingTransactions(List<Transaction> receivedTransactions);

    public void setExitOn(long exitOn);

    boolean isBlockExist(byte[] hash);
}
