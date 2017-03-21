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

import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ByteUtilTest {

    @Test
    public void testAppendByte() {
        byte[] bytes = "tes".getBytes();
        byte b = 0x74;
        Assert.assertArrayEquals("test".getBytes(), ByteUtil.appendByte(bytes, b));
    }

    @Test
    public void testBigIntegerToBytes() {
        byte[] expecteds = new byte[]{(byte) 0xff, (byte) 0xec, 0x78};
        BigInteger b = BigInteger.valueOf(16772216);
        byte[] actuals = ByteUtil.bigIntegerToBytes(b);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testBigIntegerToBytesSign() {
        {
            BigInteger b = BigInteger.valueOf(-2);
            byte[] actuals = ByteUtil.bigIntegerToBytesSigned(b, 8);
            assertArrayEquals(Hex.decode("fffffffffffffffe"), actuals);
        }
        {
            BigInteger b = BigInteger.valueOf(2);
            byte[] actuals = ByteUtil.bigIntegerToBytesSigned(b, 8);
            assertArrayEquals(Hex.decode("0000000000000002"), actuals);
        }
        {
            BigInteger b = BigInteger.valueOf(0);
            byte[] actuals = ByteUtil.bigIntegerToBytesSigned(b, 8);
            assertArrayEquals(Hex.decode("0000000000000000"), actuals);
        }
        {
            BigInteger b = new BigInteger("eeeeeeeeeeeeee", 16);
            byte[] actuals = ByteUtil.bigIntegerToBytesSigned(b, 8);
            assertArrayEquals(Hex.decode("00eeeeeeeeeeeeee"), actuals);
        }
        {
            BigInteger b = new BigInteger("eeeeeeeeeeeeeeee", 16);
            byte[] actuals = ByteUtil.bigIntegerToBytesSigned(b, 8);
            assertArrayEquals(Hex.decode("eeeeeeeeeeeeeeee"), actuals);
        }
    }

    @Test
    public void testBigIntegerToBytesNegative() {
        byte[] expecteds = new byte[]{(byte) 0xff, 0x0, 0x13, (byte) 0x88};
        BigInteger b = BigInteger.valueOf(-16772216);
        byte[] actuals = ByteUtil.bigIntegerToBytes(b);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testBigIntegerToBytesZero() {
        byte[] expecteds = new byte[]{0x00};
        BigInteger b = BigInteger.ZERO;
        byte[] actuals = ByteUtil.bigIntegerToBytes(b);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testToHexString() {
        assertEquals("", ByteUtil.toHexString(null));
    }

    @Test
    public void testCalcPacketLength() {
        byte[] test = new byte[]{0x0f, 0x10, 0x43};
        byte[] expected = new byte[]{0x00, 0x00, 0x00, 0x03};
        assertArrayEquals(expected, ByteUtil.calcPacketLength(test));
    }

    @Test
    public void testByteArrayToInt() {
        assertEquals(0, ByteUtil.byteArrayToInt(null));
        assertEquals(0, ByteUtil.byteArrayToInt(new byte[0]));

//      byte[] x = new byte[] { 5,1,7,0,8 };
//      long start = System.currentTimeMillis();
//      for (int i = 0; i < 100000000; i++) {
//           ByteArray.read32bit(x, 0);
//      }
//      long end = System.currentTimeMillis();
//      System.out.println(end - start + "ms");
//
//      long start1 = System.currentTimeMillis();
//      for (int i = 0; i < 100000000; i++) {
//          new BigInteger(1, x).intValue();
//      }
//      long end1 = System.currentTimeMillis();
//      System.out.println(end1 - start1 + "ms");

    }

    @Test
    public void testNumBytes() {
        String test1 = "0";
        String test2 = "1";
        String test3 = "1000000000"; //3B9ACA00
        int expected1 = 1;
        int expected2 = 1;
        int expected3 = 4;
        assertEquals(expected1, ByteUtil.numBytes(test1));
        assertEquals(expected2, ByteUtil.numBytes(test2));
        assertEquals(expected3, ByteUtil.numBytes(test3));
    }

    @Test
    public void testStripLeadingZeroes() {
        byte[] test1 = null;
        byte[] test2 = new byte[]{};
        byte[] test3 = new byte[]{0x00};
        byte[] test4 = new byte[]{0x00, 0x01};
        byte[] test5 = new byte[]{0x00, 0x00, 0x01};
        byte[] expected1 = null;
        byte[] expected2 = new byte[]{0};
        byte[] expected3 = new byte[]{0};
        byte[] expected4 = new byte[]{0x01};
        byte[] expected5 = new byte[]{0x01};
        assertArrayEquals(expected1, ByteUtil.stripLeadingZeroes(test1));
        assertArrayEquals(expected2, ByteUtil.stripLeadingZeroes(test2));
        assertArrayEquals(expected3, ByteUtil.stripLeadingZeroes(test3));
        assertArrayEquals(expected4, ByteUtil.stripLeadingZeroes(test4));
        assertArrayEquals(expected5, ByteUtil.stripLeadingZeroes(test5));
    }

    @Test
    public void testMatchingNibbleLength1() {
        // a larger than b
        byte[] a = new byte[]{0x00, 0x01};
        byte[] b = new byte[]{0x00};
        int result = ByteUtil.matchingNibbleLength(a, b);
        assertEquals(1, result);
    }

    @Test
    public void testMatchingNibbleLength2() {
        // b larger than a
        byte[] a = new byte[]{0x00};
        byte[] b = new byte[]{0x00, 0x01};
        int result = ByteUtil.matchingNibbleLength(a, b);
        assertEquals(1, result);
    }

    @Test
    public void testMatchingNibbleLength3() {
        // a and b the same length equal
        byte[] a = new byte[]{0x00};
        byte[] b = new byte[]{0x00};
        int result = ByteUtil.matchingNibbleLength(a, b);
        assertEquals(1, result);
    }

    @Test
    public void testMatchingNibbleLength4() {
        // a and b the same length not equal
        byte[] a = new byte[]{0x01};
        byte[] b = new byte[]{0x00};
        int result = ByteUtil.matchingNibbleLength(a, b);
        assertEquals(0, result);
    }

    @Test
    public void testNiceNiblesOutput_1() {
        byte[] test = {7, 0, 7, 5, 7, 0, 7, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x05\\x07\\x00\\x07\\x00\\x07\\x09";
        assertEquals(result, ByteUtil.nibblesToPrettyString(test));
    }

    @Test
    public void testNiceNiblesOutput_2() {
        byte[] test = {7, 0, 7, 0xf, 7, 0, 0xa, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x0f\\x07\\x00\\x0a\\x00\\x07\\x09";
        assertEquals(result, ByteUtil.nibblesToPrettyString(test));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchingNibbleLength5() {
        // a == null
        byte[] a = null;
        byte[] b = new byte[]{0x00};
        ByteUtil.matchingNibbleLength(a, b);
    }

    @Test(expected = NullPointerException.class)
    public void testMatchingNibbleLength6() {
        // b == null
        byte[] a = new byte[]{0x00};
        byte[] b = null;
        ByteUtil.matchingNibbleLength(a, b);
    }

    @Test
    public void testMatchingNibbleLength7() {
        // a or b is empty
        byte[] a = new byte[0];
        byte[] b = new byte[]{0x00};
        int result = ByteUtil.matchingNibbleLength(a, b);
        assertEquals(0, result);
    }

    /**
     * This test shows the difference between iterating over,
     * and comparing byte[] vs BigInteger value.
     *
     * Results indicate that the former has ~15x better performance.
     * Therefore this is used in the Miner.mine() method.
     */
    @Test
    public void testIncrementPerformance() {
        boolean testEnabled = false;

        if (testEnabled) {
            byte[] counter1 = new byte[4];
            byte[] max = ByteBuffer.allocate(4).putInt(Integer.MAX_VALUE).array();
            long start1 = System.currentTimeMillis();
            while (ByteUtil.increment(counter1)) {
                if (FastByteComparisons.compareTo(counter1, 0, 4, max, 0, 4) == 0) {
                    break;
                }
            }
            System.out.println(System.currentTimeMillis() - start1 + "ms to reach: " + Hex.toHexString(counter1));

            BigInteger counter2 = BigInteger.ZERO;
            long start2 = System.currentTimeMillis();
            while (true) {
                if (counter2.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 0) {
                    break;
                }
                counter2 = counter2.add(BigInteger.ONE);
            }
            System.out.println(System.currentTimeMillis() - start2 + "ms to reach: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(4, counter2)));
        }
    }


    @Test
    public void firstNonZeroByte_1() {

        byte[] data = Hex.decode("0000000000000000000000000000000000000000000000000000000000000000");
        int result = ByteUtil.firstNonZeroByte(data);

        assertEquals(-1, result);
    }

    @Test
    public void firstNonZeroByte_2() {

        byte[] data = Hex.decode("0000000000000000000000000000000000000000000000000000000000332211");
        int result = ByteUtil.firstNonZeroByte(data);

        assertEquals(29, result);
    }

    @Test
    public void firstNonZeroByte_3() {

        byte[] data = Hex.decode("2211009988776655443322110099887766554433221100998877665544332211");
        int result = ByteUtil.firstNonZeroByte(data);

        assertEquals(0, result);
    }

    @Test
    public void setBitTest() {
        /*
            Set on
         */
        byte[] data = ByteBuffer.allocate(4).putInt(0).array();
        int posBit = 24;
        int expected = 16777216;
        int result = -1;
        byte[] ret = ByteUtil.setBit(data, posBit, 1);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);

        posBit = 25;
        expected = 50331648;
        ret = ByteUtil.setBit(data, posBit, 1);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);

        posBit = 2;
        expected = 50331652;
        ret = ByteUtil.setBit(data, posBit, 1);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);

        /*
            Set off
         */
        posBit = 24;
        expected = 33554436;
        ret = ByteUtil.setBit(data, posBit, 0);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);

        posBit = 25;
        expected = 4;
        ret = ByteUtil.setBit(data, posBit, 0);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);

        posBit = 2;
        expected = 0;
        ret = ByteUtil.setBit(data, posBit, 0);
        result = ByteUtil.byteArrayToInt(ret);
        assertTrue(expected == result);
    }

    @Test
    public void getBitTest() {
        byte[] data = ByteBuffer.allocate(4).putInt(0).array();
        ByteUtil.setBit(data, 24, 1);
        ByteUtil.setBit(data, 25, 1);
        ByteUtil.setBit(data, 2, 1);

        List<Integer> found = new ArrayList<>();
        for (int i = 0; i < (data.length * 8); i++) {
            int res = ByteUtil.getBit(data, i);
            if (res == 1)
                if (i != 24 && i != 25 && i != 2)
                    assertTrue(false);
                else
                    found.add(i);
            else {
                if (i == 24 || i == 25 || i == 2)
                    assertTrue(false);
            }
        }

        if (found.size() != 3)
            assertTrue(false);
        assertTrue(found.get(0) == 2);
        assertTrue(found.get(1) == 24);
        assertTrue(found.get(2) == 25);
    }

    @Test
    public void numToBytesTest() {
        byte[] bytes = ByteUtil.intToBytesNoLeadZeroes(-1);
        assertArrayEquals(bytes, Hex.decode("ffffffff"));
        bytes = ByteUtil.intToBytesNoLeadZeroes(1);
        assertArrayEquals(bytes, Hex.decode("01"));
        bytes = ByteUtil.intToBytesNoLeadZeroes(255);
        assertArrayEquals(bytes, Hex.decode("ff"));
        bytes = ByteUtil.intToBytesNoLeadZeroes(256);
        assertArrayEquals(bytes, Hex.decode("0100"));
        bytes = ByteUtil.intToBytesNoLeadZeroes(0);
        assertArrayEquals(bytes, new byte[0]);

        bytes = ByteUtil.intToBytes(-1);
        assertArrayEquals(bytes, Hex.decode("ffffffff"));
        bytes = ByteUtil.intToBytes(1);
        assertArrayEquals(bytes, Hex.decode("00000001"));
        bytes = ByteUtil.intToBytes(255);
        assertArrayEquals(bytes, Hex.decode("000000ff"));
        bytes = ByteUtil.intToBytes(256);
        assertArrayEquals(bytes, Hex.decode("00000100"));
        bytes = ByteUtil.intToBytes(0);
        assertArrayEquals(bytes, Hex.decode("00000000"));

        bytes = ByteUtil.longToBytesNoLeadZeroes(-1);
        assertArrayEquals(bytes, Hex.decode("ffffffffffffffff"));
        bytes = ByteUtil.longToBytesNoLeadZeroes(1);
        assertArrayEquals(bytes, Hex.decode("01"));
        bytes = ByteUtil.longToBytesNoLeadZeroes(255);
        assertArrayEquals(bytes, Hex.decode("ff"));
        bytes = ByteUtil.longToBytesNoLeadZeroes(1L << 32);
        assertArrayEquals(bytes, Hex.decode("0100000000"));
        bytes = ByteUtil.longToBytesNoLeadZeroes(0);
        assertArrayEquals(bytes, new byte[0]);

        bytes = ByteUtil.longToBytes(-1);
        assertArrayEquals(bytes, Hex.decode("ffffffffffffffff"));
        bytes = ByteUtil.longToBytes(1);
        assertArrayEquals(bytes, Hex.decode("0000000000000001"));
        bytes = ByteUtil.longToBytes(255);
        assertArrayEquals(bytes, Hex.decode("00000000000000ff"));
        bytes = ByteUtil.longToBytes(256);
        assertArrayEquals(bytes, Hex.decode("0000000000000100"));
        bytes = ByteUtil.longToBytes(0);
        assertArrayEquals(bytes, Hex.decode("0000000000000000"));
    }

    @Test
    public void testHexStringToBytes() {
        {
            String str = "0000";
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {0, 0};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0x0000";
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {0, 0};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0x45a6";
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {69, -90};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "1963093cee500c081443e1045c40264b670517af";
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = Hex.decode(str);
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0x"; // Empty
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0"; // Same as 0x00
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {0};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0x00"; // This case shouldn't be empty array
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {0};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0xd"; // Should work with odd length, adding leading 0
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {13};
            assertArrayEquals(expected, actuals);
        }
        {
            String str = "0xd0d"; // Should work with odd length, adding leading 0
            byte[] actuals = ByteUtil.hexStringToBytes(str);
            byte[] expected = new byte[] {13, 13};
            assertArrayEquals(expected, actuals);
        }
    }

    @Test
    public void testIpConversion() {
        String ip1 = "0.0.0.0";
        byte[] ip1Bytes = ByteUtil.hostToBytes(ip1);
        assertEquals(ip1, ByteUtil.bytesToIp(ip1Bytes));

        String ip2 = "35.36.37.138";
        byte[] ip2Bytes = ByteUtil.hostToBytes(ip2);
        assertEquals(ip2, ByteUtil.bytesToIp(ip2Bytes));

        String ip3 = "255.255.255.255";
        byte[] ip3Bytes = ByteUtil.hostToBytes(ip3);
        assertEquals(ip3, ByteUtil.bytesToIp(ip3Bytes));

        // Fallback case
        String ip4 = "255.255.255.256";
        byte[] ip4Bytes = ByteUtil.hostToBytes(ip4);
        assertEquals("0.0.0.0", ByteUtil.bytesToIp(ip4Bytes));
    }
}
