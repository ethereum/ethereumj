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
 * Cache of Caches (child caches)
 * When a child cache is not found in the local cache it is looked up in the backing Source
 * Based on this child backing cache (or null if not found) the new local cache is created
 * via create() method
 *
 * When flushing children, each child is just flushed if it has backing Source or the whole
 * child cache is put to the MultiCache backing source
 *
 * The primary goal if for caching contract storages in the child repositories (tracks)
 *
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public abstract class MultiCache<V extends CachedSource> extends ReadWriteCache.BytesKey<V> {

    public MultiCache(Source<byte[], V> src) {
        super(src, WriteCache.CacheType.SIMPLE);
    }

    /**
     * When a child cache is not found in the local cache it is looked up in the backing Source
     * Based on this child backing cache (or null if not found) the new local cache is created
     * via create() method
     */
    @Override
    public synchronized V get(byte[] key) {
        AbstractCachedSource.Entry<V> ownCacheEntry = getCached(key);
        V ownCache = ownCacheEntry == null ? null : ownCacheEntry.value();
        if (ownCache == null) {
            V v = getSource() != null ? super.get(key) : null;
            ownCache = create(key, v);
            put(key, ownCache);
        }
        return ownCache;
    }

    /**
     * each child is just flushed if it has backing Source or the whole
     * child cache is put to the MultiCache backing source
     */
    @Override
    public synchronized boolean flushImpl() {
        boolean ret = false;
        for (byte[] key: writeCache.getModified()) {
            V value = super.get(key);
            if (value == null) {
                // cache was deleted
                ret |= flushChild(key, value);
                if (getSource() != null) {
                    getSource().delete(key);
                }
            } else if (value.getSource() != null){
                ret |= flushChild(key, value);
            } else {
                getSource().put(key, value);
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Is invoked to flush child cache if it has backing Source
     * Some additional tasks may be performed by subclasses here
     */
    protected boolean flushChild(byte[] key, V childCache) {
        return childCache != null ? childCache.flush() : true;
    }

    /**
     * Creates a local child cache instance based on the child cache instance
     * (or null) from the MultiCache backing Source
     */
    protected abstract V create(byte[] key, V srcCache);
}
