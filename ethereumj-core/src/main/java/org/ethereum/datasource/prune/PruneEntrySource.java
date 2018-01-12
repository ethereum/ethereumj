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
package org.ethereum.datasource.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;

/**
 * Persistent source of {@link PruneEntry} data. <br/>
 * Featured with bloom filter to avoid false database hits
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public class PruneEntrySource implements Source<byte[], PruneEntry> {

    private byte[] filterKey = HashUtil.sha3("filterKey".getBytes());

    Source<byte[], byte[]> store;
    AbstractCachedSource<byte[], PruneEntry> writeCache;
    AbstractCachedSource<byte[], PruneEntry> readCache;
    QuotientFilter filter;

    boolean dirty = false;

    public PruneEntrySource(Source<byte[], byte[]> src, int cacheSize) {
        this.store = src;

        Source<byte[], PruneEntry> codec = new SourceCodec.BytesKey<>(src, PruneEntry.Serializer);
        writeCache = new AsyncWriteCache<byte[], PruneEntry>(codec) {
            @Override
            protected WriteCache<byte[], PruneEntry> createCache(Source<byte[], PruneEntry> source) {
                WriteCache.BytesKey<PruneEntry> ret = new WriteCache.BytesKey<>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, PruneEntry.MemSizeEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName("pruneSource");

        // 64 bytes - rounded up size of the entry
        readCache = new ReadCache.BytesKey<>(writeCache).withMaxCapacity(cacheSize * 1024 * 1024 / 64);

        byte[] filterBytes = store.get(filterKey);
        if (filterBytes != null) {
            filter = QuotientFilter.deserialize(filterBytes);
        } else {
            filter = QuotientFilter.create(4_000_000, 2_000_000);
        }
    }

    @Override
    public void put(byte[] key, PruneEntry val) {
        readCache.put(key, val);
        filter.insert(key);
        dirty = true;
    }

    @Override
    public PruneEntry get(byte[] key) {
        if (filter.maybeContains(key)) {
            return readCache.get(key);
        } else {
            return null;
        }
    }

    @Override
    public void delete(byte[] key) {
        readCache.delete(key);
        filter.remove(key);
        dirty = true;
    }

    @Override
    public boolean flush() {

        if (dirty) {
            store.put(filterKey, filter.serialize());
            dirty = false;
            return true;
        }

        return false;
    }

    public AbstractCachedSource<byte[], PruneEntry> getWriteCache() {
        return writeCache;
    }
}
