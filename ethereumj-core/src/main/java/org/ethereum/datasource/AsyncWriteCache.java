package org.ethereum.datasource;

import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Anton Nashatyrev on 18.01.2017.
 */
public abstract class AsyncWriteCache<Key, Value> extends AbstractCachedSource<Key, Value> implements AsyncFlushable {

    private static ListeningExecutorService flushExecutor = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("AsyncWriteCacheThread-%d").build()));

    protected volatile WriteCache<Key, Value> curCache;
    protected WriteCache<Key, Value> flushingCache;

    private ListenableFuture<Boolean> lastFlush = Futures.immediateFuture(false);

    public AsyncWriteCache(Source<Key, Value> source) {
        super(source);
        flushingCache = createCache(source);
        flushingCache.setFlushSource(true);
        curCache = createCache(flushingCache);
    }

    protected abstract WriteCache<Key, Value> createCache(Source<Key, Value> source);

    @Override
    public Collection<Key> getModified() {
        return curCache.getModified();
    }

    @Override
    public boolean hasModified() {
        return curCache.hasModified();
    }

    @Override
    public void put(Key key, Value val) {
        curCache.put(key, val);
    }

    @Override
    public void delete(Key key) {
        curCache.delete(key);
    }

    @Override
    public Value get(Key key) {
        return curCache.get(key);
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

        flushingCache.cache = curCache.cache;
        curCache = createCache(flushingCache);

        lastFlush = flushExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return flushingCache.flush();
            }
        });
        return lastFlush;
    }

    @Override
    protected synchronized boolean flushImpl() {
        return false;
    }
}
