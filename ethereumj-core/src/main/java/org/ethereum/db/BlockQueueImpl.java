package org.ethereum.db;

import org.ethereum.core.BlockWrapper;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.Serializers;
import org.ethereum.util.CollectionUtils;
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
public class BlockQueueImpl implements BlockQueue {

    private final static Logger logger = LoggerFactory.getLogger("blockqueue");

    private static final int READ_HITS_COMMIT_THRESHOLD = 1000;
    private int readHits;

    private final static String STORE_NAME = "blockqueue";
    private final static String HASH_SET_NAME = "hashset";
    private MapDBFactory mapDBFactory;

    private DB db;
    private Map<Long, BlockWrapper> blocks;
    private Set<byte[]> hashes;
    private Index index;

    private boolean initDone = false;
    private final ReentrantLock initLock = new ReentrantLock();
    private final Condition init = initLock.newCondition();

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private final Object writeMutex = new Object();
    private final Object readMutex = new Object();

    @Override
    public void open() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initLock.lock();
                    db = mapDBFactory.createTransactionalDB(dbName());
                    blocks = db.hashMapCreate(STORE_NAME)
                            .keySerializer(Serializer.LONG)
                            .valueSerializer(Serializers.BLOCK_WRAPPER)
                            .makeOrGet();
                    hashes = db.hashSetCreate(HASH_SET_NAME)
                            .serializer(Serializer.BYTE_ARRAY)
                            .makeOrGet();

                    if(CONFIG.databaseReset()) {
                        blocks.clear();
                        hashes.clear();
                        db.commit();
                    }

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
        db.close();
        initDone = false;
    }

    @Override
    public void addAll(Collection<BlockWrapper> blockList) {
        awaitInit();
        synchronized (writeMutex) {
            List<Long> numbers = new ArrayList<>(blockList.size());
            Set<byte[]> newHashes = new HashSet<>();
            for (BlockWrapper b : blockList) {
                if(!index.contains(b.getNumber()) &&
                   !numbers.contains(b.getNumber())) {

                    blocks.put(b.getNumber(), b);
                    numbers.add(b.getNumber());
                    newHashes.add(b.getHash());
                }
            }
            hashes.addAll(newHashes);

            takeLock.lock();
            try {
                index.addAll(numbers);
                notEmpty.signalAll();
            } finally {
                takeLock.unlock();
            }
        }
        db.commit();
    }

    @Override
    public void add(BlockWrapper block) {
        awaitInit();
        synchronized (writeMutex) {
            if (index.contains(block.getNumber())) {
                return;
            }
            blocks.put(block.getNumber(), block);
            hashes.add(block.getHash());

            takeLock.lock();
            try {
                index.add(block.getNumber());
                notEmpty.signalAll();
            } finally {
                takeLock.unlock();
            }
        }
        db.commit();
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
                hashes.remove(block.getHash());
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
        synchronized(this) {
            blocks.clear();
            hashes.clear();
            index.clear();
        }
        db.commit();
    }

    @Override
    public List<byte[]> filterExisting(final Collection<byte[]> hashList) {
        awaitInit();
        return CollectionUtils.selectList(hashList, new Functional.Predicate<byte[]>() {
            @Override
            public boolean test(byte[] hash) {
                return !hashes.contains(hash);
            }
        });
    }

    @Override
    public Set<byte[]> getHashes() {
        awaitInit();
        return hashes;
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
            db.commit();
            readHits = 0;
        }
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }

    private interface Index {

        void addAll(Collection<Long> nums);

        void add(Long num);

        Long peek();

        Long poll();

        boolean contains(Long num);

        boolean isEmpty();

        int size();

        void clear();
    }

    private class ArrayListIndex implements Index {

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
    }
}
