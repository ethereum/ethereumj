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

    private String toString(Object obj) {
        if (obj == null) return null;
        return Hex.toHexString((byte[]) obj);
    }

    @Test
    public void testSimple() {
        DbSource src = new HashMapDB();
        WriteCache writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);
        for (int i = 0; i < 10_000; ++i) {
            writeCache.put(intToKey(i), intToValue(i));
        }
        // Everything is cached
        assertEquals(toString(intToValue(0)), toString(writeCache.getCached(intToKey(0))));
        assertEquals(toString(intToValue(9_999)), toString(writeCache.getCached(intToKey(9_999))));

        // Everything is flushed
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(writeCache.getCached(intToKey(9_999)));
        assertEquals(toString(intToValue(9_999)), toString(writeCache.get(intToKey(9_999))));
        assertEquals(toString(intToValue(0)), toString(writeCache.get(intToKey(0))));
        // Get not caches, only write cache
        assertNull(writeCache.getCached(intToKey(0)));

        // Deleting key that is currently in cache
        writeCache.put(intToKey(0), intToValue(12345));
        assertEquals(toString(intToValue(12345)), toString(writeCache.getCached(intToKey(0))));
        writeCache.delete(intToKey(0));
        assertNull(writeCache.getCached(intToKey(0)));
        assertEquals(toString(intToValue(0)), toString(src.get(intToKey(0))));
        writeCache.flush();
        assertNull(src.get(intToKey(0)));

        // Deleting key that is not currently in cache
        assertNull(writeCache.getCached(intToKey(1)));
        assertEquals(toString(intToValue(1)), toString(src.get(intToKey(1))));
        writeCache.delete(intToKey(1));
        assertNull(writeCache.getCached(intToKey(1)));
        assertEquals(toString(intToValue(1)), toString(src.get(intToKey(1))));
        writeCache.flush();
        assertNull(src.get(intToKey(1)));
    }

    @Test
    public void testCounting() {
        Source src = new CountingBytesSource(new HashMapDB());
        WriteCache writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.COUNTING);
        for (int i = 0; i < 100; ++i) {
            for (int j = 0; j <= i; ++j) {
                writeCache.put(intToKey(i), intToValue(i));
            }
        }
        // Everything is cached
        assertEquals(toString(intToValue(0)), toString(writeCache.getCached(intToKey(0))));
        assertEquals(toString(intToValue(99)), toString(writeCache.getCached(intToKey(99))));

        // Everything is flushed
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(writeCache.getCached(intToKey(99)));
        assertEquals(toString(intToValue(99)), toString(writeCache.get(intToKey(99))));
        assertEquals(toString(intToValue(0)), toString(writeCache.get(intToKey(0))));

        // Deleting key which has 1 ref
        writeCache.delete(intToKey(0));
        // for counting cache we return the cached value even if
        // it was deleted (once or several times) as we don't know
        // how many 'instances' are left behind
        assertEquals(toString(intToValue(0)), toString(writeCache.getCached(intToKey(0))));
        assertEquals(toString(intToValue(0)), toString(src.get(intToKey(0))));
        writeCache.flush();
        assertNull(writeCache.getCached(intToKey(0)));
        assertNull(src.get(intToKey(0)));

        // Deleting key which has 2 refs
        writeCache.delete(intToKey(1));
        writeCache.flush();
        assertEquals(toString(intToValue(1)), toString(writeCache.get(intToKey(1))));
        writeCache.delete(intToKey(1));
        writeCache.flush();
        assertNull(writeCache.get(intToKey(1)));
    }
}
