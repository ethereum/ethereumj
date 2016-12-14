package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects changes and propagate them to the backing Source when flush() is called
 *
 * The WriteCache can be of two types: Simple and Counting
 *
 * Simple acts as regular Map: single and double adding of the same entry has the same effect
 * Source entries (key/value pairs) may have arbitrary nature
 *
 * Counting counts the resulting number of inserts (+1) and deletes (-1) and when flushed
 * does the resulting number of inserts (if sum > 0) or deletes (if sum < 0)
 * Counting Source acts like {@link HashedKeySource} and makes sense only for data
 * where a single key always corresponds to a single value
 * Counting cache normally used as backing store for Trie data structure
 *
 * Created by Anton Nashatyrev on 11.11.2016.
 */
public class WriteCache<Key, Value> extends AbstractCachedSource<Key, Value> {

    /**
     * Type of the write cache
     */
    public enum CacheType {
        /**
         * Simple acts as regular Map: single and double adding of the same entry has the same effect
         * Source entries (key/value pairs) may have arbitrary nature
         */
        SIMPLE,
        /**
         * Counting counts the resulting number of inserts (+1) and deletes (-1) and when flushed
         * does the resulting number of inserts (if sum > 0) or deletes (if sum < 0)
         * Counting Source acts like {@link HashedKeySource} and makes sense only for data
         * where a single key always corresponds to a single value
         * Counting cache normally used as backing store for Trie data structure
         */
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

    private final boolean isCounting;

    protected Map<Key, CacheEntry<Value>> cache = new HashMap<>();

    private boolean checked = false;

    public WriteCache(Source<Key, Value> src, CacheType cacheType) {
        super(src);
        this.isCounting = cacheType == CacheType.COUNTING;
    }

    public WriteCache<Key, Value> withCache(Map<Key, CacheEntry<Value>> cache) {
        this.cache = cache;
        return this;
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
            return getSource() == null ? null : getSource().get(key);
        } else {
            return curVal.getValue();
        }
    }

    @Override
    public synchronized void delete(Key key) {
        checkByteArrKey(key);
        CacheEntry<Value> curVal = cache.get(key);
        if (curVal == null) {
            curVal = createCacheEntry(getSource() == null ? null : getSource().get(key));
            CacheEntry<Value> oldVal = cache.put(key, curVal);
            if (oldVal != null) {
                cacheRemoved(key, oldVal.value);
            }
            cacheAdded(key, curVal.value);
        }
        curVal.deleted();
    }

    @Override
    public synchronized boolean flushImpl() {
        System.out.printf("Flush start: %s%n", this);
        boolean ret = false;
        for (Map.Entry<Key, CacheEntry<Value>> entry : cache.entrySet()) {
            if (entry.getValue().counter > 0) {
                for (int i = 0; i < entry.getValue().counter; i++) {
                    getSource().put(entry.getKey(), entry.getValue().value);
                }
                ret = true;
            } else if (entry.getValue().counter < 0) {
                for (int i = 0; i > entry.getValue().counter; i--) {
                    getSource().delete(entry.getKey());
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

    /**
     * Shortcut for WriteCache with byte[] keys. Also prevents accidental
     * usage of regular Map implementation (non byte[])
     */
    public static class BytesKey<V> extends WriteCache<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src, CacheType cacheType) {
            super(src, cacheType);
            withCache(new ByteArrayMap<CacheEntry<V>>());
        }
    }
}
