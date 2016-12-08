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
        DbSource src = new HashMapDB();
        ReadCache readCache = new ReadCache.BytesKey<>(src);
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
        assertEquals(str(intToValue(0)), str(readCache.getCached(intToKey(0))));
        assertEquals(str(intToValue(9_999)), str(readCache.getCached(intToKey(9_999))));

        // Source changes doesn't affect cache
        src.delete(intToKey(13));
        assertEquals(str(intToValue(13)), str(readCache.getCached(intToKey(13))));

        // Flush is not implemented
        assertFalse(readCache.flush());
    }

    @Test
    public void testMaxCapacity() {
        DbSource src = new HashMapDB();
        ReadCache readCache = new ReadCache.BytesKey<>(src).withMaxCapacity(100);
        for (int i = 0; i < 10_000; ++i) {
            src.put(intToKey(i), intToValue(i));
            readCache.get(intToKey(i));
        }

        // Only 100 latest are cached
        assertNull(readCache.getCached(intToKey(0)));
        assertEquals(str(intToValue(0)), str(readCache.get(intToKey(0))));
        assertEquals(str(intToValue(0)), str(readCache.getCached(intToKey(0))));
        assertEquals(str(intToValue(9_999)), str(readCache.getCached(intToKey(9_999))));
        // 99_01 - 99_99 and 0 (totally 100)
        assertEquals(str(intToValue(9_901)), str(readCache.getCached(intToKey(9_901))));
        assertNull(readCache.getCached(intToKey(9_900)));
    }
}
