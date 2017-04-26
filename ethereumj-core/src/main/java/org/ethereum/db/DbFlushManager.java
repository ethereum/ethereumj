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
package org.ethereum.db;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.AbstractCachedSource;
import org.ethereum.datasource.AsyncFlushable;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.WriteCache;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class DbFlushManager {
    private static final Logger logger = LoggerFactory.getLogger("db");

    List<AbstractCachedSource<byte[], byte[]>> writeCaches = new ArrayList<>();
    Set<DbSource> dbSources = new HashSet<>();
    AbstractCachedSource<byte[], byte[]> stateDbCache;

    long sizeThreshold;
    int commitsCountThreshold;
    boolean syncDone = false;
    boolean flushAfterSyncDone;

    SystemProperties config;

    int commitCount = 0;

    private final BlockingQueue<Runnable> executorQueue = new ArrayBlockingQueue<>(1);
    private final ExecutorService flushThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            executorQueue, new ThreadFactoryBuilder().setNameFormat("DbFlushManagerThread-%d").build());
    Future<Boolean> lastFlush = Futures.immediateFuture(false);

    public DbFlushManager(SystemProperties config, Set<DbSource> dbSources, AbstractCachedSource<byte[], byte[]> stateDbCache) {
        this.config = config;
        this.dbSources = dbSources;
        sizeThreshold = config.getConfig().getInt("cache.flush.writeCacheSize") * 1024 * 1024;
        commitsCountThreshold = config.getConfig().getInt("cache.flush.blocks");
        flushAfterSyncDone = config.getConfig().getBoolean("cache.flush.shortSyncFlush");
        this.stateDbCache = stateDbCache;
    }

    @Autowired
    public void setEthereumListener(CompositeEthereumListener listener) {
        if (!flushAfterSyncDone) return;
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                if (state == SyncState.COMPLETE) {
                    logger.info("DbFlushManager: long sync done, flushing each block now");
                    syncDone = true;
                }
            }
        });
    }

    public void setSizeThreshold(long sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public void addCache(AbstractCachedSource<byte[], byte[]> cache) {
        writeCaches.add(cache);
    }

    public long getCacheSize() {
        long ret = 0;
        for (AbstractCachedSource<byte[], byte[]> writeCache : writeCaches) {
            ret += writeCache.estimateCacheSize();
        }
        return ret;
    }

    public synchronized void commit(Runnable atomicUpdate) {
        atomicUpdate.run();
        commit();
    }

    public synchronized void commit() {
        long cacheSize = getCacheSize();
        if (sizeThreshold >= 0 && cacheSize >= sizeThreshold) {
            logger.info("DbFlushManager: flushing db due to write cache size (" + cacheSize + ") reached threshold (" + sizeThreshold + ")");
            flush();
        } else if (commitsCountThreshold > 0 && commitCount >= commitsCountThreshold) {
            logger.info("DbFlushManager: flushing db due to commits (" + commitCount + ") reached threshold (" + commitsCountThreshold + ")");
            flush();
            commitCount = 0;
        } else if (flushAfterSyncDone && syncDone) {
            logger.debug("DbFlushManager: flushing db due to short sync");
            flush();
        }
        commitCount++;
    }

    public synchronized void flushSync() {
        try {
            flush().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Future<Boolean> flush() {
        if (!lastFlush.isDone()) {
            logger.info("Waiting for previous flush to complete...");
            try {
                lastFlush.get();
            } catch (Exception e) {
                logger.error("Error during last flush", e);
            }
        }
        logger.debug("Flipping async storages");
        for (AbstractCachedSource<byte[], byte[]> writeCache : writeCaches) {
            try {
                if (writeCache instanceof AsyncFlushable) {
                    ((AsyncFlushable) writeCache).flipStorage();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        logger.debug("Submitting flush task");
        return lastFlush = flushThread.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean ret = false;
                long s = System.nanoTime();
                logger.info("Flush started");

                for (AbstractCachedSource<byte[], byte[]> writeCache : writeCaches) {
                    if (writeCache instanceof AsyncFlushable) {
                        try {
                            ret |= ((AsyncFlushable) writeCache).flushAsync().get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        ret |= writeCache.flush();
                    }
                }
                if (stateDbCache != null) {
                    logger.debug("Flushing to DB");
                    stateDbCache.flush();
                }
                logger.info("Flush completed in " + (System.nanoTime() - s) / 1000000 + " ms");

                return ret;
            }
        });
    }

    /**
     * Flushes all caches and closes all databases
     */
    public synchronized void close() {
        logger.info("Flushing DBs...");
        flushSync();
        logger.info("Flush done.");
        for (DbSource dbSource : dbSources) {
            logger.info("Closing DB: {}", dbSource.getName());
            try {
                dbSource.close();
            } catch (Exception ex) {
                logger.error(String.format("Caught error while closing DB: %s", dbSource.getName()), ex);
            }
        }
    }
}

