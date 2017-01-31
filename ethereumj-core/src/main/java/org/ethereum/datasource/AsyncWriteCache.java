package org.ethereum.datasource;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Anton Nashatyrev on 18.01.2017.
 */
public abstract class AsyncWriteCache<Key, Value> extends AbstractCachedSource<Key, Value> {

    private static Executor flushExecutor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("AsyncWriteCacheThread-%d").build());

    protected volatile WriteCache<Key, Value> curCache;
    protected WriteCache<Key, Value> flushingCache;

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

    boolean flushing = false;
    @Override
    public synchronized boolean flush() {
        while (flushing) try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        flushing = true;
        flushingCache.cache = curCache.cache;
        curCache = createCache(flushingCache);
        boolean ret = flushingCache.hasModified();

        flushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                flushingCache.flush();
                synchronized (AsyncWriteCache.this) {
                    flushing = false;
                    AsyncWriteCache.this.notifyAll();
                }
            }
        });
        return ret;
    }

    @Override
    protected synchronized boolean flushImpl() {
        return false;
    }
}
