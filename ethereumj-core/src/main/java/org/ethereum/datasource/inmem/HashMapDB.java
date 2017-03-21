/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.datasource.inmem;

import org.ethereum.datasource.DbSource;
import org.ethereum.util.ByteArrayMap;

import java.util.Map;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 12.10.2016.
 */
public class HashMapDB<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    public HashMapDB() {
        this(new ByteArrayMap<V>());
    }

    public HashMapDB(ByteArrayMap<V> storage) {
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
