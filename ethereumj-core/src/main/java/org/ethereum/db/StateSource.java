package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class StateSource extends SourceChainBox<byte[], byte[], byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    JournalSource<byte[]> journalSource;
    CountingBytesSource countingSource;
    ReadCache<byte[], byte[]> readCache;
    WriteCache<byte[], byte[]> writeCache;
    BatchSourceWriter<byte[], byte[]> batchDBWriter;

    public StateSource(BatchSource<byte[], byte[]> src, boolean pruningEnabled) {
        super(src);
        add(batchDBWriter = new BatchSourceWriter<>(src));
        add(writeCache = new WriteCache.BytesKey<>(batchDBWriter, WriteCache.CacheType.SIMPLE));
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        add(readCache = new ReadCache.BytesKey<>(writeCache).withMaxCapacity(16 * 1024 * 1024 / 512)); // 512 - approx size of a node
        add(countingSource = new CountingBytesSource(readCache));
        if (pruningEnabled) {
            add(journalSource = new JournalSource<>(countingSource));
        }
    }

    @Autowired
    public void setConfig(SystemProperties config) {
        int size = config.getConfig().getInt("cache.stateCacheSize");
        readCache.withMaxCapacity(size * 1024 * 1024 / 512); // 512 - approx size of a node
    }

    public JournalSource<byte[]> getJournalSource() {
        return journalSource;
    }

    /**
     * Returns the source behind JournalSource
     */
    public CountingBytesSource getNoJournalSource() {
        return countingSource;
    }

    public WriteCache<byte[], byte[]> getWriteCache() {
        return writeCache;
    }
}
