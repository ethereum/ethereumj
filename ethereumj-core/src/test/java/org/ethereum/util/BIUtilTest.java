package org.ethereum.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.isIn20PercentRange;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 16.12.2015
 */
public class BIUtilTest {

    @Test // test isIn20PercentRange
    public void test1() {
        assertFalse(isIn20PercentRange(BigInteger.ONE, BigInteger.valueOf(5)));
        assertTrue(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.ONE));
        assertTrue(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.valueOf(6)));
        assertFalse(isIn20PercentRange(BigInteger.valueOf(5), BigInteger.valueOf(7)));
    }
}
