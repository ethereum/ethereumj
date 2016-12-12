package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.WriteCache;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class DbFlushManager {
    private static final Logger logger = LoggerFactory.getLogger("db");

    List<WriteCache<byte[], byte[]>> writeCaches = new ArrayList<>();
    long sizeThreshold;
    int commitsCountThreshold;
    boolean syncDone = false;
    boolean flushAfterSyncDone;

    SystemProperties config;

    int commitCount = 0;

    public DbFlushManager(SystemProperties config) {
        this.config = config;
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
                logger.info("DbFlushManager: long sync done, flushing each block now");
                syncDone = true;
            }
        });
    }

    public void addCache(WriteCache<byte[], byte[]> cache) {
        writeCaches.add(cache);
    }

    public long getCacheSize() {
        long ret = 0;
        for (WriteCache<byte[], byte[]> writeCache : writeCaches) {
            ret += writeCache.estimateCashSize();
        }
        return ret;
    }

    public void commit() {
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

    public void flush() {
        long s = System.nanoTime();
        for (WriteCache<byte[], byte[]> writeCache : writeCaches) {
            writeCache.flush();
        }
        logger.debug("Flush took " + (System.nanoTime() - s) / 1000000 + " ms");
    }
}

