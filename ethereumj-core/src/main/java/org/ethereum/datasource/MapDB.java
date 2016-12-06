package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;

import java.util.Map;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class MapDB<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    public MapDB() {
        this(new ByteArrayMap<V>());
    }

    public MapDB(ByteArrayMap<V> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            storage.put(key, val);
        }
    }

    @Override
    public synchronized V get(byte[] key) {
        return storage.get(key);
    }

    @Override
    public synchronized void delete(byte[] key) {
        storage.remove(key);
    }

    @Override
    public synchronized boolean flush() {
        return true;
    }

    @Override
    public void setName(String name) {}

    @Override
    public String getName() {
        return "in-memory";
    }

    @Override
    public void init() {}

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void close() {}

    @Override
    public synchronized Set<byte[]> keys() {
        return getStorage().keySet();
    }

    @Override
    public synchronized void updateBatch(Map<byte[], V> rows) {
        for (Map.Entry<byte[], V> entry : rows.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public synchronized Map<byte[], V> getStorage() {
        return storage;
    }
}
