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

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 15.10.2015
 */
public class BIUtilTest {

    @Test
    public void testIsIn20PercentRange() {

        assertTrue(isIn20PercentRange(BigInteger.valueOf(20000), BigInteger.valueOf(24000)));

        assertTrue(isIn20PercentRange(BigInteger.valueOf(24000), BigInteger.valueOf(20000)));

        assertFalse(isIn20PercentRange(BigInteger.valueOf(20000), BigInteger.valueOf(25000)));

        assertTrue(isIn20PercentRange(BigInteger.valueOf(20), BigInteger.valueOf(24)));

        assertTrue(isIn20PercentRange(BigInteger.valueOf(24), BigInteger.valueOf(20)));

        assertFalse(isIn20PercentRange(BigInteger.valueOf(20), BigInteger.valueOf(25)));

        assertTrue(isIn20PercentRange(BigInteger.ZERO, BigInteger.ZERO));

        assertFalse(isIn20PercentRange(BigInteger.ZERO, BigInteger.ONE));

        assertTrue(isIn20PercentRange(BigInteger.ONE, BigInteger.ZERO));
    }

    @Test // test isIn20PercentRange
    public void test1() {
        assertFalse(isIn20PercentRange(BigInteger.ONE, BigInteger.valueOf(5)));
        assertTrue(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.ONE));
        assertTrue(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.valueOf(6)));
        assertFalse(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.valueOf(7)));
    }
}
