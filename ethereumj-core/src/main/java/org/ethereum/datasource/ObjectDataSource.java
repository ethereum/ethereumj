package org.ethereum.datasource;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.db.ByteArrayWrapper;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public class ObjectDataSource<V> implements Flushable{
    private KeyValueDataSource src;
    private Map<ByteArrayWrapper, V> cache = Collections.synchronizedMap(new LRUMap<ByteArrayWrapper, V>(256));
    Serializer<V, byte[]> serializer;
    boolean cacheOnWrite = true;

    public ObjectDataSource(KeyValueDataSource src, Serializer<V, byte[]> serializer) {
        this.src = src;
        this.serializer = serializer;
    }

    public ObjectDataSource<V> withCacheSize(int cacheSize) {
        cache = Collections.synchronizedMap(new LRUMap<ByteArrayWrapper, V>(cacheSize));
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

    public synchronized void put(byte[] key, V value) {
        byte[] bytes = serializer.serialize(value);
        src.put(key, bytes);
        if (cacheOnWrite) {
            cache.put(new ByteArrayWrapper(key), value);
        }
    }

    public synchronized V get(byte[] key) {
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

    protected KeyValueDataSource getSrc() {
        return src;
    }

    public void close() {
        src.close();
    }
}
