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
 * A kind of source which executes {@link #get(byte[])} query as
 * a {@link DbSource#prefixLookup(byte[], int)} query of backing source.<br>
 *
 * Other operations are simply propagated to backing {@link DbSource}.
 *
 * @author Mikhail Kalinin
 * @since 01.12.2017
 */
public class PrefixLookupSource<V> implements Source<byte[], V> {

    // prefix length in bytes
    private int prefixBytes;
    private DbSource<V> source;

    public PrefixLookupSource(DbSource<V> source, int prefixBytes) {
        this.source = source;
        this.prefixBytes = prefixBytes;
    }

    @Override
    public V get(byte[] key) {
        return source.prefixLookup(key, prefixBytes);
    }

    @Override
    public void put(byte[] key, V val) {
        source.put(key, val);
    }

    @Override
    public void delete(byte[] key) {
        source.delete(key);
    }

    @Override
    public boolean flush() {
        return source.flush();
    }
}
