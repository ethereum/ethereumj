package org.ethereum.db;

import org.ethereum.datasource.inmem.HashMapDB;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 29.12.2016.
 */
public class SlowHashMapDb<V> extends HashMapDB<V> {

    long delay = 1;

    public SlowHashMapDb<V> withDelay(long delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public void put(byte[] key, V val) {
        try {Thread.sleep(delay);} catch (InterruptedException e) {}
        super.put(key, val);
    }

    @Override
    public V get(byte[] key) {
        try {Thread.sleep(delay);} catch (InterruptedException e) {}
        return super.get(key);
    }

    @Override
    public void delete(byte[] key) {
        try {Thread.sleep(delay);} catch (InterruptedException e) {}
        super.delete(key);
    }

    @Override
    public boolean flush() {
        try {Thread.sleep(delay);} catch (InterruptedException e) {}
        return super.flush();
    }

    @Override
    public void updateBatch(Map<byte[], V> rows) {
        try {Thread.sleep(delay);} catch (InterruptedException e) {}
        super.updateBatch(rows);
    }
}
