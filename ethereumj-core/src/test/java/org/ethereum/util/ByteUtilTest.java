package org.ethereum.util;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

public class ByteUtilTest {

	@Test
	public void testAppendByte() {
		byte[] bytes = "tes".getBytes();
		byte b = 0x74; 
		assertArrayEquals("test".getBytes(), ByteUtil.appendByte(bytes, b));
	}

	@Test
	public void testBigIntegerToBytes() {
		byte[] expecteds = new byte[]{(byte) 0xff, (byte) 0xec, 0x78};
		BigInteger b = BigInteger.valueOf(16772216);
		byte[] actuals = ByteUtil.bigIntegerToBytes(b);
		assertArrayEquals(expecteds, actuals);
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
		byte[] test = new byte[] { 0x0f, 0x10, 0x43 };
		byte[] expected = new byte[] { 0x00, 0x00, 0x00, 0x03 };
		assertArrayEquals(expected, ByteUtil.calcPacketLength(test));
	}

	@Test
	public void testByteArrayToInt() {
		assertEquals(0, ByteUtil.byteArrayToInt(null));
		assertEquals(0, ByteUtil.byteArrayToInt(new byte[0]));
	
//		byte[] x = new byte[] { 5,1,7,0,8 };
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < 100000000; i++) {
//			 ByteArray.read32bit(x, 0);
//		}
//		long end = System.currentTimeMillis();
//		System.out.println(end - start + "ms");
//
//		long start1 = System.currentTimeMillis();
//		for (int i = 0; i < 100000000; i++) {
//			new BigInteger(1, x).intValue();
//		}
//		long end1 = System.currentTimeMillis();
//		System.out.println(end1 - start1 + "ms");

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
		byte[] test1 = new byte[] { 0x00, 0x01 };
		byte[] test2 = new byte[] { 0x00, 0x00, 0x01 };
		byte[] expected = new byte[] { 0x01 };
		assertArrayEquals(expected, ByteUtil.stripLeadingZeroes(test1));
		assertArrayEquals(expected, ByteUtil.stripLeadingZeroes(test2));
	}
		
	@Test
	public void testMatchingNibbleLength1() {
		// a larger than b
		byte[] a = new byte[] { 0x00, 0x01 };
		byte[] b = new byte[] { 0x00 };
		int result = ByteUtil.matchingNibbleLength(a, b);
		assertEquals(1, result);
	}
	@Test
	public void testMatchingNibbleLength2() {
		// b larger than a
		byte[] a  = new byte[] { 0x00 };
		byte[] b  = new byte[] { 0x00, 0x01 };
		int result = ByteUtil.matchingNibbleLength(a, b);
		assertEquals(1, result);
	}
	
	@Test
	public void testMatchingNibbleLength3() {
		// a and b the same length equal
		byte[] a = new byte[] { 0x00 };
		byte[] b = new byte[] { 0x00 };
		int result = ByteUtil.matchingNibbleLength(a, b);
		assertEquals(1, result);
	}
	
	@Test
	public void testMatchingNibbleLength4() {
		// a and b the same length not equal
		byte[] a = new byte[] { 0x01 };
		byte[] b = new byte[] { 0x00 };
		int result = ByteUtil.matchingNibbleLength(a, b);
		assertEquals(0, result);
	}
	
    @Test
    public void testNiceNiblesOutput_1(){
        byte[] test = {7, 0, 7, 5, 7, 0, 7, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x05\\x07\\x00\\x07\\x00\\x07\\x09";
        assertEquals(result, ByteUtil.nibblesToPrettyString(test));
    }

    @Test
    public void testNiceNiblesOutput_2(){
        byte[] test = {7, 0, 7, 0xf, 7, 0, 0xa, 0, 7, 9};
        String result = "\\x07\\x00\\x07\\x0f\\x07\\x00\\x0a\\x00\\x07\\x09";
        assertEquals(result, ByteUtil.nibblesToPrettyString(test));
    }
	
	@Test(expected=NullPointerException.class)
	public void testMatchingNibbleLength5() {
		// a == null
		byte[] a = null;
		byte[] b = new byte[] { 0x00 };
		ByteUtil.matchingNibbleLength(a, b);
	}
	
	@Test(expected=NullPointerException.class)
	public void testMatchingNibbleLength6() {
		// b == null
		byte[] a = new byte[] { 0x00 };
		byte[] b = null;
		ByteUtil.matchingNibbleLength(a, b);
	}
	
	@Test
	public void testMatchingNibbleLength7() {
		// a or b is empty
		byte[] a = new byte[0];
		byte[] b = new byte[] { 0x00 };
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
		
		if(testEnabled) {
			byte[] counter1 = new byte[4];
			byte[] max = ByteBuffer.allocate(4).putInt(Integer.MAX_VALUE).array();
			long start1 = System.currentTimeMillis();
			while(ByteUtil.increment(counter1)) {
				if(FastByteComparisons.compareTo(counter1, 0, 4, max, 0, 4) == 0) {
					break;
				}
			}
			System.out.println(System.currentTimeMillis() - start1 + "ms to reach: " + Hex.toHexString(counter1));
			
			BigInteger counter2 = BigInteger.ZERO;
			long start2 = System.currentTimeMillis();
			while(true) {
				if(counter2.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 0) {
					break;
				}
				counter2 = counter2.add(BigInteger.ONE);
			}
			System.out.println(System.currentTimeMillis() - start2 + "ms to reach: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(4, counter2)));
		}
	}
}
