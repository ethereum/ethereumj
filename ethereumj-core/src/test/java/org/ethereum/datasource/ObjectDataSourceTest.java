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
 * Test for {@link ObjectDataSource}
 */
public class ObjectDataSourceTest {

    private byte[] intToKey(int i) {
        return sha3(longToBytes(i));
    }

    private byte[] intToValue(int i) {
        return (new DataWord(i)).getData();
    }

    private DataWord intToDataWord(int i) {
        return new DataWord(i);
    }

    private String str(Object obj) {
        if (obj == null) return null;

        byte[] data;
        if (obj instanceof DataWord) {
            data = ((DataWord) obj).getData();
        } else {
            data = (byte[]) obj;
        }

        return Hex.toHexString(data);
    }

    @Test
    public void testDummySerializer() {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Serializer<byte[], byte[]> serializer = new Serializers.Identity<>();
        ObjectDataSource<byte[]> src = new ObjectDataSource<>(parentSrc, serializer, 256);

        for (int i = 0; i < 10_000; ++i) {
            src.put(intToKey(i), intToValue(i));
        }
        // Everything is in src and parentSrc w/o flush
        assertEquals(str(intToValue(0)), str(src.get(intToKey(0))));
        assertEquals(str(intToValue(9_999)), str(src.get(intToKey(9_999))));
        assertEquals(str(intToValue(0)), str(parentSrc.get(intToKey(0))));
        assertEquals(str(intToValue(9_999)), str(parentSrc.get(intToKey(9_999))));

        // Testing read cache is available
        parentSrc.delete(intToKey(9_999));
        assertEquals(str(intToValue(9_999)), str(src.get(intToKey(9_999))));
        src.delete(intToKey(9_999));

        // Testing src delete invalidates read cache
        src.delete(intToKey(9_998));
        assertNull(src.get(intToKey(9_998)));

        // Modifying key
        src.put(intToKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(src.get(intToKey(0))));
        assertEquals(str(intToValue(12345)), str(parentSrc.get(intToKey(0))));
    }

    @Test
    public void testDataWordValueSerializer() {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Serializer<DataWord, byte[]> serializer = Serializers.StorageValueSerializer;
        ObjectDataSource<DataWord> src = new ObjectDataSource<>(parentSrc, serializer, 256);

        for (int i = 0; i < 10_000; ++i) {
            src.put(intToKey(i), intToDataWord(i));
        }

        // Everything is in src
        assertEquals(str(intToDataWord(0)), str(src.get(intToKey(0))));
        assertEquals(str(intToDataWord(9_999)), str(src.get(intToKey(9_999))));

        // Modifying key
        src.put(intToKey(0), intToDataWord(12345));
        assertEquals(str(intToDataWord(12345)), str(src.get(intToKey(0))));
    }
}
