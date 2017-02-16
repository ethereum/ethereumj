package org.ethereum.datasource;

import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.ethereum.util.ALock;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Anton Nashatyrev on 18.01.2017.
 */
public abstract class AsyncWriteCache<Key, Value> extends AbstractCachedSource<Key, Value> implements AsyncFlushable {

    private static ListeningExecutorService flushExecutor = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("AsyncWriteCacheThread-%d").build()));

    protected volatile WriteCache<Key, Value> curCache;
    protected WriteCache<Key, Value> flushingCache;

    private ListenableFuture<Boolean> lastFlush = Futures.immediateFuture(false);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ALock rLock = new ALock(rwLock.readLock());
    private final ALock wLock = new ALock(rwLock.writeLock());

    public AsyncWriteCache(Source<Key, Value> source) {
        super(source);
        flushingCache = createCache(source);
        flushingCache.setFlushSource(true);
        curCache = createCache(flushingCache);
    }

    protected abstract WriteCache<Key, Value> createCache(Source<Key, Value> source);

    @Override
    public Collection<Key> getModified() {
        try (ALock l = rLock.lock()) {
            return curCache.getModified();
        }
    }

    @Override
    public boolean hasModified() {
        try (ALock l = rLock.lock()) {
            return curCache.hasModified();
        }
    }

    @Override
    public void put(Key key, Value val) {
        try (ALock l = rLock.lock()) {
            curCache.put(key, val);
        }
    }

    @Override
    public void delete(Key key) {
        try (ALock l = rLock.lock()) {
            curCache.delete(key);
        }
    }

    @Override
    public Value get(Key key) {
        try (ALock l = rLock.lock()) {
            return curCache.get(key);
        }
    }

    @Override
    public synchronized boolean flush() {
        try {
            flushAsync();
            return flushingCache.hasModified();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized ListenableFuture<Boolean> flushAsync() throws InterruptedException {
        // if previous flush still running
        try {
            lastFlush.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        try (ALock l = wLock.lock()) {
            flushingCache.cache = curCache.cache;
            curCache = createCache(flushingCache);
        }

        lastFlush = flushExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return flushingCache.flush();
            }
        });
        return lastFlush;
    }

    @Override
    public long estimateCacheSize() {
        return (long) (curCache.estimateCacheSize() * 0.7);
    }

    @Override
    protected synchronized boolean flushImpl() {
        return false;
    }
}
