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
package org.ethereum.db;

import org.ethereum.datasource.QuotientFilter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.intToBytes;

/**
 * Created by Anton Nashatyrev on 13.03.2017.
 */
public class QuotientFilterTest {

    @Ignore
    @Test
    public void perfTest() {
        QuotientFilter f = QuotientFilter.create(50_000_000, 100_000);
        long s = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            f.insert(sha3(intToBytes(i)));

            // inserting duplicate slows down significantly
            if (i % 10 == 0) f.insert(sha3(intToBytes(0)));

            if (i > 100_000 && i % 2 == 0) {
                f.remove(sha3(intToBytes(i - 100_000)));
            }
            if (i % 10000 == 0) {
                System.out.println(i + ": " + (System.currentTimeMillis() - s));
                s = System.currentTimeMillis();
            }
        }
    }

    @Test
    public void maxDuplicatesTest() {
        QuotientFilter f = QuotientFilter.create(50_000_000, 1000).withMaxDuplicates(2);
        f.insert(1);
        Assert.assertTrue(f.maybeContains(1));
        f.remove(1);
        Assert.assertFalse(f.maybeContains(1));

        f.insert(1);
        f.insert(1);
        f.insert(2);
        Assert.assertTrue(f.maybeContains(2));
        f.remove(2);
        Assert.assertFalse(f.maybeContains(2));

        f.remove(1);
        f.remove(1);
        Assert.assertTrue(f.maybeContains(1));

        f.insert(3);
        f.insert(3);
        Assert.assertTrue(f.maybeContains(3));
        f.remove(3);
        f.remove(3);
        Assert.assertTrue(f.maybeContains(3));
    }
}
