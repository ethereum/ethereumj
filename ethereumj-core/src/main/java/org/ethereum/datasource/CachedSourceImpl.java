package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class CachedSourceImpl<Key, Value> implements
        CachedSource<Key, Value> {

    Source<Key, Value> src;

    Map<Key, Value> cache = new HashMap<>();
    Set<Key> writes = new HashSet<>();

    boolean cacheReads;
    boolean cacheWrites;
    boolean writeThrough;
    boolean noDelete;       // disregard deletes
    boolean delayedDelete;  // only mark cache entry deleted and actually delete on flush
    boolean flushSource;    // on flush() flush source DS as well
    boolean countWrites;    // if e.g. put(k, v) was called twice then src.put(k, v) also called twice on flush

    public CachedSourceImpl(Source<Key, Value> src) {
        this.src = src;
    }

    public CachedSourceImpl<Key, Value> withCache(Map<Key, Value> cache, Set<Key> writes) {
        this.cache = cache;
        this.writes = writes;
        return this;
    }

    private boolean checked = false;
    private void checkByteArrKey(Key key) {
        if (checked) return;

        if (key instanceof byte[]) {
            if (!(cache instanceof ByteArrayMap && writes instanceof ByteArraySet)) {
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
            writes.add(key);
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
        if (noDelete) return;
        cache.put(key, null);
        writes.add(key);
    }

    @Override
    public synchronized boolean flush() {
        boolean ret = flushTo(src);
        writes.clear();
        return ret;
    }

    public synchronized boolean flushTo(Source<Key, Value> src) {
        for (Key key : writes) {
            Value value = cache.get(key);
            if (value == null) {
                src.delete(key);
            } else {
                src.put(key, value);
            }
        }
        return !writes.isEmpty();
    }

    public Source<Key, Value> getSrc() {
        return src;
    }

    public Collection<Key> getModified() {
        throw new RuntimeException("TODO");
    }

    public Map<Key, Value> getCache() {
        return cache;
    }

    public CachedSourceImpl<Key, Value>  withCacheReads(boolean cacheReads) {
        this.cacheReads = cacheReads;
        return this;
    }

    public CachedSourceImpl<Key, Value>  withNoDelete(boolean noDelete) {
        this.noDelete = noDelete;
        return this;
    }

    public static class BytesKey<V> extends CachedSourceImpl<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src) {
            super(src);
            withCache(new ByteArrayMap<V>(), new ByteArraySet());
        }
    }
}
