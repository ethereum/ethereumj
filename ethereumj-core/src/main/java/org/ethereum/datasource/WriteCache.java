package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 11.11.2016.
 */
public class WriteCache<Key, Value> extends AbstractCachedSource<Key, Value> {

    public enum CacheType {
        SIMPLE,
        COUNTING
    }

    protected static abstract class CacheEntry<V> {
        V value;
        int counter = 0;

        public CacheEntry(V value) {
            this.value = value;
        }

        public abstract void deleted();

        public abstract void added();

        public abstract V getValue();
    }

    private static final class SimpleCacheEntry<V> extends CacheEntry<V> {
        public SimpleCacheEntry(V value) {
            super(value);
        }

        public void deleted() {
            counter = -1;
        }

        public void added() {
            counter = 1;
        }

        @Override
        public V getValue() {
            return counter < 0 ? null : value;
        }
    }

    private static final class CountCacheEntry<V> extends CacheEntry<V> {
        public CountCacheEntry(V value) {
            super(value);
        }

        public void deleted() {
            counter--;
        }

        public void added() {
            counter++;
        }

        @Override
        public V getValue() {
            // for counting cache we return the cached value even if
            // it was deleted (once or several times) as we don't know
            // how many 'instances' are left behind
            return value;
        }
    }

    protected final Source<Key, Value> src;
    private final boolean isCounting;

    protected Map<Key, CacheEntry<Value>> cache = new HashMap<>();

    private boolean checked = false;

    public WriteCache(Source<Key, Value> src, CacheType cacheType) {
        this.src = src;
        this.isCounting = cacheType == CacheType.COUNTING;
    }

    public WriteCache<Key, Value> withCache(Map<Key, CacheEntry<Value>> cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public synchronized Source<Key, Value> getSrc() {
        return src;
    }

    @Override
    public synchronized Collection<Key> getModified() {
        return cache.keySet();
    }

    private CacheEntry<Value> createCacheEntry(Value val) {
        if (isCounting) {
            return new CountCacheEntry<>(val);
        } else {
            return new SimpleCacheEntry<>(val);
        }
    }

    @Override
    public synchronized void put(Key key, Value val) {
        checkByteArrKey(key);
        if (val == null)  {
            delete(key);
            return;
        }
        CacheEntry<Value> curVal = cache.get(key);
        if (curVal == null) {
            curVal = createCacheEntry(val);
            CacheEntry<Value> oldVal = cache.put(key, curVal);
            if (oldVal != null) {
                cacheRemoved(key, oldVal.value);
            }
            cacheAdded(key, curVal.value);
        }
        // assigning for non-counting cache only
        // for counting cache the value should be immutable (see HashedKeySource)
        curVal.value = val;
        curVal.added();
    }

    @Override
    public synchronized Value get(Key key) {
        checkByteArrKey(key);
        CacheEntry<Value> curVal = cache.get(key);
        if (curVal == null) {
            return src == null ? null : src.get(key);
        } else {
            return curVal.getValue();
        }
    }

    @Override
    public synchronized void delete(Key key) {
        checkByteArrKey(key);
        CacheEntry<Value> curVal = cache.get(key);
        if (curVal == null) {
            curVal = createCacheEntry(src == null ? null : src.get(key));
            CacheEntry<Value> oldVal = cache.put(key, curVal);
            if (oldVal != null) {
                cacheRemoved(key, oldVal.value);
            }
            cacheAdded(key, curVal.value);
        }
        curVal.deleted();
    }

    @Override
    public synchronized boolean flush() {
        boolean ret = false;
        for (Map.Entry<Key, CacheEntry<Value>> entry : cache.entrySet()) {
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
        cache.clear();
        cacheCleared();
        return ret;
    }

    public synchronized Value getCached(Key key) {
        CacheEntry<Value> entry = cache.get(key);
        return entry == null ? null : entry.getValue();
    }

    // Guard against wrong cache Map
    // if a regular Map is accidentally used for byte[] type keys
    // the situation might be tricky to debug
    private void checkByteArrKey(Key key) {
        if (checked) return;

        if (key instanceof byte[]) {
            if (!(cache instanceof ByteArrayMap)) {
                throw new RuntimeException("Wrong map/set for byte[] key");
            }
        }
        checked = true;
    }

    public static class BytesKey<V> extends WriteCache<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src, CacheType cacheType) {
            super(src, cacheType);
            withCache(new ByteArrayMap<CacheEntry<V>>());
        }
    }
}
