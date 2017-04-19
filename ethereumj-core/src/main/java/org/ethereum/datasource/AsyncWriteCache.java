/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.datasource;

import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.ethereum.util.ALock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Anton Nashatyrev on 18.01.2017.
 */
public abstract class AsyncWriteCache<Key, Value> extends AbstractCachedSource<Key, Value> implements AsyncFlushable {
    private static final Logger logger = LoggerFactory.getLogger("db");

    private static ListeningExecutorService flushExecutor = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("AsyncWriteCacheThread-%d").build()));

    protected volatile WriteCache<Key, Value> curCache;
    protected WriteCache<Key, Value> flushingCache;

    private ListenableFuture<Boolean> lastFlush = Futures.immediateFuture(false);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ALock rLock = new ALock(rwLock.readLock());
    private final ALock wLock = new ALock(rwLock.writeLock());

    private String name = "<null>";

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
            flipStorage();
            flushAsync();
            return flushingCache.hasModified();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    Entry<Value> getCached(Key key) {
        return curCache.getCached(key);
    }

    @Override
    public synchronized void flipStorage() throws InterruptedException {
        // if previous flush still running
        try {
            if (!lastFlush.isDone()) logger.debug("AsyncWriteCache (" + name + "): waiting for previous flush to complete");
            lastFlush.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        try (ALock l = wLock.lock()) {
            flushingCache.cache = curCache.cache;
            curCache = createCache(flushingCache);
        }
    }

    public synchronized ListenableFuture<Boolean> flushAsync() throws InterruptedException {
        logger.debug("AsyncWriteCache (" + name + "): flush submitted");
        lastFlush = flushExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                logger.debug("AsyncWriteCache (" + name + "): flush started");
                long s = System.currentTimeMillis();
                boolean ret = flushingCache.flush();
                logger.debug("AsyncWriteCache (" + name + "): flush completed in " + (System.currentTimeMillis() - s) + " ms");
                return ret;
            }
        });
        return lastFlush;
    }

    @Override
    public long estimateCacheSize() {
        // 2.0 is upper cache size estimation to take into account there are two
        // caches may exist simultaneously up to doubling cache size
        return (long) (curCache.estimateCacheSize() * 2.0);
    }

    @Override
    protected synchronized boolean flushImpl() {
        return false;
    }

    public AsyncWriteCache<Key, Value> withName(String name) {
        this.name = name;
        return this;
    }
}
