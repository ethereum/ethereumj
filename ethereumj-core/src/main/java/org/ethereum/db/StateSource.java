package org.ethereum.db;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class StateSource extends SourceChainBox<byte[], byte[], byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    JournalSource<byte[]> journalSource;
    NoDeleteSource<byte[], byte[]> noDeleteSource;

    CountingBytesSource countingSource;
    ReadCache<byte[], byte[]> readCache;
    WriteCache<byte[], byte[]> writeCache;
    BloomedSource<byte[]> bloomedSource;
    BatchSourceWriter<byte[], byte[]> batchDBWriter;

    public StateSource(BatchSource<byte[], byte[]> src, boolean pruningEnabled) {
        super(src);
        add(batchDBWriter = new BatchSourceWriter<>(src));
        add(bloomedSource = new BloomedSource<>(batchDBWriter));
        bloomedSource.setFlushSource(true);
        add(writeCache = new WriteCache.BytesKey<>(bloomedSource, WriteCache.CacheType.SIMPLE));
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        add(readCache = new ReadCache.BytesKey<>(writeCache).withMaxCapacity(16 * 1024 * 1024 / 512)); // 512 - approx size of a node
        add(countingSource = new CountingBytesSource(readCache));
        if (pruningEnabled) {
            add(journalSource = new JournalSource<>(countingSource));
        } else {
            add(noDeleteSource = new NoDeleteSource<>(countingSource));
        }
    }

    @Autowired
    public void setConfig(SystemProperties config) {
        int size = config.getConfig().getInt("cache.stateCacheSize");
        readCache.withMaxCapacity(size * 1024 * 1024 / 512); // 512 - approx size of a node
    }

    @Autowired
    public void setCommonConfig(CommonConfig commonConfig) {
        if (journalSource != null) {
            journalSource.setJournalStore(commonConfig.cachedDbSource("journal"));
        }
    }

    public JournalSource<byte[]> getJournalSource() {
        return journalSource;
    }

    public BloomedSource<byte[]> getBloomedSource() {
        return bloomedSource;
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
