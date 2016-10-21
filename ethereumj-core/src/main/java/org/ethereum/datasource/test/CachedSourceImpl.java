package org.ethereum.datasource.test;

import org.ethereum.datasource.Serializer;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class CachedSourceImpl<Key, Value, SourceKey, SourceValue> implements
        CachedSource<Key, Value, SourceKey, SourceValue> {

    Source<SourceKey, SourceValue> src;

    Serializer<Key, SourceKey> keySerializer;
    Serializer<Value, SourceValue> valSerializer;

    Map<Key, Value> cache = new HashMap<>();
    Set<Key> writes = new HashSet<>();

    boolean cacheReads;
    boolean cacheWrites;
    boolean writeThrough;
    boolean noDelete;       // disregard deletes
    boolean delayedDelete;  // only mark cache entry deleted and actually delete on flush
    boolean flushSource;    // on flush() flush source DS as well
    boolean countWrites;    // if e.g. put(k, v) was called twice then src.put(k, v) also called twice on flush

    public CachedSourceImpl(Source<SourceKey, SourceValue> src, Serializer<Key, SourceKey> keySerializer, Serializer<Value, SourceValue> valSerializer) {
        this.src = src;
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
    }

    public CachedSourceImpl<Key, Value, SourceKey, SourceValue> withCache(Map<Key, Value> cache, Set<Key> writes) {
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
    public void put(Key key, Value val) {
        checkByteArrKey(key);
        if (val == null) {
            delete(key);
        } else {
            cache.put(key, val);
            writes.add(key);
        }
    }

    @Override
    public Value get(Key key) {
        checkByteArrKey(key);
        Value ret = cache.get(key);
        if (ret == null) {
            if (cache.containsKey(key)) {
                ret = null;
            } else {
                SourceValue sourceValue = src.get(keySerializer.serialize(key));
                ret = sourceValue != null ? valSerializer.deserialize(sourceValue) : null;
                cache.put(key, ret);
            }
        }
        return ret;
    }

    @Override
    public void delete(Key key) {
        checkByteArrKey(key);
        if (noDelete) return;
        cache.put(key, null);
        writes.add(key);
    }

    @Override
    public boolean flush() {
        boolean ret = flushTo(src);
        writes.clear();
        return ret;
    }

    public boolean flushTo(Source<SourceKey, SourceValue> src) {
        for (Key key : writes) {
            Value value = cache.get(key);
            if (value == null) {
                src.delete(keySerializer.serialize(key));
            } else {
                src.put(keySerializer.serialize(key), valSerializer.serialize(value));
            }
        }
        return !writes.isEmpty();
    }

    public Source<SourceKey, SourceValue> getSrc() {
        return src;
    }

    public Collection<Key> getModified() {
        throw new RuntimeException("TODO");
    }

    public Map<Key, Value> getCache() {
        return cache;
    }

    public CachedSourceImpl<Key, Value, SourceKey, SourceValue>  withCacheReads(boolean cacheReads) {
        this.cacheReads = cacheReads;
        return this;
    }

    public CachedSourceImpl<Key, Value, SourceKey, SourceValue>  withNoDelete(boolean noDelete) {
        this.noDelete = noDelete;
        return this;
    }

    public static class BytesKey<V, VS> extends CachedSourceImpl<byte[], V, byte[], VS> implements CachedSource.BytesKey<V, VS> {

        public BytesKey(Source<byte[], VS> src, Serializer<V, VS> valSerializer) {
            super(src, new Serializer.IdentitySerializer<byte[]>(), valSerializer);
            withCache(new ByteArrayMap<V>(), new ByteArraySet());
        }
    }

    public static class Simple<K, V> extends CachedSourceImpl<K, V, K, V> implements CachedSource.Identity<K, V> {

        public Simple(Source<K, V> src) {
            super(src, new Serializer.IdentitySerializer<K>(), new Serializer.IdentitySerializer<V>());
        }
    }

    public static class SimpleBytesKey<V> extends BytesKey<V, V> implements CachedSource.SimpleBytesKey<V>  {
        public SimpleBytesKey(Source<byte[], V> src) {
            super(src, new Serializer.IdentitySerializer<V>());
        }
    }
}
