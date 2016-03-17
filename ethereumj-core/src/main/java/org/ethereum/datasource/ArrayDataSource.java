package org.ethereum.datasource;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.util.ByteUtil;
import org.mapdb.DataIO;

import java.io.IOException;
import java.util.AbstractList;

/**
 * Created by Anton Nashatyrev on 04.03.2016.
 */
public class ArrayDataSource<V> extends AbstractList<V> implements Flushable {
    private DatasourceArray src;
    private LRUMap<Integer, V> cache = new LRUMap<>(256);

    Serializer<V, byte[]> serializer;
    boolean cacheOnWrite = true;

    public ArrayDataSource(KeyValueDataSource src, Serializer<V, byte[]> serializer) {
        this.src = new DatasourceArray(src);
        this.serializer = serializer;
    }

    public ArrayDataSource withCacheSize(int cacheSize) {
        cache = new LRUMap<>(cacheSize);
        return this;
    }

    public ArrayDataSource withWriteThrough(boolean writeThrough) {
        if (!writeThrough) {
            throw new RuntimeException("Not implemented yet");
        }
        return this;
    }

    public ArrayDataSource withCacheOnWrite(boolean cacheOnWrite) {
        this.cacheOnWrite = cacheOnWrite;
        return this;
    }

    public void flush() {
        // for write-back type cache only
    }

    @Override
    public synchronized int size() {
        return src.size();
    }

    @Override
    public synchronized V get(int idx) {
        if (idx >= size()) return null;

        V v = cache.get(idx);
        if (v == null) {
            byte[] bytes = src.get(idx);
            if (bytes == null) return null;
            v = serializer.deserialize(bytes);
            cache.put(idx, v);
        }
        return v;
    }

    @Override
    public synchronized V set(int idx, V value) {
        byte[] bytes = serializer.serialize(value);
        src.put(idx, bytes);
        if (cacheOnWrite) {
            cache.put(idx, value);
        }
        return value;
    }

    @Override
    public void add(int index, V element) {
        set(index, element);
    }


    private static class DatasourceArray  {
        KeyValueDataSource db;
        static final byte[] sizeKey = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        int size = -1;

        public DatasourceArray(KeyValueDataSource db) {
            this.db = db;
        }

        public synchronized int size() {
            if (size < 0) {
                byte[] sizeBB = db.get(sizeKey);
                size = sizeBB == null ? 0 : ByteUtil.byteArrayToInt(sizeBB);
            }
            return size;
        }

        public synchronized byte[] get(int idx) {
            if (idx < 0 || idx >= size()) throw new IndexOutOfBoundsException(idx + " > " + size);
            return db.get(ByteUtil.intToBytes(idx));
        }

        public synchronized byte[] put(int idx, byte[] value) {
            if (idx >= size()) {
                setSize(idx + 1);
            }
            db.put(ByteUtil.intToBytes(idx), value);
            return value;
        }

        private synchronized void setSize(int newSize) {
            size = newSize;
            db.put(sizeKey, ByteUtil.intToBytes(newSize));
        }

//        public void flush() {
//            if (db instanceof CachingDataSource) {
//                ((CachingDataSource) db).flush();
//            }
//        }
    }
}
