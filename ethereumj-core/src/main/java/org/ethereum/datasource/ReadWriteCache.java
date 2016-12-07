package org.ethereum.datasource;

import java.util.Collection;

/**
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class ReadWriteCache<Key, Value> extends SourceChainBox<Key, Value, Key, Value> implements CachedSource<Key, Value> {
    protected ReadCache<Key, Value> readCache;
    protected WriteCache<Key, Value> writeCache;

    protected ReadWriteCache(Source<Key, Value> source) {
        super(source);
    }

    public ReadWriteCache(Source<Key, Value> src, WriteCache.CacheType cacheType) {
        super(src);
        add(writeCache = new WriteCache<>(src, cacheType));
        add(readCache = new ReadCache<>(writeCache));
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
    public long estimateCashSize() {
        return readCache.estimateCashSize() + writeCache.estimateCashSize();
    }

    public static class BytesKey<V> extends ReadWriteCache<byte[], V> {
        public BytesKey(Source<byte[], V> src, WriteCache.CacheType cacheType) {
            super(src);
            add(this.writeCache = new WriteCache.BytesKey<>(src, cacheType));
            add(this.readCache = new ReadCache.BytesKey<>(writeCache));
        }
    }
}
