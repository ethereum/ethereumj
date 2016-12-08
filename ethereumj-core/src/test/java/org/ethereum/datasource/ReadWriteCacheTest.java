package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        DbSource src = new HashMapDB();
        ReadWriteCache cache = new ReadWriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);

        for (int i = 0; i < 10_000; ++i) {
            cache.put(intToKey(i), intToValue(i));
        }

        // Everything is cached
        assertEquals(str(intToValue(0)), str(cache.getCached(intToKey(0))));
        assertEquals(str(intToValue(9_999)), str(cache.getCached(intToKey(9_999))));

        // Source is empty
        assertNull(src.get(intToKey(0)));
        assertNull(src.get(intToKey(9_999)));

        // After flush src is filled
        cache.flush();
        assertEquals(str(intToValue(9_999)), str(src.get(intToKey(9_999))));
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));

        // Deleting key that is currently in cache
        cache.put(intToKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(cache.getCached(intToKey(0))));
        cache.delete(intToKey(0));
        assertNull(cache.getCached(intToKey(0)));
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        cache.flush();
        assertNull(src.get(intToKey(0)));

        // No size estimators
        assertEquals(0, cache.estimateCacheSize());
    }
}
