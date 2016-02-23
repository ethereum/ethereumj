package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;
import org.ethereum.db.index.ArrayListIndex;
import org.ethereum.db.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class BlockQueueMem implements BlockQueue {

    private final static Logger logger = LoggerFactory.getLogger("blockqueue");

    private Map<Long, BlockWrapper> blocks = Collections.synchronizedMap(new HashMap<Long, BlockWrapper>());
    private final Index index = new ArrayListIndex(Collections.<Long>emptyList());

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private final Object mutex = new Object();

    @Override
    public void open() {
        logger.info("Block queue opened");
    }

    @Override
    public void close() {
    }

    @Override
    public void addOrReplaceAll(Collection<BlockWrapper> blockList) {

        List<Long> numbers = new ArrayList<>(blockList.size());
        Map<Long, BlockWrapper> newBlocks = new HashMap<>();

        for (BlockWrapper b : blockList) {

            // do not add existing number to index
            if (!index.contains(b.getNumber()) &&
                    !numbers.contains(b.getNumber())) {
                numbers.add(b.getNumber());
            }

            newBlocks.put(b.getNumber(), b);
        }

        synchronized (mutex) {
            blocks.putAll(newBlocks);
            index.addAll(numbers);
        }

        fireNotEmpty();

        logger.debug("Added: " + blockList.size() + ", BlockQueue size: " + blocks.size());
    }

    @Override
    public void add(BlockWrapper block) {

        if (index.contains(block.getNumber())) return;

        synchronized (mutex) {
            addInner(block);
        }

        fireNotEmpty();
    }

    @Override
    public void addOrReplace(BlockWrapper block) {

        synchronized (mutex) {
            if (!index.contains(block.getNumber())) {
                addInner(block);
            } else {
                replaceInner(block);
            }
        }

        fireNotEmpty();
    }

    private void replaceInner(BlockWrapper block) {
        blocks.put(block.getNumber(), block);
    }

    private void addInner(BlockWrapper block) {
        blocks.put(block.getNumber(), block);
        index.add(block.getNumber());
    }

    @Override
    public BlockWrapper poll() {
        return pollInner();
    }

    private BlockWrapper pollInner() {
        synchronized (mutex) {
            if (index.isEmpty()) {
                return null;
            }

            Long idx = index.poll();
            BlockWrapper block = blocks.get(idx);
            blocks.remove(idx);

            if (block == null) {
                logger.error("Block for index {} is null", idx);
            }

            return block;
        }
    }

    @Override
    public BlockWrapper peek() {
        synchronized (mutex) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.peek();
            return blocks.get(idx);
        }
    }

    @Override
    public BlockWrapper take() {
        takeLock.lock();
        try {
            BlockWrapper block;
            while (null == (block = pollInner())) {
                notEmpty.awaitUninterruptibly();
            }
            return block;
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public void clear() {
        blocks.clear();
        index.clear();
    }

    @Override
    public List<byte[]> filterExisting(final Collection<byte[]> hashList) {
        return (List<byte[]>) hashList;
    }

    @Override
    public List<BlockHeader> filterExistingHeaders(Collection<BlockHeader> headers) {
        return (List<BlockHeader>) headers;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return false;
    }

    @Override
    public void drop(byte[] nodeId, int scanLimit) {

        List<Long> removed = new ArrayList<>();

        synchronized (index) {

            boolean hasSent = false;

            for (Long idx : index) {
                BlockWrapper b = blocks.get(idx);

                if (!hasSent) {
                    hasSent = b.sentBy(nodeId);
                }
                if (hasSent) removed.add(idx);
            }

            blocks.keySet().removeAll(removed);
            index.removeAll(removed);
        }

        if (logger.isDebugEnabled()) {
            if (removed.isEmpty()) {
                logger.debug("0 blocks are dropped out");
            } else {
                logger.debug("{} blocks [{}..{}] are dropped out", removed.size(), removed.get(0), removed.get(removed.size() - 1));
            }
        }
    }

    @Override
    public long getLastNumber() {
        Long num = index.peekLast();
        return num == null ? 0 : num;
    }

    @Override
    public BlockWrapper peekLast() {

        synchronized (mutex) {
            Long num = index.peekLast();
            return blocks.get(num);
        }
    }

    @Override
    public void remove(BlockWrapper block) {

        synchronized (mutex) {

            BlockWrapper existing = blocks.get(block.getNumber());
            if (existing == null || !existing.equals(block))
                return;

            index.remove(block.getNumber());
            blocks.remove(block.getNumber());
        }
    }

    private void fireNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            takeLock.unlock();
        }
    }
}
