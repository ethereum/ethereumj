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
 * Testing {@link WriteCache}
 */
public class WriteCacheTest {

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
        WriteCache<byte[], byte[]> writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);
        for (int i = 0; i < 10_000; ++i) {
            writeCache.put(intToKey(i), intToValue(i));
        }
        // Everything is cached
        assertEquals(str(intToValue(0)), str(writeCache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(9_999)), str(writeCache.getCached(intToKey(9_999)).value()));

        // Everything is flushed
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(writeCache.getCached(intToKey(9_999)));
        assertEquals(str(intToValue(9_999)), str(writeCache.get(intToKey(9_999))));
        assertEquals(str(intToValue(0)), str(writeCache.get(intToKey(0))));
        // Get not caches, only write cache
        assertNull(writeCache.getCached(intToKey(0)));

        // Deleting key that is currently in cache
        writeCache.put(intToKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(writeCache.getCached(intToKey(0)).value()));
        writeCache.delete(intToKey(0));
        assertTrue(null == writeCache.getCached(intToKey(0)) || null == writeCache.getCached(intToKey(0)).value());
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        writeCache.flush();
        assertNull(src.get(intToKey(0)));

        // Deleting key that is not currently in cache
        assertTrue(null == writeCache.getCached(intToKey(1)) || null == writeCache.getCached(intToKey(1)).value());
        assertEquals(str(intToValue(1)), str(src.get(intToKey(1))));
        writeCache.delete(intToKey(1));
        assertTrue(null == writeCache.getCached(intToKey(1)) || null == writeCache.getCached(intToKey(1)).value());
        assertEquals(str(intToValue(1)), str(src.get(intToKey(1))));
        writeCache.flush();
        assertNull(src.get(intToKey(1)));
    }

    @Test
    public void testCounting() {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Source<byte[], byte[]> src = new CountingBytesSource(parentSrc);
        WriteCache<byte[], byte[]> writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.COUNTING);
        for (int i = 0; i < 100; ++i) {
            for (int j = 0; j <= i; ++j) {
                writeCache.put(intToKey(i), intToValue(i));
            }
        }
        // Everything is cached
        assertEquals(str(intToValue(0)), str(writeCache.getCached(intToKey(0)).value()));
        assertEquals(str(intToValue(99)), str(writeCache.getCached(intToKey(99)).value()));

        // Everything is flushed
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(writeCache.getCached(intToKey(99)));
        assertEquals(str(intToValue(99)), str(writeCache.get(intToKey(99))));
        assertEquals(str(intToValue(0)), str(writeCache.get(intToKey(0))));

        // Deleting key which has 1 ref
        writeCache.delete(intToKey(0));

        // for counting cache we return the cached value even if
        // it was deleted (once or several times) as we don't know
        // how many 'instances' are left behind

        // but when we delete entry which is not in the cache we don't
        // want to spend unnecessary time for getting the value from
        // underlying storage, so getCached may return null.
        // get() should work as expected
//        assertEquals(str(intToValue(0)), str(writeCache.getCached(intToKey(0))));

        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(src.get(intToKey(0)));

        // Deleting key which has 2 refs
        writeCache.delete(intToKey(1));
        writeCache.flush();
        assertEquals(str(intToValue(1)), str(writeCache.get(intToKey(1))));
        writeCache.delete(intToKey(1));
        writeCache.flush();
        assertNull(writeCache.get(intToKey(1)));
    }

    @Test
    public void testWithSizeEstimator() {
        Source<byte[], byte[]> src = new HashMapDB<>();
        WriteCache<byte[], byte[]> writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        assertEquals(0, writeCache.estimateCacheSize());

        writeCache.put(intToKey(0), intToValue(0));
        assertNotEquals(0, writeCache.estimateCacheSize());
        long oneObjSize = writeCache.estimateCacheSize();

        for (int i = 0; i < 100; ++i) {
            for (int j = 0; j <= i; ++j) {
                writeCache.put(intToKey(i), intToValue(i));
            }
        }
        assertEquals(oneObjSize * 100, writeCache.estimateCacheSize());

        writeCache.flush();
        assertEquals(0, writeCache.estimateCacheSize());
    }
}
