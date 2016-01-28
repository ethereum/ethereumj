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
    private Set<ByteArrayWrapper> hashes = Collections.synchronizedSet(new HashSet<ByteArrayWrapper>());
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
        synchronized (mutex) {
            List<Long> numbers = new ArrayList<>(blockList.size());
            Set<ByteArrayWrapper> newHashes = new HashSet<>();
            for (BlockWrapper b : blockList) {

                if (!index.contains(b.getNumber())) {

                    if (!numbers.contains(b.getNumber())) {
                        numbers.add(b.getNumber());
                        blocks.put(b.getNumber(), b);
                        newHashes.add(new ByteArrayWrapper(b.getHash()));
                    }

                } else  {
                    replaceInner(b);
                }
            }
            hashes.addAll(newHashes);

            logger.debug("Added: " + blockList.size() + ", BlockQueue size: " + blocks.size());

            takeLock.lock();
            try {
                index.addAll(numbers);
                notEmpty.signalAll();
            } finally {
                takeLock.unlock();
            }
        }
    }

    @Override
    public void add(BlockWrapper block) {
        synchronized (mutex) {

            if (!index.contains(block.getNumber())) {
                addInner(block);
            }

        }
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
    }

    private void replaceInner(BlockWrapper block) {

        BlockWrapper old = blocks.get(block.getNumber());

        if (block.isEqual(old)) return;

        if (old != null) {
            hashes.remove(new ByteArrayWrapper(old.getHash()));
        }

        blocks.put(block.getNumber(), block);
        hashes.add(new ByteArrayWrapper(block.getHash()));
    }

    private void addInner(BlockWrapper block) {
        blocks.put(block.getNumber(), block);
        hashes.add(new ByteArrayWrapper(block.getHash()));

        takeLock.lock();
        try {
            index.add(block.getNumber());
            notEmpty.signalAll();
        } finally {
            takeLock.unlock();
        }
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

            if (block != null) {
                hashes.remove(new ByteArrayWrapper(block.getHash()));
            } else {
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
        hashes.clear();
        index.clear();
    }

    @Override
    public List<byte[]> filterExisting(final Collection<byte[]> hashList) {
        List<byte[]> filtered = new ArrayList<>();
        for (byte[] hash : hashList) {
            if (!hashes.contains(new ByteArrayWrapper(hash))) {
                filtered.add(hash);
            }
        }

        return filtered;
    }

    @Override
    public List<BlockHeader> filterExistingHeaders(Collection<BlockHeader> headers) {
        List<BlockHeader> filtered = new ArrayList<>();
        for (BlockHeader header : headers) {
            if (!hashes.contains(new ByteArrayWrapper(header.getHash()))) {
                filtered.add(header);
            }
        }

        return filtered;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return hashes.contains(new ByteArrayWrapper(hash));
    }

    @Override
    public void drop(byte[] nodeId, int scanLimit) {

        int i = 0;
        List<Long> removed = new ArrayList<>();

        synchronized (index) {

            for (Long idx : index) {
                if (++i > scanLimit) break;

                BlockWrapper b = blocks.get(idx);
                if (b.sentBy(nodeId)) removed.add(idx);
            }

            blocks.keySet().removeAll(removed);
            index.removeAll(removed);
        }

        if (logger.isDebugEnabled()) {
            if (removed.isEmpty()) {
                logger.debug("0 blocks are dropped out");
            } else {
                logger.debug("[{}..{}] blocks are dropped out", removed.get(0), removed.get(removed.size() - 1));
            }
        }
    }
}
