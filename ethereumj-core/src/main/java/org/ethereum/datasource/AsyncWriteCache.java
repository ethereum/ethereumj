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

    protected WriteCache<Key, Value> curCache;
    protected volatile WriteCache<Key, Value> flushingCache;

    public AsyncWriteCache(Source<Key, Value> source) {
        super(source);
        curCache = createCache(source);
    }

    protected abstract WriteCache<Key, Value> createCache(Source<Key, Value> source);

    @Override
    public synchronized Collection<Key> getModified() {
        return curCache.getModified();
    }

    @Override
    public boolean hasModified() {
        return curCache.hasModified();
    }

    @Override
    public synchronized void put(Key key, Value val) {
        curCache.put(key, val);
    }

    @Override
    public synchronized void delete(Key key) {
        curCache.delete(key);
    }

    @Override
    public Value get(Key key) {
        WriteCache<Key, Value> curCache;
        synchronized(this) {
            curCache = this.curCache;
            Value value = curCache.getCached(key);
            if (value != null) return value == WriteCache.NULL ? null : value;
            if (flushingCache != null) {
                value = flushingCache.getCached(key);
                if (value != null) return value == WriteCache.NULL ? null : value;
            }
        }
        return curCache.get(key);
    }

    @Override
    protected synchronized boolean flushImpl() {
        while (flushingCache != null) try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        flushingCache = curCache;
        curCache = createCache(getSource());
        boolean ret = flushingCache.hasModified();
        flushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                flushingCache.flush();
                synchronized (AsyncWriteCache.this) {
                    flushingCache = null;
                    AsyncWriteCache.this.notifyAll();
                }
            }
        });
        return ret;
    }
}
