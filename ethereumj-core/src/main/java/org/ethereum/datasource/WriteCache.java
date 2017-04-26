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
package org.ethereum.datasource;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;
import org.ethereum.util.ALock;
import org.ethereum.util.ByteArrayMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects changes and propagate them to the backing Source when flush() is called
 *
 * The WriteCache can be of two types: Simple and Counting
 *
 * Simple acts as regular Map: single and double adding of the same entry has the same effect
 * Source entries (key/value pairs) may have arbitrary nature
 *
 * Counting counts the resulting number of inserts (+1) and deletes (-1) and when flushed
 * does the resulting number of inserts (if sum > 0) or deletes (if sum < 0)
 * Counting Source acts like {@link HashedKeySource} and makes sense only for data
 * where a single key always corresponds to a single value
 * Counting cache normally used as backing store for Trie data structure
 *
 * Created by Anton Nashatyrev on 11.11.2016.
 */
public class WriteCache<Key, Value> extends AbstractCachedSource<Key, Value> {

    /**
     * Type of the write cache
     */
    public enum CacheType {
        /**
         * Simple acts as regular Map: single and double adding of the same entry has the same effect
         * Source entries (key/value pairs) may have arbitrary nature
         */
        SIMPLE,
        /**
         * Counting counts the resulting number of inserts (+1) and deletes (-1) and when flushed
         * does the resulting number of inserts (if sum > 0) or deletes (if sum < 0)
         * Counting Source acts like {@link HashedKeySource} and makes sense only for data
         * where a single key always corresponds to a single value
         * Counting cache normally used as backing store for Trie data structure
         */
        COUNTING
    }

    private static abstract class CacheEntry<V> implements Entry<V>{
        // dedicated value instance which indicates that the entry was deleted
        // (ref counter decremented) but we don't know actual value behind it
        static final Object UNKNOWN_VALUE = new Object();

        V value;
        int counter = 0;

        protected CacheEntry(V value) {
            this.value = value;
        }

        protected abstract void deleted();

        protected abstract void added();

        protected abstract V getValue();

        @Override
        public V value() {
            V v = getValue();
            return v == UNKNOWN_VALUE ? null : v;
        }
    }

    private static final class SimpleCacheEntry<V> extends CacheEntry<V> {
        public SimpleCacheEntry(V value) {
            super(value);
        }

        public void deleted() {
            counter = -1;
        }

        public void added() {
            counter = 1;
        }

        @Override
        public V getValue() {
            return counter < 0 ? null : value;
        }
    }

    private static final class CountCacheEntry<V> extends CacheEntry<V> {
        public CountCacheEntry(V value) {
            super(value);
        }

        public void deleted() {
            counter--;
        }

        public void added() {
            counter++;
        }

        @Override
        public V getValue() {
            // for counting cache we return the cached value even if
            // it was deleted (once or several times) as we don't know
            // how many 'instances' are left behind
            return value;
        }
    }

    private final boolean isCounting;

    protected volatile Map<Key, CacheEntry<Value>> cache = new HashMap<>();

    protected ReadWriteUpdateLock rwuLock = new ReentrantReadWriteUpdateLock();
    protected ALock readLock = new ALock(rwuLock.readLock());
    protected ALock writeLock = new ALock(rwuLock.writeLock());
    protected ALock updateLock = new ALock(rwuLock.updateLock());

    private boolean checked = false;

    public WriteCache(Source<Key, Value> src, CacheType cacheType) {
        super(src);
        this.isCounting = cacheType == CacheType.COUNTING;
    }

