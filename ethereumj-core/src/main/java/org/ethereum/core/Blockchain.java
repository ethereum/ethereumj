package org.ethereum.core;

import org.ethereum.db.TransactionInfo;

import java.math.BigInteger;
import java.util.List;

public interface Blockchain {

    public long getSize();

    public boolean add(Block block);

    public ImportResult tryToConnect(Block block);

    public void storeBlock(Block block, List<TransactionReceipt> receipts);

    public Block getBlockByNumber(long blockNr);

    public void setBestBlock(Block block);

    public Block getBestBlock();

    TransactionInfo getTransactionInfo(byte[] hash);
    
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

    public void setExitOn(long exitOn);

    boolean isBlockExist(byte[] hash);

    List<BlockHeader> getListOfHeadersStartFrom(BlockIdentifier identifier, int skip, int limit, boolean reverse);

    List<byte[]> getListOfBodiesByHashes(List<byte[]> hashes);

    Block createNewBlock(Block parent, List<Transaction> transactions, List<BlockHeader> uncles);
}
