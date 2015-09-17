package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.mapdb.MapDBFactory;
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
 * @since 16.09.2015
 */
public class HeaderStoreImpl implements HeaderStore {

    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    private static final String STORE_NAME = "headerstore";
    private MapDBFactory mapDBFactory;

    private DB db;
    private Map<Long, BlockHeader> headers;
    private BlockQueueImpl.Index index;

    private boolean initDone = false;
    private final ReentrantLock initLock = new ReentrantLock();
    private final Condition init = initLock.newCondition();

    private final Object writeMutex = new Object();
    private final Object readMutex = new Object();

    @Override
    public void open() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initLock.lock();
                try {
                    db = mapDBFactory.createTransactionalDB(dbName());
                    headers = db.hashMapCreate(STORE_NAME)
                            .keySerializer(Serializer.LONG)
                            .valueSerializer(Serializer.BYTE_ARRAY)
                            .makeOrGet();

                    if(CONFIG.databaseReset()) {
                        headers.clear();
                        db.commit();
                    }

                    index = new BlockQueueImpl.ArrayListIndex(headers.keySet());
                    initDone = true;
                    init.signalAll();

                    logger.info("Header store loaded, size [{}]", size());
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
    public void add(BlockHeader header) {
        awaitInit();

        synchronized (writeMutex) {
            if (index.contains(header.getNumber())) {
                return;
            }
            headers.put(header.getNumber(), header);
        }

        db.commit();
    }

    @Override
    public void addBatch(Collection<BlockHeader> headers) {
        awaitInit();
        synchronized (writeMutex) {
            List<Long> numbers = new ArrayList<>(headers.size());
            for (BlockHeader b : headers) {
                if(!index.contains(b.getNumber()) &&
                        !numbers.contains(b.getNumber())) {

                    this.headers.put(b.getNumber(), b);
                    numbers.add(b.getNumber());
                }
            }

            index.addAll(numbers);
        }
        db.commit();
    }

    @Override
    public BlockHeader peek() {
        awaitInit();

        synchronized (readMutex) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.peek();
            return headers.get(idx);
        }
    }

    @Override
    public BlockHeader poll() {
        awaitInit();

        BlockHeader header = pollInner();
        db.commit();
        return header;
    }

    @Override
    public List<BlockHeader> pollBatch(int qty) {

        if (index.isEmpty()) {
            return Collections.emptyList();
        }

        List<BlockHeader> headers = new ArrayList<>(qty > size() ? qty : size());
        while (headers.size() < qty) {
            BlockHeader header = pollInner();
            if(header == null) {
                break;
            }
            headers.add(header);
        }

        db.commit();

        return headers;
    }

    @Override
    public boolean isEmpty() {
        awaitInit();
        return index.isEmpty();
    }

    @Override
    public int size() {
        awaitInit();
        return index.size();
    }

    @Override
    public void clear() {
        awaitInit();

        headers.clear();
        index.clear();

        db.commit();
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

    private BlockHeader pollInner() {
        synchronized (readMutex) {
            if (index.isEmpty()) {
                return null;
            }

            Long idx = index.poll();
            BlockHeader header = headers.get(idx);
            headers.remove(idx);

            if (header == null) {
                logger.error("Header for index {} is null", idx);
            }

            return header;
        }
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }
}
