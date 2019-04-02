package org.ethereum.core;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author alexbraz
 * @since 29/03/2019
 */
public class PremineRawTest {

    @Test
    public void testPremineRawNotNull() {

        byte[] addr = "0xcf0f482f2c1ef1f221f09e3cf14122fce0424f94".getBytes();
        PremineRaw pr = new PremineRaw(addr, BigInteger.ONE, Denomination.ETHER);

        assertTrue(pr.getDenomination() == Denomination.ETHER);
        assertEquals(pr.value, BigInteger.ONE);
        assertNotNull(pr.getAddr());
    }
}
