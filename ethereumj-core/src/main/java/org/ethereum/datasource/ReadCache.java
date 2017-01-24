package org.ethereum.datasource;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteArrayMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches entries get/updated and use LRU algo to purge them if the number
 * of entries exceeds threshold.
 *
 * This implementation could extended to estimate cached data size for
 * more accurate size restriction, but if entries are more or less
 * of the same size the entries count would be good enough
 *
 * Another implementation idea is heap sensitive read cache based on
 * SoftReferences, when the cache occupies all the available heap
 * but get shrink when low heap
 *
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class ReadCache<Key, Value> extends AbstractCachedSource<Key, Value> {

    private Map<Key, Value> cache = new HashMap<>();

    public ReadCache(Source<Key, Value> src) {
        super(src);
    }

    /**
     * Installs the specific cache Map implementation
     */
    public ReadCache<Key, Value> withCache(Map<Key, Value> cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Sets the max number of entries to cache
     */
    public ReadCache<Key, Value> withMaxCapacity(int maxCapacity) {
        return withCache(new LRUMap<Key, Value>(maxCapacity) {
            @Override
            protected boolean removeLRU(LinkEntry<Key, Value> entry) {
                cacheRemoved(entry.getKey(), entry.getValue());
                return super.removeLRU(entry);
            }
        });
    }

    // the guard against incorrect Map implementation for byte[] keys
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
            cacheAdded(key, val);
            getSource().put(key, val);
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
                ret = getSource().get(key);
                cache.put(key, ret);
                cacheAdded(key, ret);
            }
        }
        return ret;
    }

    @Override
    public synchronized void delete(Key key) {
        checkByteArrKey(key);
        Value value = cache.remove(key);
        cacheRemoved(key, value);
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }

    public synchronized Collection<Key> getModified() {
        return Collections.emptyList();
    }

    public synchronized Value getCached(Key key) {
        return cache.get(key);
    }

    /**
     * Shortcut for ReadCache with byte[] keys. Also prevents accidental
     * usage of regular Map implementation (non byte[])
     */
    public static class BytesKey<V> extends ReadCache<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src) {
            super(src);
            withCache(new ByteArrayMap<V>());
        }

        public ReadCache.BytesKey<V> withMaxCapacity(int maxCapacity) {
            withCache(new ByteArrayMap<V>(new LRUMap<ByteArrayWrapper, V>(maxCapacity) {
                @Override
                protected boolean removeLRU(LinkEntry<ByteArrayWrapper, V> entry) {
                    cacheRemoved(entry.getKey().getData(), entry.getValue());
                    return super.removeLRU(entry);
                }
            }));
            return this;
        }
    }
}
