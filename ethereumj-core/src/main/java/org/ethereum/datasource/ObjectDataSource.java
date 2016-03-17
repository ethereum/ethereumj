package org.ethereum.datasource;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.db.ByteArrayWrapper;

/**
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public class ObjectDataSource<V> implements Flushable{
    private KeyValueDataSource src;
    private LRUMap<ByteArrayWrapper, V> cache = new LRUMap<>(256);
    Serializer<V, byte[]> serializer;
    boolean cacheOnWrite = true;

    public ObjectDataSource(KeyValueDataSource src, Serializer<V, byte[]> serializer) {
        this.src = src;
        this.serializer = serializer;
    }

    public ObjectDataSource<V> withCacheSize(int cacheSize) {
        cache = new LRUMap<>(cacheSize);
        return this;
    }

    public ObjectDataSource<V> withWriteThrough(boolean writeThrough) {
        if (!writeThrough) {
            throw new RuntimeException("Not implemented yet");
        }
        return this;
    }

    public ObjectDataSource<V> withCacheOnWrite(boolean cacheOnWrite) {
        this.cacheOnWrite = cacheOnWrite;
        return this;
    }

    public void flush() {
        // for write-back type cache only
    }

    public void put(byte[] key, V value) {
        byte[] bytes = serializer.serialize(value);
        src.put(key, bytes);
        if (cacheOnWrite) {
            cache.put(new ByteArrayWrapper(key), value);
        }
    }

    public V get(byte[] key) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        V ret = cache.get(keyW);
        if (ret == null) {
            byte[] bytes = src.get(key);
            if (bytes == null) return null;
            ret = serializer.deserialize(bytes);
            cache.put(keyW, ret);
        }
        return ret;
    }
}
