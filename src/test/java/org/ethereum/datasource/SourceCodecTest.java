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

/**
 * Test for {@link SourceCodec}
 */
public class SourceCodecTest {

    private byte[] intToKey(int i) {
        return sha3(longToBytes(i));
    }

    private byte[] intToValue(int i) {
        return (new DataWord(i)).getData();
    }

    private DataWord intToDataWord(int i) {
        return new DataWord(i);
    }

    private DataWord intToDataWordKey(int i) {
        return new DataWord(intToKey(i));
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
    public void testDataWordKeySerializer() {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Serializer<DataWord, byte[]> keySerializer = Serializers.StorageKeySerializer;
        Serializer<byte[], byte[]> valueSerializer = new Serializers.Identity<>();
        SourceCodec<DataWord, byte[], byte[], byte[]> src = new SourceCodec<>(parentSrc, keySerializer, valueSerializer);

        for (int i = 0; i < 10_000; ++i) {
            src.put(intToDataWordKey(i), intToValue(i));
        }

        // Everything is in src
        assertEquals(str(intToValue(0)), str(src.get(intToDataWordKey(0))));
        assertEquals(str(intToValue(9_999)), str(src.get(intToDataWordKey(9_999))));

        // Modifying key
        src.put(intToDataWordKey(0), intToValue(12345));
        assertEquals(str(intToValue(12345)), str(src.get(intToDataWordKey(0))));

        // Testing there is no cache
        assertEquals(str(intToValue(9_990)), str(src.get(intToDataWordKey(9_990))));
        parentSrc.delete(keySerializer.serialize(intToDataWordKey(9_990)));
        assertNull(src.get(intToDataWordKey(9_990)));
    }

    @Test
    public void testDataWordKeyValueSerializer() {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Serializer<DataWord, byte[]> keySerializer = Serializers.StorageKeySerializer;
        Serializer<DataWord, byte[]> valueSerializer = Serializers.StorageValueSerializer;
        SourceCodec<DataWord, DataWord, byte[], byte[]> src = new SourceCodec<>(parentSrc, keySerializer, valueSerializer);

        for (int i = 0; i < 10_000; ++i) {
            src.put(intToDataWordKey(i), intToDataWord(i));
        }

        // Everything is in src
        assertEquals(str(intToDataWord(0)), str(src.get(intToDataWordKey(0))));
        assertEquals(str(intToDataWord(9_999)), str(src.get(intToDataWordKey(9_999))));

        // Modifying key
        src.put(intToDataWordKey(0), intToDataWord(12345));
        assertEquals(str(intToDataWord(12345)), str(src.get(intToDataWordKey(0))));
    }
}
