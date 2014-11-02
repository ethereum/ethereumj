package org.ethereum.facade;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.ethereum.core.Block;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.BlockQueue;
import org.ethereum.core.Genesis;

public interface Blockchain  {

	public static final byte[] GENESIS_HASH = Genesis.getInstance().getHash();

    public int getSize();
    public void add(Block block);
    public void storeBlock(Block block);
    public Map<Long, ByteArrayWrapper> getBlockCache();
    public Block getBlockByNumber(long blockNr);
    public long getGasPrice();
    public void setLastBlock(Block block);
    public Block getLastBlock();
    public BlockQueue getQueue();
    public void close();
	public void updateTotalDifficulty(Block block);
    public BigInteger getTotalDifficulty();
	public byte[] getLatestBlockHash();
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty);
    public Block getBlockByHash(byte[] hash);

}
