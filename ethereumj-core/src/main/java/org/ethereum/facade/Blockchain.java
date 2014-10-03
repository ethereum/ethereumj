package org.ethereum.facade;

import java.math.BigInteger;
import java.util.Map;

import org.ethereum.core.Block;
import org.ethereum.core.BlockQueue;
import org.ethereum.core.Genesis;

public interface Blockchain  {

	public static final byte[] GENESIS_HASH = Genesis.getInstance().getHash();

    public int getSize();
    public void add(Block block);
    public void storeBlock(Block block);
    public Map<Long, byte[]> getBlockCache();
    public Block getBlockByNumber(long blockNr);
    public long getGasPrice();
    public void setLastBlock(Block block);
    public Block getLastBlock();
    public BlockQueue getQueue();
    public void close();
	public BigInteger getTotalDifficulty();
	public byte[] getLatestBlockHash();
}
