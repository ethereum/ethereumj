package org.ethereum.db;

import org.ethereum.datasource.*;

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

    public StateSource(BatchSource<byte[], byte[]> src) {
        batchDBWriter = new BatchSourceWriter<>(src);
        writeCache = new WriteCache.BytesKey<>(batchDBWriter, WriteCache.CacheType.SIMPLE);
        readCache = new ReadCache.BytesKey<>(writeCache).withMaxCapacity(512 * 1024 * 1024 / 512); // 512 - approx size of a node
        countingSource = new CountingBytesSource(readCache);
        journalSource = new JournalBytesSource(countingSource);
        this.src = journalSource;
    }

    @Override
    public boolean flush() {
        journalSource.flush();
        countingSource.flush();
        readCache.flush();
        writeCache.flush();
        return batchDBWriter.flush();
    }
}
