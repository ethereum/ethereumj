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
package org.ethereum.net.shh;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Nashatyrev on 24.09.2015.
 */
public class BloomFilterTest {
    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test
    public void test1() {
        BloomFilter filter = BloomFilter.createNone();

        for (int i = 0; i < 100; i++) {
            filter.addTopic(new Topic("Topic" + i));
        }

        for (int i = 0; i < 100; i++) {
            assertTrue("Topic #" + i, filter.hasTopic(new Topic("Topic" + i)));
        }

        int falsePositiveCnt = 0;
        for (int i = 0; i < 10000; i++) {
            falsePositiveCnt += filter.hasTopic(new Topic("Topic_" + i)) ? 1 : 0;
        }
        logger.info("falsePositiveCnt: " + falsePositiveCnt);
        assertTrue(falsePositiveCnt < 1000); // false positive probability ~8.7%
    }
}
