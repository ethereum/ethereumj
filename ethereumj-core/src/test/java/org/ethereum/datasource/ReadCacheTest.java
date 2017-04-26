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
import static org.junit.Assert.*;

/**
 * Testing {@link ReadCache}
 */
public class ReadCacheTest {

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
    public void test1() {
        Source<byte[], byte[]> src = new HashMapDB<>();
        ReadCache<byte[], byte[]> readCache = new ReadCache.BytesKey<>(src);
        for (int i = 0; i < 10_000; ++i) {
            src.put(intToKey(i), intToValue(i));
        }
        // Nothing is cached
        assertNull(readCache.getCached(intToKey(0)));
        assertNull(readCache.getCached(intToKey(9_999)));

        for (int i = 0; i < 10_000; ++i) {
            readCache.get(intToKey(i));
        }
        // Everything is cached
        assertEquals(str(intToValue(0)), str(readCache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(9_999)), str(readCache.getCached(intToKey(9_999)).value()));

        // Source changes doesn't affect cache
        src.delete(intToKey(13));
        assertEquals(str(intToValue(13)), str(readCache.getCached(intToKey(13)).value()));

        // Flush is not implemented
        assertFalse(readCache.flush());
    }

    @Test
    public void testMaxCapacity() {
        Source<byte[], byte[]> src = new HashMapDB<>();
        ReadCache<byte[], byte[]> readCache = new ReadCache.BytesKey<>(src).withMaxCapacity(100);
        for (int i = 0; i < 10_000; ++i) {
            src.put(intToKey(i), intToValue(i));
            readCache.get(intToKey(i));
        }

        // Only 100 latest are cached
        assertNull(readCache.getCached(intToKey(0)));
        assertEquals(str(intToValue(0)), str(readCache.get(intToKey(0))));
        assertEquals(str(intToValue(0)), str(readCache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(9_999)), str(readCache.getCached(intToKey(9_999)).value()));
        // 99_01 - 99_99 and 0 (totally 100)
        assertEquals(str(intToValue(9_901)), str(readCache.getCached(intToKey(9_901)).value()));
        assertNull(readCache.getCached(intToKey(9_900)));
    }
}
