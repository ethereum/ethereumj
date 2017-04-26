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

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Testing {@link ReadWriteCache}
 */
public class ReadWriteCacheTest {

    private byte[] intToKey(int i) {
        return sha3(longToBytes(i));
    }

    private byte[] intToValue(int i) {
        return (new DataWord(i)).getData();
    }

    private String str(Object obj) {
        if (obj == null) return null;
        return Hex.toHexString((byte[]) obj);
    }

    @Test
    public void testSimple() {
        Source<byte[], byte[]> src = new HashMapDB<>();
        ReadWriteCache<byte[], byte[]> cache = new ReadWriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);

        for (int i = 0; i < 10_000; ++i) {
            cache.put(intToKey(i), intToValue(i));
        }

        // Everything is cached
        assertEquals(str(intToValue(0)), str(cache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(9_999)), str(cache.getCached(intToKey(9_999)).value()));

        // Source is empty
        assertNull(src.get(intToKey(0)));
        assertNull(src.get(intToKey(9_999)));

        // After flush src is filled
        cache.flush();
        assertEquals(str(intToValue(9_999)), str(src.get(intToKey(9_999))));
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));

        // Deleting key that is currently in cache
        cache.put(intToKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(cache.getCached(intToKey(0)).value()));
        cache.delete(intToKey(0));
        assertTrue(null == cache.getCached(intToKey(0)) || null == cache.getCached(intToKey(0)).value());
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        cache.flush();
        assertNull(src.get(intToKey(0)));

        // No size estimators
        assertEquals(0, cache.estimateCacheSize());
    }
}
