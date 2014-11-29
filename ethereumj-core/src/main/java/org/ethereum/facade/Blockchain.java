package org.ethereum.facade;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.ethereum.core.Block;
import org.ethereum.core.Chain;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.BlockQueue;
import org.ethereum.core.Genesis;

public interface Blockchain  {

	public static final byte[] GENESIS_HASH = Genesis.getInstance().getHash();

    public long getSize();
    public void add(Block block);
    public void tryToConnect(Block block);
    public void storeBlock(Block block);
    public Block getBlockByNumber(long blockNr);
    public void setBestBlock(Block block);
    public Block getBestBlock();
    public BlockQueue getQueue();
    public boolean hasParentOnTheChain(Block block);
    public void reset();
    public void close();
	public void updateTotalDifficulty(Block block);
    public BigInteger getTotalDifficulty();
    public void setTotalDifficulty(BigInteger totalDifficulty);
	public byte[] getBestBlockHash();
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);
    public Block getBlockByHash(byte[] hash);
    public List<Chain> getAltChains();
    public List<Block> getGarbage();
}
