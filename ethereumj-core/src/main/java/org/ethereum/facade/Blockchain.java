package org.ethereum.facade;

import java.util.Map;

import org.ethereum.core.Block;
import org.ethereum.core.BlockQueue;

public interface Blockchain  {

    public int getSize();
    public void add(Block block);
    public void storeBlock(Block block);
    public Map<Long, byte[]> getBlockCache();
    public Block getBlockByNumber(long blockNr);
    public long getGasPrice();
    public void setLastBlock(Block block);
    public Block getLastBlock();
    public BlockQueue getBlockQueue();
    public void close();
	public byte[] getTotalDifficulty();
	public byte[] getLatestBlockHash();
}
