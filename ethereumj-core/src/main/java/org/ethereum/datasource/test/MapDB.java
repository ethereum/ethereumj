package org.ethereum.datasource.test;

import org.ethereum.util.ByteArrayMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class MapDB<V> implements Source<byte[], V> {

    Map<byte[], V> storage = new ByteArrayMap<>();

    @Override
    public void put(byte[] key, V val) {
        storage.put(key, val);
    }

    @Override
    public V get(byte[] key) {
        return storage.get(key);
    }

    @Override
    public void delete(byte[] key) {
        storage.remove(key);
    }

    @Override
    public boolean flush() {
        return true;
    }

    public Map<byte[], V> getStorage() {
        return storage;
    }
}
