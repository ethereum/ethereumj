package org.ethereum.util;

import org.ethereum.util.slicer.ByteListSlicer;
import org.ethereum.util.slicer.EncodedListSlicer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ListSlicerTest {

    @Test
    public void byteListSlicerTest() {
        List<byte[]> data = new ArrayList<>();
        byte[] data1 = new byte[] {1, 2, 3};
        byte[] data2 = new byte[] {4, 5, 6};
        byte[] data3 = new byte[] {7, 8, 9};
        data.add(data1);
        data.add(data2);
        data.add(data3);

        ByteListSlicer listSlicer = new ByteListSlicer(data, 100);
        assertEquals(2, listSlicer.getEntities().size());
        assertArrayEquals(data1, listSlicer.getEntities().get(0));
        assertArrayEquals(data2, listSlicer.getEntities().get(1));

        ByteListSlicer listSlicer2 = new ByteListSlicer(data, 200);
        assertEquals(3, listSlicer2.getEntities().size());

        ByteListSlicer iteratorSlicer = new ByteListSlicer(data.iterator(), 100);
        assertEquals(2, iteratorSlicer.getEntities().size());
        assertArrayEquals(data1, iteratorSlicer.getEntities().get(0));
        assertArrayEquals(data2, iteratorSlicer.getEntities().get(1));
    }

    @Test
    public void listSlicerTest() {
        List<Value> data = new ArrayList<>();
        String data1 = "abc";
        String data2 = "def";
        String data3 = "ghi";
        data.add(new Value(data1));
        data.add(new Value(data2));
        data.add(new Value(data3));

        EncodedListSlicer<Value> listSlicer = new EncodedListSlicer<>(81, v -> {});
        listSlicer.add(data.get(0));
        listSlicer.add(data.get(1));
        assertEquals(1, listSlicer.getEntities().size());  // At least one could be added even if it definitely will exceed size
        assertEquals(data1, listSlicer.getEntities().get(0).asString());

        EncodedListSlicer<Value> iteratorSlicer = new EncodedListSlicer<>(data.iterator(), 150, v -> {});
        assertEquals(2, iteratorSlicer.getEntities().size());
        assertEquals(data1, iteratorSlicer.getEntities().get(0).asString());
        assertEquals(data2, iteratorSlicer.getEntities().get(1).asString());
    }
}
