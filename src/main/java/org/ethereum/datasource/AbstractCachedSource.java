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

/**
 * Abstract cache implementation which tracks the cache size with
 * supplied key and value MemSizeEstimator's
 *
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public abstract class AbstractCachedSource <Key, Value>
        extends AbstractChainedSource<Key, Value, Key, Value>
        implements CachedSource<Key, Value> {

    private final Object lock = new Object();

    /**
     * Like the Optional interface represents either the value cached
     * or null cached (i.e. cache knows that underlying storage contain null)
     */
    public interface Entry<V> {
        V value();
    }

    static final class SimpleEntry<V> implements Entry<V> {
        private V val;
        public SimpleEntry(V val) {
            this.val = val;
        }
        public V value() {
            return val;
        }
    }

    protected MemSizeEstimator<Key> keySizeEstimator;
    protected MemSizeEstimator<Value> valueSizeEstimator;
    private int size = 0;

    public AbstractCachedSource(Source<Key, Value> source) {
        super(source);
    }

    /**
     * Returns the cached value if exist.
     * Method doesn't look into the underlying storage
     * @return The value Entry if it cached (Entry may has null value if null value is cached),
     *        or null if no information in the cache for this key
     */
    abstract Entry<Value> getCached(Key key);

    /**
     * Needs to be called by the implementation when cache entry is added
     * Only new entries should be accounted for accurate size tracking
     * If the value for the key is changed the {@link #cacheRemoved}
     * needs to be called first
     */
    protected void cacheAdded(Key key, Value value) {
        synchronized (lock) {
            if (keySizeEstimator != null) {
                size += keySizeEstimator.estimateSize(key);
            }
            if (valueSizeEstimator != null) {
                size += valueSizeEstimator.estimateSize(value);
            }
        }
    }

    /**
     * Needs to be called by the implementation when cache entry is removed
     */
    protected void cacheRemoved(Key key, Value value) {
        synchronized (lock) {
            if (keySizeEstimator != null) {
                size -= keySizeEstimator.estimateSize(key);
            }
            if (valueSizeEstimator != null) {
                size -= valueSizeEstimator.estimateSize(value);
            }
        }
    }

    /**
     * Needs to be called by the implementation when cache is cleared
     */
    protected void cacheCleared() {
        synchronized (lock) {
            size = 0;
        }
    }

    /**
     * Sets the key/value size estimators
     */
    public AbstractCachedSource <Key, Value> withSizeEstimators(MemSizeEstimator<Key> keySizeEstimator, MemSizeEstimator<Value> valueSizeEstimator) {
        this.keySizeEstimator = keySizeEstimator;
        this.valueSizeEstimator = valueSizeEstimator;
        return this;
    }

    @Override
    public long estimateCacheSize() {
        return size;
    }
}
