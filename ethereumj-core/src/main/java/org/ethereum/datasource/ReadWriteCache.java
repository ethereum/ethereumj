package org.ethereum.datasource;

import java.util.Collection;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class ReadWriteCache<Key, Value> extends SourceDelegateAdapter<Key, Value> implements CachedSource<Key, Value> {
    protected ReadCache<Key, Value> readCache;
    protected WriteCache<Key, Value> writeCache;

    public ReadWriteCache() {
    }

    public ReadWriteCache(Source<Key, Value> src, WriteCache.CacheType cacheType) {
        writeCache = new WriteCache<>(src, cacheType);
        readCache = new ReadCache<>(writeCache);
        this.src = readCache;
    }

    @Override
    public Source<Key, Value> getSrc() {
        return src;
    }

    @Override
    public Collection<Key> getModified() {
        return writeCache.getModified();
    }

    protected Value getCached(Key key) {
        Value v = readCache.getCached(key);
        if (v == null) {
            v = writeCache.getCached(key);
        }
        return v;
    }

    @Override
    public boolean flush() {
        readCache.flush();
        return writeCache.flush();
    }

    @Override
    public long estimateCashSize() {
        return readCache.estimateCashSize() + writeCache.estimateCashSize();
    }

    public static class BytesKey<V> extends ReadWriteCache<byte[], V> {
        public BytesKey(Source<byte[], V> src, WriteCache.CacheType cacheType) {
            this.writeCache = new WriteCache.BytesKey<>(src, cacheType);
            this.readCache = new ReadCache.BytesKey<>(writeCache);
            this.src = readCache;
        }
    }
}
