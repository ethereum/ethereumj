package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.WriteCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class DbFlushManager {

    List<WriteCache<byte[], byte[]>> writeCaches = new ArrayList<>();
    long flushThreshold;

    SystemProperties config;

    public DbFlushManager(SystemProperties config) {
        this.config = config;
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
        if (getCacheSize() >= flushThreshold) {
            flush();
        }
    }

    public void flush() {
        for (WriteCache<byte[], byte[]> writeCache : writeCaches) {
            writeCache.flush();
        }
    }
}

