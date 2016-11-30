package org.ethereum.datasource;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class ReadCache<Key, Value> implements CachedSource<Key, Value> {

    private Source<Key, Value> src;

    private Map<Key, Value> cache = new HashMap<>();

    public ReadCache(Source<Key, Value> src) {
        this.src = src;
    }

    public ReadCache<Key, Value> withCache(Map<Key, Value> cache) {
        this.cache = cache;
        return this;
    }

    public ReadCache<Key, Value> withMaxCapacity(int maxCapacity) {
        return withCache(new LRUMap<Key, Value>(maxCapacity));
    }

    private boolean checked = false;
    private void checkByteArrKey(Key key) {
        if (checked) return;

        if (key instanceof byte[]) {
            if (!(cache instanceof ByteArrayMap)) {
                throw new RuntimeException("Wrong map/set for byte[] key");
            }
        }
        checked = true;
    }

    @Override
    public synchronized void put(Key key, Value val) {
        checkByteArrKey(key);
        if (val == null) {
            delete(key);
        } else {
            cache.put(key, val);
            src.put(key, val);
        }
    }

    @Override
    public synchronized Value get(Key key) {
        checkByteArrKey(key);
        Value ret = cache.get(key);
        if (ret == null) {
            if (cache.containsKey(key)) {
                ret = null;
            } else {
                ret = src.get(key);
                cache.put(key, ret);
            }
        }
        return ret;
    }

    @Override
    public synchronized void delete(Key key) {
        checkByteArrKey(key);
        cache.remove(key);
        src.delete(key);
    }

    @Override
    public synchronized boolean flush() {
        return false;
    }

    public synchronized Source<Key, Value> getSrc() {
        return src;
    }

    public synchronized Collection<Key> getModified() {
        return Collections.emptyList();
    }

    public synchronized Map<Key, Value> getCache() {
        return cache;
    }

    public synchronized Value getCached(Key key) {
        return cache.get(key);
    }

    public static class BytesKey<V> extends ReadCache<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src) {
            super(src);
            withCache(new ByteArrayMap<V>());
        }

        public ReadCache.BytesKey<V> withMaxCapacity(int maxCapacity) {
            withCache(new ByteArrayMap<V>(new LRUMap<ByteArrayWrapper, V>(maxCapacity)));
            return this;
        }
    }
}
