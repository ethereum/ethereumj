package org.ethereum.datasource.test;

import org.ethereum.datasource.Flushable;
import org.ethereum.datasource.Serializer;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class CachedSource<Key, Value, SourceKey, SourceValue> implements Source<Key, Value>, Flushable {
//    static Object NULL = new Object();

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

    public CachedSource(Source<SourceKey, SourceValue> src, Serializer<Key, SourceKey> keySerializer, Serializer<Value, SourceValue> valSerializer) {
        this.src = src;
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
    }

    public CachedSource<Key, Value, SourceKey, SourceValue>  withCache(Map<Key, Value> cache, Set<Key> writes) {
        this.cache = cache;
        this.writes = writes;
        return this;
    }

    @Override
    public void put(Key key, Value val) {
        if (val == null) {
            delete(key);
        } else {
            cache.put(key, val);
            writes.add(key);
        }
    }

    @Override
    public Value get(Key key) {
        Value ret = cache.get(key);
        if (ret == null) {
            SourceValue sourceValue = src.get(keySerializer.serialize(key));
            if (sourceValue != null) {
                ret = valSerializer.deserialize(sourceValue);
                cache.put(key, ret);
            }
        }
        return ret;
    }

    @Override
    public void delete(Key key) {
        if (noDelete) return;
        cache.put(key, null);
        writes.add(key);
    }

    @Override
    public void flush() {
        flushTo(src);
        writes.clear();
    }

    public void flushTo(Source<SourceKey, SourceValue> src) {
        for (Key key : writes) {
            Value value = cache.get(key);
            if (value == null) {
                src.delete(keySerializer.serialize(key));
            } else {
                src.put(keySerializer.serialize(key), valSerializer.serialize(value));
            }
        }
    }

    public Collection<Key> getModified() {
        throw new RuntimeException("TODO");
    }

    public static class BytesKey<V, VS> extends CachedSource<byte[], V, byte[], VS> {

        public BytesKey(Source<byte[], VS> src, Serializer<V, VS> valSerializer) {
            super(src, new Serializer.IdentitySerializer<byte[]>(), valSerializer);
            withCache(new ByteArrayMap<V>(), new ByteArraySet());
        }
    }

    public static class Simple<K, V> extends CachedSource<K, V, K, V> {

        public Simple(Source<K, V> src) {
            super(src, new Serializer.IdentitySerializer<K>(), new Serializer.IdentitySerializer<V>());
        }
    }
}
