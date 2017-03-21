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
 * Just a convenient class to store arbitrary Objects into byte[] value backing
 * Source.
 * Includes ReadCache for caching deserialized objects and object Serializer
 *
 * Created by Anton Nashatyrev on 06.12.2016.
 */
public class ObjectDataSource<V> extends SourceChainBox<byte[], V, byte[], byte[]> {
    ReadCache<byte[], V> cache;
    SourceCodec<byte[], V, byte[], byte[]> codec;
    Source<byte[], byte[]> byteSource;

    /**
     * Creates new instance
     * @param byteSource baking store
     * @param serializer for encode/decode byte[] <=> V
     * @param readCacheEntries number of entries to cache
     */
    public ObjectDataSource(Source<byte[], byte[]> byteSource, Serializer<V, byte[]> serializer, int readCacheEntries) {
        super(byteSource);
        this.byteSource = byteSource;
        add(codec = new SourceCodec<>(byteSource, new Serializers.Identity<byte[]>(), serializer));
        if (readCacheEntries > 0) {
            add(cache = new ReadCache.BytesKey<>(codec).withMaxCapacity(readCacheEntries));
        }
    }
}
