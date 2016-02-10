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
