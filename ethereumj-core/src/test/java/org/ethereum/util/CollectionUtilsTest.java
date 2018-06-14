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

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest {

    @Test
    public void test() {
        final List<Integer> input = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        assertEquals(10, input.size());

        List<Integer> resEqual = CollectionUtils.truncateRand(input, 10);
        assertArrayEquals(input.toArray(), resEqual.toArray());

        List<Integer> resEqual2 = CollectionUtils.truncateRand(input, 20);
        assertArrayEquals(input.toArray(), resEqual2.toArray());

        Set<Integer> excluded = new HashSet<>();
        for (int i = 0; i < 1000; ++i) {
            List<Integer> resMinusOne = CollectionUtils.truncateRand(input, 9);
            Set<Integer> resMinusOneSet = new HashSet<>(resMinusOne);
            assertEquals(resMinusOne.size(), resMinusOneSet.size());
            AtomicInteger exclusionCounter = new AtomicInteger(0);
            input.forEach(x -> {
                if(!resMinusOneSet.contains(x)) {
                    excluded.add(x);
                    exclusionCounter.getAndIncrement();
                }
            });
            assertEquals(1, exclusionCounter.get());
        }
        assertEquals("Someday I'll fail due to the nature of random", 10, excluded.size());

        Set<Integer> included = new HashSet<>();
        for (int i = 0; i < 1000; ++i) {
            List<Integer> resOne = CollectionUtils.truncateRand(input, 1);
            included.add(resOne.get(0));
            assertTrue(input.contains(resOne.get(0)));
        }
        assertEquals("Someday I'll fail due to the nature of random", 10, included.size());

        assertEquals(3, CollectionUtils.truncateRand(input, 3).size());
    }
}
