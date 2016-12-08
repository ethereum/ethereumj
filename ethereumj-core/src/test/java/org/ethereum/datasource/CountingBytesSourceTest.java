package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.junit.Assert.*;

/**
 * Test for {@link CountingBytesSource}
 */
public class CountingBytesSourceTest {

    private Source src;

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


    @Before
    public void setUp() {
        this.src = new CountingBytesSource(new HashMapDB());
    }

    @Test(expected = NullPointerException.class)
    public void testKeyNull() {
        src.put(null, null);
    }

    @Test
    public void testValueNull() {
        src.put(intToKey(0), null);
        assertNull(src.get(intToKey(0)));
    }

    @Test
    public void testDelete() {
        src.put(intToKey(0), intToValue(0));
        src.delete(intToKey(0));
        assertNull(src.get(intToKey(0)));

        src.put(intToKey(0), intToValue(0));
        src.put(intToKey(0), intToValue(0));
        src.delete(intToKey(0));
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        src.delete(intToKey(0));
        assertNull(src.get(intToKey(0)));

        src.put(intToKey(1), intToValue(1));
        src.put(intToKey(1), intToValue(1));
        src.put(intToKey(1), null);
        assertEquals(str(intToValue(1)), str(src.get(intToKey(1))));
        src.put(intToKey(1), null);
        assertNull(src.get(intToKey(1)));

        src.put(intToKey(1), intToValue(1));
        src.put(intToKey(1), intToValue(2));
        src.delete(intToKey(1));
        assertEquals(str(intToValue(2)), str(src.get(intToKey(1))));
        src.delete(intToKey(1));
        assertNull(src.get(intToKey(1)));
    }

    @Test
    public void testALotRefs() {
        for (int i = 0; i < 100_000; ++i) {
            src.put(intToKey(0), intToValue(0));
        }

        for (int i = 0; i < 99_999; ++i) {
            src.delete(intToKey(0));
            assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        }
        src.delete(intToKey(0));
        assertNull(src.get(intToKey(0)));
    }

    @Test
    public void testFlushDoNothing() {
        for (int i = 0; i < 100; ++i) {
            for (int j = 0; j <= i; ++j) {
                src.put(intToKey(i), intToValue(i));
            }
        }
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        assertEquals(str(intToValue(99)), str(src.get(intToKey(99))));
        assertFalse(src.flush());
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        assertEquals(str(intToValue(99)), str(src.get(intToKey(99))));
    }

    @Test
    public void testEmptyValue() {
        byte[] value = new byte[0];
        src.put(intToKey(0), value);
        src.put(intToKey(0), value);
        src.delete(intToKey(0));
        assertEquals(str(value), str(src.get(intToKey(0))));
        src.delete(intToKey(0));
        assertNull(src.get(intToKey(0)));
    }
}
