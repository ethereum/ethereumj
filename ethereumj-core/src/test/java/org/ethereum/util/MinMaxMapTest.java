package org.ethereum.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Anton Nashatyrev on 08.12.2016.
 */
public class MinMaxMapTest {

    @Test
    public void test1() {
        MinMaxMap<Integer> map = new MinMaxMap<>();
        assertNull(map.getMin());
        assertNull(map.getMax());
        map.clearAllAfter(100);
        map.clearAllBefore(100);

        map.put(100L, 100);
        assertEquals(100, map.getMin().longValue());
        assertEquals(100, map.getMax().longValue());
        map.clearAllAfter(100);
        assertEquals(1, map.size());
        map.clearAllBefore(100);
        assertEquals(1, map.size());
        map.clearAllBefore(101);
        assertEquals(0, map.size());

        map.put(100L, 100);
        assertEquals(1, map.size());
        map.clearAllAfter(99);
        assertEquals(0, map.size());

        map.put(100L, 100);
        map.put(110L, 100);
        map.put(90L, 100);
        assertEquals(90, map.getMin().longValue());
        assertEquals(110, map.getMax().longValue());

        map.remove(100L);
        assertEquals(90, map.getMin().longValue());
        assertEquals(110, map.getMax().longValue());

        map.remove(110L);
        assertEquals(90, map.getMin().longValue());
        assertEquals(90, map.getMax().longValue());
    }
}
