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

    public static StateSource INST;

    JournalSource<byte[]> journalSource;
    NoDeleteSource<byte[], byte[]> noDeleteSource;

    CountingBytesSource countingSource;
    ReadCache<byte[], byte[]> readCache;
    AbstractCachedSource<byte[], byte[]> writeCache;
    BloomedSource<byte[]> bloomedSource;
    BatchSourceWriter<byte[], byte[]> batchDBWriter;

    public StateSource(BatchSource<byte[], byte[]> src, boolean pruningEnabled) {
        super(src);
        INST = this;
        add(batchDBWriter = new BatchSourceWriter<>(src));
        add(bloomedSource = new BloomedSource<>(batchDBWriter));
//        add(bloomedSource = new BloomedSource<>(src));
        bloomedSource.setFlushSource(true);
//        bloomedSource.startBlooming(new BloomFilter(0.01, 1_000_000));
        add(readCache = new ReadCache.BytesKey<>(bloomedSource).withMaxCapacity(16 * 1024 * 1024 / 512)); // 512 - approx size of a node
        readCache.setFlushSource(true);
        add(countingSource = new CountingBytesSource(readCache));
        countingSource.setFlushSource(true);
        writeCache = new AsyncWriteCache<byte[], byte[]>(countingSource) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<byte[]>(source, WriteCache.CacheType.COUNTING) {
                    @Override
                    public boolean flush() {
                        System.err.println("###### Flush started");
                        boolean ret = super.flush();
                        System.err.println("###### Flush complete");
                        return ret;
                    }
                };
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        };
//        writeCache = new WriteCache.BytesKey<>(countingSource, WriteCache.CacheType.COUNTING);
//        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
//        writeCache.setFlushSource(false);

        add(writeCache);

        if (pruningEnabled) {
            add(journalSource = new JournalSource<>(writeCache));
        } else {
            add(noDeleteSource = new NoDeleteSource<>(writeCache));
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
    public Source<byte[], byte[]> getNoJournalSource() {
        return writeCache;
    }

    public AbstractCachedSource<byte[], byte[]> getWriteCache() {
        return writeCache;
    }

    public ReadCache<byte[], byte[]> getReadCache() {
        return readCache;
    }
}
