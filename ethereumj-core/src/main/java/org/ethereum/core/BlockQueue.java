package org.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Roman Mandeleil 
 * Created on: 27/07/2014 11:28
 */
public class BlockQueue {

	private static Logger logger = LoggerFactory.getLogger("blockchain");

	private Deque<byte[]> blockHashQueue = new ArrayDeque<>();
	private Queue<Block> blockReceivedQueue = new ConcurrentLinkedQueue<>();
    private BigInteger highestTotalDifficulty;
	private Block lastBlock;

	private Timer timer = new Timer("BlockQueueTimer");

	public BlockQueue() {
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				nudgeQueue();
			}
		}, 10, 10);
	}

	private void nudgeQueue() {
		if (blockReceivedQueue.isEmpty())
			return;
		
		Block block = blockReceivedQueue.poll();
		WorldManager.getInstance().getBlockchain().add(block);
	}

	public void addBlocks(List<Block> blockList) {

		Block lastReceivedBlock = blockList.get(blockList.size() - 1);
		if (lastReceivedBlock.getNumber() != getLastBlock().getNumber() + 1)
			return;

		for (int i = blockList.size() - 1; i >= 0; --i) {

			if (blockReceivedQueue.size() > SystemProperties.CONFIG.maxBlocksQueued())
				return;

			this.lastBlock = blockList.get(i);
			logger.trace("Last block now index: [{}]", lastBlock.getNumber());
			blockReceivedQueue.add(lastBlock);
		}
		logger.trace("Blocks waiting to be proceed in the queue: [{}]", 
				blockReceivedQueue.size());
	}
	
    public BigInteger getHighestTotalDifficulty() {
		return highestTotalDifficulty;
	}

	public void setHighestTotalDifficulty(BigInteger highestTotalDifficulty) {
		this.highestTotalDifficulty = highestTotalDifficulty;
	}

	public Block getLastBlock() {
		if (blockReceivedQueue.isEmpty())
			return WorldManager.getInstance().getBlockchain().getLastBlock();
		return lastBlock;
	}

	public void setBestHash(byte[] hash) {
		blockHashQueue.clear();
		blockHashQueue.addLast(hash);
	}

	public byte[] getBestHash() {
		return blockHashQueue.peekLast();
	}

	public void addHash(byte[] hash) {
		blockHashQueue.addLast(hash);
	}

	public List<byte[]> getHashes(int amount) {
		List<byte[]> hashes = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			if (!blockHashQueue.isEmpty())
				hashes.add(blockHashQueue.poll());
			else break;
		}
		return hashes;
	}

	private class BlockByIndexComparator implements Comparator<Block> {

		@Override
		public int compare(Block o1, Block o2) {

			if (o1 == null || o2 == null)
				throw new NullPointerException();

			if (o1.getNumber() > o2.getNumber())
				return 1;
			if (o1.getNumber() < o2.getNumber())
				return -1;

			return 0;
		}
	}

	public int size() {
		return blockReceivedQueue.size();
	}

	public void close() {
		timer.cancel();
		timer.purge();
	}
}
