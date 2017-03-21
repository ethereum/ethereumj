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

import java.util.Collection;

/**
 * Facade class which encapsulates both Read and Write caches
 *
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class ReadWriteCache<Key, Value>
        extends SourceChainBox<Key, Value, Key, Value>
        implements CachedSource<Key, Value> {

    protected ReadCache<Key, Value> readCache;
    protected WriteCache<Key, Value> writeCache;

    protected ReadWriteCache(Source<Key, Value> source) {
        super(source);
    }

    public ReadWriteCache(Source<Key, Value> src, WriteCache.CacheType cacheType) {
        super(src);
        add(writeCache = new WriteCache<>(src, cacheType));
        add(readCache = new ReadCache<>(writeCache));
        readCache.setFlushSource(true);
    }

    @Override
    public synchronized Collection<Key> getModified() {
        return writeCache.getModified();
    }

    @Override
    public boolean hasModified() {
        return writeCache.hasModified();
    }

    protected synchronized AbstractCachedSource.Entry<Value> getCached(Key key) {
        AbstractCachedSource.Entry<Value> v = readCache.getCached(key);
        if (v == null) {
            v = writeCache.getCached(key);
        }
        return v;
    }

    @Override
    public synchronized long estimateCacheSize() {
        return readCache.estimateCacheSize() + writeCache.estimateCacheSize();
    }

    public static class BytesKey<V> extends ReadWriteCache<byte[], V> {
        public BytesKey(Source<byte[], V> src, WriteCache.CacheType cacheType) {
            super(src);
            add(this.writeCache = new WriteCache.BytesKey<>(src, cacheType));
            add(this.readCache = new ReadCache.BytesKey<>(writeCache));
            readCache.setFlushSource(true);
        }
    }
}