    public WriteCache<Key, Value> withCache(Map<Key, CacheEntry<Value>> cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public Collection<Key> getModified() {
        try (ALock l = readLock.lock()){
            return cache.keySet();
        }
    }

    @Override
    public boolean hasModified() {
        return !cache.isEmpty();
    }

    private CacheEntry<Value> createCacheEntry(Value val) {
        if (isCounting) {
            return new CountCacheEntry<>(val);
        } else {
            return new SimpleCacheEntry<>(val);
        }
    }

    @Override
    public void put(Key key, Value val) {
        checkByteArrKey(key);
        if (val == null)  {
            delete(key);
            return;
        }


        try (ALock l = writeLock.lock()){
            CacheEntry<Value> curVal = cache.get(key);
            if (curVal == null) {
                curVal = createCacheEntry(val);
                CacheEntry<Value> oldVal = cache.put(key, curVal);
                if (oldVal != null) {
                    cacheRemoved(key, oldVal.value == unknownValue() ? null : oldVal.value);
                }
                cacheAdded(key, curVal.value);
            }
            // assigning for non-counting cache only
            // for counting cache the value should be immutable (see HashedKeySource)
            curVal.value = val;
            curVal.added();
        }
    }

    @Override
    public Value get(Key key) {
        checkByteArrKey(key);
        try (ALock l = readLock.lock()){
            CacheEntry<Value> curVal = cache.get(key);
            if (curVal == null) {
                return getSource() == null ? null : getSource().get(key);
            } else {
                Value value = curVal.getValue();
                if (value == unknownValue()) {
                    return getSource() == null ? null : getSource().get(key);
                } else {
                    return value;
                }
            }
        }
    }

    @Override
    public void delete(Key key) {
        checkByteArrKey(key);
        try (ALock l = writeLock.lock()){
            CacheEntry<Value> curVal = cache.get(key);
            if (curVal == null) {
                curVal = createCacheEntry(getSource() == null ? null : unknownValue());
                CacheEntry<Value> oldVal = cache.put(key, curVal);
                if (oldVal != null) {
                    cacheRemoved(key, oldVal.value);
                }
                cacheAdded(key, curVal.value == unknownValue() ? null : curVal.value);
            }
            curVal.deleted();
        }
    }

    @Override
    public boolean flush() {
        boolean ret = false;
        try (ALock l = updateLock.lock()){
            for (Map.Entry<Key, CacheEntry<Value>> entry : cache.entrySet()) {
                if (entry.getValue().counter > 0) {
                    for (int i = 0; i < entry.getValue().counter; i++) {
                        getSource().put(entry.getKey(), entry.getValue().value);
                    }
                    ret = true;
                } else if (entry.getValue().counter < 0) {
                    for (int i = 0; i > entry.getValue().counter; i--) {
                        getSource().delete(entry.getKey());
                    }
                    ret = true;
                }
            }
            if (flushSource) {
                getSource().flush();
            }
            try (ALock l1 = writeLock.lock()){
                cache.clear();
                cacheCleared();
            }
            return ret;
        }
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }

    private Value unknownValue() {
        return (Value) CacheEntry.UNKNOWN_VALUE;
    }

    public Entry<Value> getCached(Key key) {
        try (ALock l = readLock.lock()){
            CacheEntry<Value> entry = cache.get(key);
            if (entry == null || entry.value == unknownValue()) {
                return null;
            }else {
                return entry;
            }
        }
    }

    // Guard against wrong cache Map
    // if a regular Map is accidentally used for byte[] type keys
    // the situation might be tricky to debug
    private void checkByteArrKey(Key key) {
        if (checked) return;

        if (key instanceof byte[]) {
            if (!(cache instanceof ByteArrayMap)) {
                throw new RuntimeException("Wrong map/set for byte[] key");
            }
        }
        checked = true;
    }

    public long debugCacheSize() {
        long ret = 0;
        for (Map.Entry<Key, CacheEntry<Value>> entry : cache.entrySet()) {
            ret += keySizeEstimator.estimateSize(entry.getKey());
            ret += valueSizeEstimator.estimateSize(entry.getValue().value());
        }
        return ret;
    }

    /**
     * Shortcut for WriteCache with byte[] keys. Also prevents accidental
     * usage of regular Map implementation (non byte[])
     */
    public static class BytesKey<V> extends WriteCache<byte[], V> implements CachedSource.BytesKey<V> {

        public BytesKey(Source<byte[], V> src, CacheType cacheType) {
            super(src, cacheType);
            withCache(new ByteArrayMap<CacheEntry<V>>());
        }
    }
}
