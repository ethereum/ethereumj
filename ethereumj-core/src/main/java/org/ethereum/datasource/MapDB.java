package org.ethereum.datasource;

import org.ethereum.util.ByteArrayMap;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class MapDB<V> implements Source<byte[], V> {

    Map<byte[], V> storage = new ByteArrayMap<>();

    @Override
    public synchronized void put(byte[] key, V val) {
        storage.put(key, val);
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

    public synchronized Map<byte[], V> getStorage() {
        return storage;
    }
}
