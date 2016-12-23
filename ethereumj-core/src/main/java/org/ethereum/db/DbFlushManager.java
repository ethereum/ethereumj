package org.ethereum.db;

import org.ethereum.config.SystemProperties;
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

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class DbFlushManager {
    private static final Logger logger = LoggerFactory.getLogger("db");

    List<WriteCache<byte[], byte[]>> writeCaches = new ArrayList<>();
    Set<DbSource> dbSources = new HashSet<>();

    long sizeThreshold;
    int commitsCountThreshold;
    boolean syncDone = false;
    boolean flushAfterSyncDone;

    SystemProperties config;

    int commitCount = 0;

    public DbFlushManager(SystemProperties config, Set<DbSource> dbSources) {
        this.config = config;
        this.dbSources = dbSources;
        sizeThreshold = config.getConfig().getInt("cache.flush.writeCacheSize") * 1024 * 1024;
        commitsCountThreshold = config.getConfig().getInt("cache.flush.blocks");
        flushAfterSyncDone = config.getConfig().getBoolean("cache.flush.shortSyncFlush");
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

    public void addCache(WriteCache<byte[], byte[]> cache) {
        writeCaches.add(cache);
    }

    public long getCacheSize() {
        long ret = 0;
        for (WriteCache<byte[], byte[]> writeCache : writeCaches) {
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

    public synchronized void flush() {
        long s = System.nanoTime();
        for (WriteCache<byte[], byte[]> writeCache : writeCaches) {
            writeCache.flush();
        }
        logger.debug("Flush took " + (System.nanoTime() - s) / 1000000 + " ms");
    }

    /**
     * Flushes all caches and closes all databases
     */
    public synchronized void close() {
        flush();
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

