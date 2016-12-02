package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class StateSource extends SourceDelegateAdapter<byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    JournalBytesSource journalSource;
    CountingBytesSource countingSource;
    ReadCache<byte[], byte[]> readCache;
    WriteCache<byte[], byte[]> writeCache;
    BatchSourceWriter<byte[], byte[]> batchDBWriter;

    public StateSource(BatchSource<byte[], byte[]> src, boolean pruningEnabled) {
        batchDBWriter = new BatchSourceWriter<>(src);
        writeCache = new WriteCache.BytesKey<>(batchDBWriter, WriteCache.CacheType.SIMPLE);
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        readCache = new ReadCache.BytesKey<>(writeCache).withMaxCapacity(16 * 1024 * 1024 / 512); // 512 - approx size of a node
        countingSource = new CountingBytesSource(readCache);
        if (!pruningEnabled) {
            this.delegate = countingSource;
        } else {
            journalSource = new JournalBytesSource(countingSource);
            this.delegate = journalSource;
        }
    }

    @Autowired
    public void setConfig(SystemProperties config) {
        int size = config.getConfig().getInt("cache.stateCacheSize");
        readCache.withMaxCapacity(size * 1024 * 1024 / 512); // 512 - approx size of a node
    }

    @Override
    public boolean flush() {
        return writeCache.flush();
    }

    public JournalBytesSource getJournalSource() {
        return journalSource;
    }

    public WriteCache<byte[], byte[]> getWriteCache() {
        return writeCache;
    }
}
