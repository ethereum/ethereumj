package org.ethereum.datasource.test;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public abstract class MultiCache<V extends Source> extends CachedSource.Simple<byte[], V> {

    Map<byte[], V> ownCaches;

    public MultiCache(Source<byte[], V> src) {
        super(src);
    }

    @Override
    public V get(byte[] key) {
        V ownCache = ownCaches.get(key);
        if (ownCache == null) {
            V v = super.get(key);
            ownCache = create(key, v);
        }
        return ownCache;
    }

    @Override
    public void flush() {
        for (V ownCache : ownCaches.values()) {
            ownCache.flush();
        }
    }

    protected abstract V create(byte[] key, V srcCache);
}
