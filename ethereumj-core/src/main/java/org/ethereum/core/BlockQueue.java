package org.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The processing queue for blocks to be validated and added to the blockchain.
 * This class also maintains the list of hashes from the peer with the heaviest sub-tree.
 * Based on these hashes, blocks are added to the queue.
 * 
 * @author Roman Mandeleil 
 * Created on: 27/07/2014 11:28
 */
public class BlockQueue {

	private static Logger logger = LoggerFactory.getLogger("blockchain");

	/** The list of hashes of the heaviest chain on the network, 
	 * for which this client doesn't have the blocks yet */
	private Deque<byte[]> blockHashQueue = new ConcurrentLinkedDeque<>();
	
	/** Queue with blocks to be validated and added to the blockchain */
	private Queue<Block> blockReceivedQueue = new ConcurrentLinkedQueue<>();
	
	/** Highest known total difficulty, representing the heaviest chain on the network */
    private BigInteger highestTotalDifficulty;
    
    /** Last block in the queue to be processed */
	private Block lastBlock;

	private Timer timer = new Timer("BlockQueueTimer");

	public BlockQueue() {
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				nudgeQueue();
			}
		}, 10, 10);
	}

	/**
	 * Processing the queue adding blocks to the chain.
	 */
	private void nudgeQueue() {
		if (blockReceivedQueue.isEmpty())
			return;
		
		Block block = blockReceivedQueue.poll();
		WorldManager.getInstance().getBlockchain().add(block);
	}

	/**
	 * Add a list of blocks to the processing queue.
	 * The list is validated by making sure the first block in the received list of blocks
	 * is the next expected block number of the queue.
	 * 
	 * The queue is configured to contain a maximum number of blocks to avoid memory issues
	 * If the list exceeds that, the rest of the received blocks in the list are discarded.
	 * 
	 * @param blockList - the blocks received from a peer to be added to the queue
	 */
	public void addBlocks(List<Block> blockList) {

		Block lastReceivedBlock = blockList.get(0);
		if (lastReceivedBlock.getNumber() != getLastBlock().getNumber() + 1)
			return;

		for (Block block : blockList) {
			
			if (blockReceivedQueue.size() > SystemProperties.CONFIG.maxBlocksQueued())
				return;

			this.lastBlock = block;
			logger.trace("Last block now index: [{}]", lastBlock.getNumber());
			blockReceivedQueue.add(lastBlock);
		}
		logger.trace("Blocks waiting to be proceed in the queue: [{}]", 
				blockReceivedQueue.size());
	}
	
	/**
	 * Returns the last block in the queue. If the queue is empty, 
	 * this will return the last block added to the blockchain.
	 * 
	 * @return The last known block this client on the network
	 */
	public Block getLastBlock() {
		if (blockReceivedQueue.isEmpty())
			return WorldManager.getInstance().getBlockchain().getLastBlock();
		return lastBlock;
	}

	/**
	 * Reset the queue of hashes of blocks to be retrieved
	 * and add the best hash to the top of the queue
	 * 
	 * @param hash - the best hash
	 */
	public void setBestHash(byte[] hash) {
		blockHashQueue.clear();
		blockHashQueue.addLast(hash);
	}

	/**
	 * Returns the last added hash to the queue representing 
	 * the latest known block on the network
	 * 
	 * @return The best hash on the network known to the client
	 */
	public byte[] getBestHash() {
		return blockHashQueue.peekLast();
	}

	public void addHash(byte[] hash) {
		blockHashQueue.addLast(hash);
	}
	
	/**
	 * Return a list of hashes from blocks that still need to be downloaded.
	 * 
	 * @param amount - the number of hashes to return
	 * @return A list of hashes for which blocks need to be retrieved.
	 */
	public List<byte[]> getHashes(int amount) {
		List<byte[]> hashes = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			if (!blockHashQueue.isEmpty())
				hashes.add(blockHashQueue.pollLast());
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
	
    public BigInteger getHighestTotalDifficulty() {
		return highestTotalDifficulty;
	}

	public void setHighestTotalDifficulty(BigInteger highestTotalDifficulty) {
		this.highestTotalDifficulty = highestTotalDifficulty;
	}

	/**
	 * Returns the current number of blocks in the queue
	 * 
	 * @return the current number of blocks in the queue
	 */
	public int size() {
		return blockReceivedQueue.size();
	}

	/**
	 * Cancel and purge the timer-thread that 
	 * processes the blocks in the queue
	 */
	public void close() {
		timer.cancel();
		timer.purge();
	}
}
