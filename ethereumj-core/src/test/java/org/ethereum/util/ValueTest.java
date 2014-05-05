package org.ethereum.util;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

public class ValueTest {

	@Test
	public void testCmp() {
		Value val1 = new Value("hello");
		Value val2 = new Value("world");
		
		assertFalse("Expected values not to be equal", val1.cmp(val2));

		Value val3 = new Value("hello");
		Value val4 = new Value("hello");
		
		assertTrue("Expected values to be equal", val3.cmp(val4));
	}

	@Test
	public void testTypes() {
		Value str = new Value("str");
		assertEquals(str.asString(), "str");

		Value num = new Value(1);
		assertEquals(num.asInt(), 1);

		Value inter = new Value(new Object[]{1});
		Object[] interExp = new Object[]{1};
		assertTrue(new Value(inter.asObj()).cmp(new Value(interExp)));

		Value byt = new Value(new byte[]{1, 2, 3, 4});
		byte[] bytExp = new byte[]{1, 2, 3, 4};
		assertTrue(Arrays.equals(byt.asBytes(), bytExp));

		Value bigInt = new Value(BigInteger.valueOf(10));
		BigInteger bigExp = BigInteger.valueOf(10);
		assertEquals(bigInt.asBigInt(), bigExp);
	}

	
}
