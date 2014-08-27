package org.ethereum.util;

import static org.junit.Assert.*;

import org.ethereum.util.CompactEncoder;
import org.junit.Test;

public class CompactEncoderTest {

	private final static byte T = 16; // terminator
	
	@Test
	public void testCompactEncodeOddCompact() {
		byte[] test = new byte[] { 1, 2, 3, 4, 5 };
		byte[] expectedData = new byte[] { 0x11, 0x23, 0x45 };
		assertArrayEquals("odd compact encode fail", expectedData, CompactEncoder.packNibbles(test));
	}
	
	@Test
	public void testCompactEncodeEvenCompact() {
		byte[] test = new byte[] { 0, 1, 2, 3, 4, 5 };
		byte[] expectedData = new byte[] { 0x00, 0x01, 0x23, 0x45 };
		assertArrayEquals("even compact encode fail", expectedData, CompactEncoder.packNibbles(test));
	}

	@Test
	public void testCompactEncodeEvenTerminated() {
		byte[] test = new byte[] { 0, 15, 1, 12, 11, 8, T };
		byte[] expectedData = new byte[] { 0x20, 0x0f, 0x1c, (byte) 0xb8 };
		assertArrayEquals("even terminated compact encode fail", expectedData, CompactEncoder.packNibbles(test));
	}

	@Test
	public void testCompactEncodeOddTerminated() {		
		byte[] test = new byte[] { 15, 1, 12, 11, 8, T };
		byte[] expectedData = new byte[] { 0x3f, 0x1c, (byte) 0xb8 };
		assertArrayEquals("odd terminated compact encode fail", expectedData, CompactEncoder.packNibbles(test));
	}

	@Test
	public void testCompactDecodeOddCompact() {
		byte[] test = new byte[] { 0x11, 0x23, 0x45 };
		byte[] expected = new byte[] {1, 2, 3, 4, 5};
		assertArrayEquals("odd compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
	}

	@Test
	public void testCompactDecodeEvenCompact() {
		byte[] test = new byte[] { 0x00, 0x01, 0x23, 0x45 };
		byte[] expected = new byte[] {0, 1, 2, 3, 4, 5};
		assertArrayEquals("even compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
	}

	@Test
	public void testCompactDecodeEvenTerminated() {
		byte[] test = new byte[] { 0x20, 0x0f, 0x1c, (byte) 0xb8 };
		byte[] expected = new byte[] {0, 15, 1, 12, 11, 8, T};
		assertArrayEquals("even terminated compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
	}

	@Test
	public void testCompactDecodeOddTerminated() {
		byte[] test = new byte[] { 0x3f, 0x1c, (byte) 0xb8 };
		byte[] expected = new byte[] {15, 1, 12, 11, 8, T};
		assertArrayEquals("odd terminated compact decode fail", expected, CompactEncoder.unpackToNibbles(test));
	}

	@Test
	public void testCompactHexEncode_1() {
		byte[] test = "stallion".getBytes();
		byte[] result = new byte[] { 7, 3, 7, 4, 6, 1, 6, 12, 6, 12, 6, 9, 6, 15, 6, 14, T };
		assertArrayEquals(result, CompactEncoder.binToNibbles(test));
	}

    @Test
    public void testCompactHexEncode_2() {
        byte[] test = "verb".getBytes();
        byte[] result = new byte[] {  7, 6, 6, 5, 7, 2, 6, 2, T };
        assertArrayEquals(result, CompactEncoder.binToNibbles(test));
    }

    @Test
    public void testCompactHexEncode_3() {
        byte[] test = "puppy".getBytes();
        byte[] result = new byte[] {  7, 0, 7, 5, 7, 0, 7, 0, 7, 9, T };
        assertArrayEquals(result, CompactEncoder.binToNibbles(test));
    }


    @Test
    public void testNiceNiblesOutput_1(){
        byte[] test = {7, 0, 7, 5, 7, 0, 7, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x05\\x07\\x00\\x07\\x00\\x07\\x09";

        assertEquals(result, CompactEncoder.nibblesToPrettyString(test));
    }

    @Test
    public void testNiceNiblesOutput_2(){
        byte[] test = {7, 0, 7, 0xf, 7, 0, 0xa, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x0f\\x07\\x00\\x0a\\x00\\x07\\x09";

        assertEquals(result, CompactEncoder.nibblesToPrettyString(test));
    }

}
