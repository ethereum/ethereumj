package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 11.11.2016.
 */
public class CountingCachedSource<V> implements CachedSource.BytesKey<V>, HashedKeySource<byte[], V> {

    private static class CacheEntry<V> {
        V value;
        int counter = 0;

        public CacheEntry(V value) {
            this.value = value;
        }
    }

    private Source<byte[], V> src;

    private Map<byte[], CacheEntry<V>> cache = new ByteArrayMap<>();

    public CountingCachedSource(Source<byte[], V> src) {
        this.src = src;
    }

    @Override
    public Source<byte[], V> getSrc() {
        return src;
    }

    @Override
    public Collection<byte[]> getModified() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Map<byte[], V> getCache() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null)  {
            delete(key);
            return;
        }
        CacheEntry<V> curVal = cache.get(key);
        if (curVal == null) {
            curVal = new CacheEntry<>(val);
            cache.put(key, curVal);
        }
        // though value shouldn't change it could be null (non-existing)
        curVal.value = val;
        curVal.counter++;
    }

    @Override
    public V get(byte[] key) {
        CacheEntry<V> curVal = cache.get(key);
        if (curVal == null) {
            V val = src.get(key);
            curVal = new CacheEntry<>(val);
            cache.put(key, curVal);
        }
        return curVal.value;
    }

    @Override
    public void delete(byte[] key) {
        CacheEntry<V> curVal = cache.get(key);
        if (curVal == null) {
            curVal = new CacheEntry<>(src.get(key));
            cache.put(key, curVal);
        }
        curVal.counter--;
    }

    @Override
    public boolean flush() {
        boolean ret = false;
        for (Map.Entry<byte[], CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().counter > 0) {
                for (int i = 0; i < entry.getValue().counter; i++) {
                    src.put(entry.getKey(), entry.getValue().value);
                }
                ret = true;
            } else if (entry.getValue().counter < 0) {
                for (int i = 0; i > entry.getValue().counter; i--) {
                    src.delete(entry.getKey());
                }
                ret = true;
            }
        }
        return ret;
    }
}
