package org.ethereum.db;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;
import org.ethereum.db.index.ArrayListIndex;
import org.ethereum.db.index.Index;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.Serializers;
import org.ethereum.util.ExecutorPipeline;
import org.ethereum.util.Functional;
import org.mapdb.DB;
import org.mapdb.Serializer;
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

    // Transaction.getSender() is quite heavy operation so we are prefetching this value on several threads
    // to unload the main block importing cycle
    private ExecutorPipeline<Pair<BlockWrapper, Boolean>, Pair<BlockWrapper, Boolean>> exec1 = new ExecutorPipeline<>
            (4, 1000, true, new Functional.Function<Pair<BlockWrapper, Boolean>, Pair<BlockWrapper, Boolean>>() {
                @Override
                public Pair<BlockWrapper, Boolean> apply(Pair<BlockWrapper, Boolean> blockWrapper) {
                    for (Transaction tx : blockWrapper.getLeft().getBlock().getTransactionsList()) {
                        tx.getSender();
                    }
                    return blockWrapper;
                }
            }, new Functional.Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    logger.error("Unexpected exception: ", throwable);
                }
            });

    private ExecutorPipeline<Pair<BlockWrapper, Boolean>, Void> exec2 = exec1.add(1, 1, new Functional.Consumer<Pair<BlockWrapper, Boolean>>() {
        @Override
        public void accept(Pair<BlockWrapper, Boolean> blockWrapper) {
            if (blockWrapper.getRight()) {
                addOrReplaceImpl(blockWrapper.getLeft());
            } else {
                addImpl(blockWrapper.getLeft());
            }
        }
    });

    @Override
    public void open() {
        logger.info("Block queue opened");
    }

    @Override
    public void close() {
    }

    @Override
    public void addOrReplaceAll(Collection<BlockWrapper> blockList) {
        for (BlockWrapper blockWrapper : blockList) {
            addOrReplace(blockWrapper);
        }
    }

    @Override
    public void add(BlockWrapper block) {
        exec1.push(Pair.of(block, false));
    }

    @Override
    public void returnBlock(BlockWrapper block) {
        addImpl(block);
    }

    public void addImpl(BlockWrapper block) {

        if (index.contains(block.getNumber())) return;

        synchronized (mutex) {
            addInner(block);
        }

        fireNotEmpty();
    }

    @Override
    public void addOrReplace(BlockWrapper block) {
        exec1.push(Pair.of(block, true));
    }

    private void addOrReplaceImpl(BlockWrapper block) {

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
