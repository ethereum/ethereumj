package org.ethereum.vm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class DataWordTest {

	@Test
	public void testAddPerformance() {
		boolean enabled = true;
		
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
		System.out.println(Hex.toHexString(x.data));
		
		DataWord y = new DataWord(two);
		y.add2(new DataWord(two));
		System.out.println(Hex.toHexString(y.data));
	}
	
	@Test
	public void testAdd3() {
		byte[] three = new byte[32];
		for (int i = 0; i < three.length; i++) {
			three[i] = (byte) 0xff;
		}
		
		DataWord x = new DataWord(three);
		x.add(new DataWord(three));
		assertEquals(32, x.data.length);
		System.out.println(Hex.toHexString(x.data));

		// FAIL
//		DataWord y = new DataWord(three);
//		y.add2(new DataWord(three));
//		System.out.println(Hex.toHexString(y.data));
	}
}
