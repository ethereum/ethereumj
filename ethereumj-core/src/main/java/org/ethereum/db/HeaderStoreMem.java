package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.Serializers;
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
public class HeaderStoreMem implements HeaderStore {

    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    private static final String STORE_NAME = "headerstore";
    private MapDBFactory mapDBFactory;

//    private DB db;
    private Map<Long, BlockHeader> headers = Collections.synchronizedMap(new HashMap<Long, BlockHeader>());
    private BlockQueueImpl.Index index = new BlockQueueImpl.ArrayListIndex(Collections.<Long>emptySet());

    private boolean initDone = false;
    private final ReentrantLock initLock = new ReentrantLock();
    private final Condition init = initLock.newCondition();

    private final Object mutex = new Object();

    @Override
    public void open() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initLock.lock();
                try {
//                    db = mapDBFactory.createTransactionalDB(dbName());
//                    headers = db.hashMapCreate(STORE_NAME)
//                            .keySerializer(Serializer.LONG)
//                            .valueSerializer(Serializers.BLOCK_HEADER)
//                            .makeOrGet();
//
//                    if(CONFIG.databaseReset()) {
//                        headers.clear();
//                        db.commit();
//                    }

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
//        db.close();
        initDone = false;
    }

    @Override
    public void add(BlockHeader header) {
        awaitInit();

        synchronized (mutex) {
            if (index.contains(header.getNumber())) {
                return;
            }
            headers.put(header.getNumber(), header);
            index.add(header.getNumber());
        }

        dbCommit("add");
    }

    @Override
    public void addBatch(Collection<BlockHeader> headers) {
        awaitInit();
        synchronized (mutex) {
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
        dbCommit("addBatch: " + headers.size());
    }

    @Override
    public BlockHeader peek() {
        awaitInit();

        synchronized (mutex) {
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
        dbCommit("poll");
        return header;
    }

    @Override
    public List<BlockHeader> pollBatch(int qty) {
        awaitInit();

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

        dbCommit("pollBatch: " + headers.size());

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

        dbCommit();
    }

    private void dbCommit() {
        dbCommit("");
    }

    private void dbCommit(String info) {
//        long s = System.currentTimeMillis();
//        db.commit();
//        logger.debug("HashStoreImpl: db.commit took " + (System.currentTimeMillis() - s) + " ms (" + info + ") " + Thread.currentThread().getName());
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
        synchronized (mutex) {
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
