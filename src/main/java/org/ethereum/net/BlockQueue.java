package org.ethereum.net;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 27/07/2014 11:28
 */

public class BlockQueue {

    private static Logger logger = LoggerFactory.getLogger("blockchain");

    private Queue<Block> blockQueue = new ConcurrentLinkedQueue<>();
    private Block lastBlock;

    private Timer timer = new Timer();

    public BlockQueue() {

        timer.scheduleAtFixedRate (new TimerTask() {

            public void run() {
                nudgeQueue();
            }
        }, 10, 10);
    }

    private void nudgeQueue() {
        if (blockQueue.isEmpty()) return;

        Block block = blockQueue.poll();

        WorldManager.getInstance().getBlockchain().add(block);
    }

    public void addBlocks(List<Block> blockList) {

        Block lastReceivedBlock = blockList.get(blockList.size() - 1);
        if (lastReceivedBlock.getNumber() != getLast().getNumber() + 1) return;

        for (int i = blockList.size() - 1; i >= 0; --i) {

            if (blockQueue.size() >
                    SystemProperties.CONFIG.maxBlocksQueued()) return;

            this.lastBlock = blockList.get(i);
            logger.trace("Last block now index: [ {} ]", lastBlock.getNumber());
            blockQueue.add(lastBlock);
        }

        logger.trace("Blocks waiting to be proceed in the queue: [ {} ]", blockQueue.size());
    }

    public Block getLast() {

        if (blockQueue.isEmpty())
            return WorldManager.getInstance().getBlockchain().getLastBlock();

        return lastBlock;
    }

    private class BlockByIndexComparator implements Comparator<Block> {

        @Override
        public int compare(Block o1, Block o2) {

            if (o1 == null || o2 == null ) throw new NullPointerException();

            if (o1.getNumber() > o2.getNumber()) return 1;
            if (o1.getNumber() < o2.getNumber()) return -1;

            return 0;
        }
    }

    public int size() {
        return blockQueue.size();
    }

}
