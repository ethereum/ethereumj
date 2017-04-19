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
