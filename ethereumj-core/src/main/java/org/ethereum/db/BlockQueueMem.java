package org.ethereum.db;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;
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

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class BlockQueueMem implements BlockQueue {

    private final static Logger logger = LoggerFactory.getLogger("blockqueue");

    private static final int READ_HITS_COMMIT_THRESHOLD = 1000;
    private int readHits;

    private final static String STORE_NAME = "blockqueue";
    private final static String HASH_SET_NAME = "hashset";
    private MapDBFactory mapDBFactory;

    private Map<Long, BlockWrapper> blocks = Collections.synchronizedMap(new HashMap<Long, BlockWrapper>());
    private Set<ByteArrayWrapper> hashes = Collections.synchronizedSet(new HashSet<ByteArrayWrapper>());
    private Index index = new ArrayListIndex(Collections.<Long>emptyList());

    private boolean initDone = false;
    private final ReentrantLock initLock = new ReentrantLock();
    private final Condition init = initLock.newCondition();

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private final Object writeMutex = new Object();
    private final Object readMutex = new Object();

    private ExecutorPipeline<Pair<BlockWrapper, Boolean>, Pair<BlockWrapper, Boolean>> exec1 = new ExecutorPipeline<>
            (8, 1000, true, new Functional.Function<Pair<BlockWrapper, Boolean>, Pair<BlockWrapper, Boolean>>() {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initLock.lock();
//                    db = mapDBFactory.createTransactionalDB(dbName());
//                    blocks = db.hashMapCreate(STORE_NAME)
//                            .keySerializer(Serializer.LONG)
//                            .valueSerializer(Serializers.BLOCK_WRAPPER)
//                            .makeOrGet();
//                    hashes = db.hashSetCreate(HASH_SET_NAME)
//                            .serializer(Serializers.BYTE_ARRAY_WRAPPER)
//                            .makeOrGet();
//
//                    if(CONFIG.databaseReset()) {
//                        blocks.clear();
//                        hashes.clear();
//                        db.commit();
//                    }

                    index = new ArrayListIndex(blocks.keySet());
                    initDone = true;
                    readHits = 0;
                    init.signalAll();

                    logger.info("Block queue loaded, size [{}]", size());
                } finally {
                    initLock.unlock();
                }
            }
        }).start();
    }

    private String dbName() {
        return String.format("%s/%s", STORE_NAME, STORE_NAME);
    }

    @Override
    public void close() {
        awaitInit();
//        db.close();
        initDone = false;
    }

    @Override
    public void addOrReplaceAll(Collection<BlockWrapper> blockList) {
        awaitInit();
        for (BlockWrapper blockWrapper : blockList) {
            addOrReplace(blockWrapper);
        }
//        synchronized (writeMutex) {
//            List<Long> numbers = new ArrayList<>(blockList.size());
//            Set<ByteArrayWrapper> newHashes = new HashSet<>();
//            for (BlockWrapper b : blockList) {
//
//                if (!index.contains(b.getNumber())) {
//
//                    if (!numbers.contains(b.getNumber())) {
//                        numbers.add(b.getNumber());
//                        blocks.put(b.getNumber(), b);
//                        newHashes.add(new ByteArrayWrapper(b.getHash()));
//                    }
//
//                } else  {
//                    replaceInner(b);
//                }
//            }
//            hashes.addAll(newHashes);
//
//            logger.debug("Added: " + blockList.size() + ", BlockQueue size: " + blocks.size());
//
//            takeLock.lock();
//            try {
//                index.addAll(numbers);
//                notEmpty.signalAll();
//            } finally {
//                takeLock.unlock();
//            }
//        }
//        db.commit();
    }

    @Override
    public void add(BlockWrapper block) {
        exec1.push(Pair.of(block, false));
    }

    public void addImpl(BlockWrapper block) {
        awaitInit();
        synchronized (writeMutex) {

            if (!index.contains(block.getNumber())) {
                addInner(block);
            }

        }
//        db.commit();
    }

    @Override
    public void addOrReplace(BlockWrapper block) {
        awaitInit();
        exec1.push(Pair.of(block, true));
    }

    private void addOrReplaceImpl(BlockWrapper block) {
        synchronized (writeMutex) {

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
        awaitInit();
        BlockWrapper block = pollInner();
        commitReading();
        return block;
    }

    private BlockWrapper pollInner() {
        synchronized (readMutex) {
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
        awaitInit();
        synchronized (readMutex) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.peek();
            return blocks.get(idx);
        }
    }

    @Override
    public BlockWrapper take() {
        awaitInit();
        takeLock.lock();
        try {
            BlockWrapper block;
            while (null == (block = pollInner())) {
                notEmpty.awaitUninterruptibly();
            }
            commitReading();
            return block;
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public int size() {
        awaitInit();
        return index.size();
    }

    @Override
    public boolean isEmpty() {
        awaitInit();
        return index.isEmpty();
    }

    @Override
    public void clear() {
        awaitInit();

        blocks.clear();
        hashes.clear();
        index.clear();

//        db.commit();
    }

    @Override
    public List<byte[]> filterExisting(final Collection<byte[]> hashList) {
        awaitInit();

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
        awaitInit();

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
        awaitInit();

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

//        db.commit();

        if (logger.isDebugEnabled()) {
            if (removed.isEmpty()) {
                logger.debug("0 blocks are dropped out");
            } else {
                logger.debug("[{}..{}] blocks are dropped out", removed.get(0), removed.get(removed.size() - 1));
            }
        }
    }


    private void awaitInit() {
        initLock.lock();
        try {
            if(!initDone) {
                init.awaitUninterruptibly();
            }
        } finally {
            initLock.unlock();
        }
    }

    private void commitReading() {
        if(++readHits >= READ_HITS_COMMIT_THRESHOLD) {
//            db.commit();
            readHits = 0;
        }
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }

    public interface Index extends Iterable<Long> {

        void addAll(Collection<Long> nums);

        void add(Long num);

        Long peek();

        Long poll();

        boolean contains(Long num);

        boolean isEmpty();

        int size();

        void clear();

        void removeAll(Collection<Long> indexes);
    }

    public static class ArrayListIndex implements Index {

        private List<Long> index;

        public ArrayListIndex(Collection<Long> numbers) {
            index = new ArrayList<>(numbers);
            sort();
        }

        @Override
        public synchronized void addAll(Collection<Long> nums) {
            index.addAll(nums);
            sort();
        }

        @Override
        public synchronized void add(Long num) {
            index.add(num);
            sort();
        }

        @Override
        public synchronized Long peek() {
            return index.get(0);
        }

        @Override
        public synchronized Long poll() {
            Long num = index.get(0);
            index.remove(0);
            return num;
        }

        @Override
        public synchronized boolean contains(Long num) {
            return Collections.binarySearch(index, num) >= 0;
        }

        @Override
        public boolean isEmpty() {
            return index.isEmpty();
        }

        @Override
        public int size() {
            return index.size();
        }

        @Override
        public synchronized void clear() {
            index.clear();
        }

        private void sort() {
            Collections.sort(index);
        }

        @Override
        public Iterator<Long> iterator() {
            return index.iterator();
        }

        public synchronized void removeAll(Collection<Long> indexes) {
            index.removeAll(indexes);
        }
    }
}
