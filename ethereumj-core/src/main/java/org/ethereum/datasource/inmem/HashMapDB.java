package org.ethereum.datasource.inmem;

import org.ethereum.datasource.DbSource;
import org.ethereum.util.ByteArrayMap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class HashMapDB<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected Lock readLock = rwLock.readLock();
    protected Lock writeLock = rwLock.writeLock();

    public HashMapDB() {
        this(new ByteArrayMap<V>());
    }

    public HashMapDB(ByteArrayMap<V> storage) {
        this.storage = storage;
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            try {
                writeLock.lock();
                storage.put(key, val);
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public V get(byte[] key) {
        try {
            readLock.lock();
            return storage.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        try {
            writeLock.lock();
            storage.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean flush() {
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
    public Set<byte[]> keys() {
        try {
            readLock.lock();
            return getStorage().keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void updateBatch(Map<byte[], V> rows) {
        try {
            writeLock.lock();
            for (Map.Entry<byte[], V> entry : rows.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public Map<byte[], V> getStorage() {
        return storage;
    }
}
