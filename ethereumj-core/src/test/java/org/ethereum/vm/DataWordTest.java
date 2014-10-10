package org.ethereum.vm;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class DataWordTest {

	@Test
	public void testAddPerformance() {
		boolean enabled = false;
		
		if(enabled) {
			byte[] one = new byte[] { 0x01, 0x31, 0x54, 0x41, 0x01, 0x31, 0x54,
					0x41, 0x01, 0x31, 0x54, 0x41, 0x01, 0x31, 0x54, 0x41, 0x01,
					0x31, 0x54, 0x41, 0x01, 0x31, 0x54, 0x41, 0x01, 0x31, 0x54,
					0x41, 0x01, 0x31, 0x54, 0x41 }; // Random value
			
			int ITERATIONS = 10000000;
			
			long now1 = System.currentTimeMillis();
			for (int i = 0; i < ITERATIONS; i++) {
				DataWord x = new DataWord(one);
				x.add(x);
			}
			System.out.println("Add1: " + (System.currentTimeMillis() - now1) + "ms");
				
			long now2 = System.currentTimeMillis();
			for (int i = 0; i < ITERATIONS; i++) {
				DataWord x = new DataWord(one);
				x.add2(x);
			}
			System.out.println("Add2: " + (System.currentTimeMillis() - now2) + "ms");
		} else {
			System.out.println("ADD performance test is disabled.");
		}
	}

	@Test
	public void testAdd2() {
		byte[] two = new byte[32];
		two[31] = (byte) 0xff; // 0x000000000000000000000000000000000000000000000000000000000000ff
		
		DataWord x = new DataWord(two);
		x.add(new DataWord(two));
		System.out.println(Hex.toHexString(x.getData()));
		
		DataWord y = new DataWord(two);
		y.add2(new DataWord(two));
		System.out.println(Hex.toHexString(y.getData()));
	}
	
	@Test
	public void testAdd3() {
		byte[] three = new byte[32];
		for (int i = 0; i < three.length; i++) {
			three[i] = (byte) 0xff;
		}
		
		DataWord x = new DataWord(three);
		x.add(new DataWord(three));
		assertEquals(32, x.getData().length);
		System.out.println(Hex.toHexString(x.getData()));

		// FAIL
//		DataWord y = new DataWord(three);
//		y.add2(new DataWord(three));
//		System.out.println(Hex.toHexString(y.getData()));
	}
	
	@Test
	public void testMod() {
		String expected = "000000000000000000000000000000000000000000000000000000000000001a";
		
		byte[] one = new byte[32];
		one[31] = 0x1e; // 0x000000000000000000000000000000000000000000000000000000000000001e
		
		byte[] two = new byte[32];
		for (int i = 0; i < two.length; i++) {
			two[i] = (byte) 0xff;
		}
		two[31] = 0x56; // 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff56
		
		DataWord x = new DataWord(one);// System.out.println(x.value());
		DataWord y = new DataWord(two);// System.out.println(y.value());
		y.mod(x);
		assertEquals(32, y.getData().length);
		assertEquals(expected, Hex.toHexString(y.getData()));
	}
	
	@Test
	public void testMul() {
		byte[] one = new byte[32];
		one[31] = 0x1; // 0x0000000000000000000000000000000000000000000000000000000000000001

		byte[] two = new byte[32];
		two[11] = 0x1; // 0x0000000000000000000000010000000000000000000000000000000000000000

		DataWord x = new DataWord(one);// System.out.println(x.value());
		DataWord y = new DataWord(two);// System.out.println(y.value());
		x.mul(y);
		assertEquals(32, y.getData().length);
		assertEquals("0000000000000000000000010000000000000000000000000000000000000000", Hex.toHexString(y.getData()));
	}
	
	@Test
	public void testMulOverflow() {
		
		byte[] one = new byte[32];
		one[30] = 0x1; // 0x0000000000000000000000000000000000000000000000000000000000000100

		byte[] two = new byte[32];
		two[0] = 0x1; //  0x1000000000000000000000000000000000000000000000000000000000000000

		DataWord x = new DataWord(one);// System.out.println(x.value());
		DataWord y = new DataWord(two);// System.out.println(y.value());
		x.mul(y);
		assertEquals(32, y.getData().length);
		assertEquals("0100000000000000000000000000000000000000000000000000000000000000", Hex.toHexString(y.getData()));
	}
	
	@Test
	public void testPow() {
		
		BigInteger x = BigInteger.valueOf(Integer.MAX_VALUE);
		BigInteger y = BigInteger.valueOf(1000);
		
		BigInteger result1 = x.modPow(x, y);
		BigInteger result2 = pow(x, y);
		System.out.println(result1);
		System.out.println(result2);
	}
	
	
	public static BigInteger pow(BigInteger x, BigInteger y) {
		if (y.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException();
		BigInteger z = x; // z will successively become x^2, x^4, x^8, x^16,
							// x^32...
		BigInteger result = BigInteger.ONE;
		byte[] bytes = y.toByteArray();
		for (int i = bytes.length - 1; i >= 0; i--) {
			byte bits = bytes[i];
			for (int j = 0; j < 8; j++) {
				if ((bits & 1) != 0)
					result = result.multiply(z);
				// short cut out if there are no more bits to handle:
				if ((bits >>= 1) == 0 && i == 0)
					return result;
				z = z.multiply(z);
			}
		}
		return result;
	}
	
}
