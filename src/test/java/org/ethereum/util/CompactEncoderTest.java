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

import static org.junit.Assert.assertArrayEquals;

public class CompactEncoderTest {

    private final static byte T = 16; // terminator

    @Test
    public void testCompactEncodeOddCompact() {
        byte[] test = new byte[]{1, 2, 3, 4, 5};
        byte[] expectedData = new byte[]{0x11, 0x23, 0x45};
        assertArrayEquals("odd compact encode fail", expectedData, CompactEncoder.packNibbles(test));
    }

    @Test
    public void testCompactEncodeEvenCompact() {
        byte[] test = new byte[]{0, 1, 2, 3, 4, 5};
        byte[] expectedData = new byte[]{0x00, 0x01, 0x23, 0x45};
        assertArrayEquals("even compact encode fail", expectedData, CompactEncoder.packNibbles(test));
    }

    @Test
    public void testCompactEncodeEvenTerminated() {
        byte[] test = new byte[]{0, 15, 1, 12, 11, 8, T};
        byte[] expectedData = new byte[]{0x20, 0x0f, 0x1c, (byte) 0xb8};
        assertArrayEquals("even terminated compact encode fail", expectedData, CompactEncoder.packNibbles(test));
    }

    @Test
    public void testCompactEncodeOddTerminated() {
        byte[] test = new byte[]{15, 1, 12, 11, 8, T};
        byte[] expectedData = new byte[]{0x3f, 0x1c, (byte) 0xb8};
        assertArrayEquals("odd terminated compact encode fail", expectedData, CompactEncoder.packNibbles(test));
    }

    @Test
    public void testCompactDecodeOddCompact() {
        byte[] test = new byte[]{0x11, 0x23, 0x45};
        byte[] expected = new byte[]{1, 2, 3, 4, 5};
        assertArrayEquals("odd compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
    }

    @Test
    public void testCompactDecodeEvenCompact() {
        byte[] test = new byte[]{0x00, 0x01, 0x23, 0x45};
        byte[] expected = new byte[]{0, 1, 2, 3, 4, 5};
        assertArrayEquals("even compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
    }

    @Test
    public void testCompactDecodeEvenTerminated() {
        byte[] test = new byte[]{0x20, 0x0f, 0x1c, (byte) 0xb8};
        byte[] expected = new byte[]{0, 15, 1, 12, 11, 8, T};
        assertArrayEquals("even terminated compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
    }

    @Test
    public void testCompactDecodeOddTerminated() {
        byte[] test = new byte[]{0x3f, 0x1c, (byte) 0xb8};
        byte[] expected = new byte[]{15, 1, 12, 11, 8, T};
        assertArrayEquals("odd terminated compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
    }

    @Test
    public void testCompactHexEncode_1() {
        byte[] test = "stallion".getBytes();
        byte[] result = new byte[]{7, 3, 7, 4, 6, 1, 6, 12, 6, 12, 6, 9, 6, 15, 6, 14, T};
        assertArrayEquals(result, CompactEncoder.binToNibbles(test));
    }

    @Test
    public void testCompactHexEncode_2() {
        byte[] test = "verb".getBytes();
        byte[] result = new byte[]{7, 6, 6, 5, 7, 2, 6, 2, T};
        assertArrayEquals(result, CompactEncoder.binToNibbles(test));
    }

    @Test
    public void testCompactHexEncode_3() {
        byte[] test = "puppy".getBytes();
        byte[] result = new byte[]{7, 0, 7, 5, 7, 0, 7, 0, 7, 9, T};
        assertArrayEquals(result, CompactEncoder.binToNibbles(test));
    }
}
