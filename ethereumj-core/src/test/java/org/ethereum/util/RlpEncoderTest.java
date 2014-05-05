package org.ethereum.util;

import static org.ethereum.util.RlpEncoder.toInt;
import static org.ethereum.util.RlpTestData.expected14;
import static org.ethereum.util.RlpTestData.expected16;
import static org.ethereum.util.RlpTestData.result01;
import static org.ethereum.util.RlpTestData.result02;
import static org.ethereum.util.RlpTestData.result03;
import static org.ethereum.util.RlpTestData.result04;
import static org.ethereum.util.RlpTestData.result05;
import static org.ethereum.util.RlpTestData.result06;
import static org.ethereum.util.RlpTestData.result07;
import static org.ethereum.util.RlpTestData.result08;
import static org.ethereum.util.RlpTestData.result09;
import static org.ethereum.util.RlpTestData.result10;
import static org.ethereum.util.RlpTestData.result11;
import static org.ethereum.util.RlpTestData.result12;
import static org.ethereum.util.RlpTestData.result13;
import static org.ethereum.util.RlpTestData.result14;
import static org.ethereum.util.RlpTestData.result15;
import static org.ethereum.util.RlpTestData.result16;
import static org.ethereum.util.RlpTestData.test01;
import static org.ethereum.util.RlpTestData.test02;
import static org.ethereum.util.RlpTestData.test03;
import static org.ethereum.util.RlpTestData.test04;
import static org.ethereum.util.RlpTestData.test05;
import static org.ethereum.util.RlpTestData.test06;
import static org.ethereum.util.RlpTestData.test07;
import static org.ethereum.util.RlpTestData.test08;
import static org.ethereum.util.RlpTestData.test09;
import static org.ethereum.util.RlpTestData.test10;
import static org.ethereum.util.RlpTestData.test11;
import static org.ethereum.util.RlpTestData.test12;
import static org.ethereum.util.RlpTestData.test13;
import static org.ethereum.util.RlpTestData.test14;
import static org.ethereum.util.RlpTestData.test15;
import static org.ethereum.util.RlpTestData.test16;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import com.cedarsoftware.util.DeepEquals;

public class RlpEncoderTest {

	/************************************
	 * Test data from: https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-RLP
	 * 
	 * Using assertEquals(String, String) instead of assertArrayEquals to see the actual content when the test fails.
	 */
	@Test(expected = RuntimeException.class)
	public void testEncodeNull() {
		RlpEncoder.encode(null);
	}
	
	@Test
	public void testEncodeEmptyString() {
		String test = "";
		String expected = "80";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(test, bytesToAscii(decodeResult));
	}

	@Test
	public void testEncodeShortString() {
		String test = "dog";
		String expected = "83646f67";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(test, bytesToAscii(decodeResult));
	}
	
	@Test
	public void testEncodeSingleCharacter() {
		String test = "d";
		String expected = "64";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(test, bytesToAscii(decodeResult));
	}

	@Test
	public void testEncodeLongString() {
		String test = "Lorem ipsum dolor sit amet, consectetur adipisicing elit"; // length = 56
		String expected = "b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(test, bytesToAscii(decodeResult));
	}

	@Test
	public void testEncodeZero() {
		Integer test = new Integer(0);
		String expected = "80";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		int result = toInt(decodeResult);
		assertEquals(test, Integer.valueOf(result));
	}
		
	@Test
	public void testEncodeSmallInteger() {
		Integer test = new Integer(15);
		String expected = "0f";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		int result = toInt(decodeResult);
		assertEquals(test, Integer.valueOf(result));
	}
	
	@Test
	public void testEncodeMediumInteger() {
		Integer test = new Integer(1000);
		String expected = "8203e8";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		int result = toInt(decodeResult);
		assertEquals(test, Integer.valueOf(result));
		
		test = new Integer(1024);
		expected = "820400";
		encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		result = toInt(decodeResult);
		assertEquals(test, Integer.valueOf(result));
	}
	
	@Test
	public void testEncodeBigInteger() {
		BigInteger test = new BigInteger("100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", 16);
		String expected = "a0100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		byte[] decodeResult = (byte[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(test, new BigInteger(decodeResult));
	}

	@Test
	public void TestEncodeEmptyList() {
		String[] test = new String[0];
		String expected = "c0";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertTrue(decodeResult.length == 0);
	}

	@Test
	public void testEncodeShortStringList() {
		String[] test = new String[] { "cat", "dog" };
		String expected = "c88363617483646f67";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals("cat", bytesToAscii((byte[]) decodeResult[0]));
		assertEquals("dog", bytesToAscii((byte[]) decodeResult[1]));
	
		test = new String[] { "dog", "god", "cat" };
		expected = "cc83646f6783676f6483636174";
		encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals("dog", bytesToAscii((byte[]) decodeResult[0]));
		assertEquals("god", bytesToAscii((byte[]) decodeResult[1]));
		assertEquals("cat", bytesToAscii((byte[]) decodeResult[2]));
	}
	
	@Test
	public void testEncodeLongStringList() {
		String element1 = "cat";
		String element2 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit";
		String[] test = new String[] { element1, element2 };
		String expected = "f83e83636174b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974";
		byte[] encoderesult = (byte[]) RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(element1, bytesToAscii((byte[]) decodeResult[0]));
		assertEquals(element2, bytesToAscii((byte[]) decodeResult[1]));
	}
	
	//multilist:
	//in: [ 1, ["cat"], "dog", [ 2 ] ], 
	//out: "cc01c48363617483646f67c102"
	//in: [ [ ["cat"], ["dog"] ], [ [1] [2] ], [] ], 
	//out: "cdc88363617483646f67c20102c0"
	@Test
	public void testEncodeMultiList() {
		Object[] test = new Object[] { 1, new Object[] { "cat" }, "dog", new Object[] { 2 } };
		String expected = "cc01c48363617483646f67c102";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals(1, toInt( (byte[]) decodeResult[0] ));
		assertEquals("cat", bytesToAscii( ((byte[]) ((Object[]) decodeResult[1])[0] )));
		assertEquals("dog", bytesToAscii( (byte[]) decodeResult[2]));
		assertEquals(2, toInt( ((byte[]) ((Object[]) decodeResult[3])[0] )));
		
		test = new Object[] { new Object[] { "cat", "dog" }, new Object[] { 1, 2 }, new Object[] { } };
		expected = "cdc88363617483646f67c20102c0";
		encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertEquals("cat", bytesToAscii( ((byte[]) ((Object[]) decodeResult[0])[0] )));
		assertEquals("dog", bytesToAscii( ((byte[]) ((Object[]) decodeResult[0])[1] )));
		assertEquals(1, toInt( ((byte[]) ((Object[]) decodeResult[1])[0] )));
		assertEquals(2, toInt( ((byte[]) ((Object[]) decodeResult[1])[1] )));
		assertTrue( ( ((Object[]) decodeResult[2]).length == 0 ));
	}
	
	@Test
	public void testEncodeEmptyListOfList() {
		// list = [ [ [], [] ], [] ],
		Object[] test = new Object[] { new Object[] { new Object[] {}, new Object[] {} }, new Object[] {} };
		String expected = "c4c2c0c0c0";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertTrue( decodeResult.length == 2 );
		assertTrue( ( (Object[]) (decodeResult[0] ) ).length == 2);
		assertTrue( ( (Object[]) (decodeResult[1] ) ).length == 0);
		assertTrue( ( (Object[]) ( (Object[]) ( decodeResult[0] ) )[0]).length == 0);
		assertTrue( ( (Object[]) ( (Object[]) ( decodeResult[0] ) )[1]).length == 0);
	}
	
	//The set theoretical representation of two
	@Test
	public void testEncodeRepOfTwoListOfList() {
		//list: [ [], [[]], [ [], [[]] ] ]
		Object[] test = new Object[] { new Object[] { }, new Object[] { new Object[] {} }, new Object[] { new Object[] {}, new Object[] { new Object[] { } } } };
		String expected = "c7c0c1c0c3c0c1c0";
		byte[] encoderesult = RlpEncoder.encode(test);
		assertEquals(expected, asHex(encoderesult));
		
		Object[] decodeResult = (Object[]) RlpEncoder.decode(encoderesult, 0).getDecoded();
		assertTrue( decodeResult.length == 3 );
		assertTrue( ( (Object[]) (decodeResult[0]) ).length == 0);
		assertTrue( ( (Object[]) (decodeResult[1]) ).length == 1);
		assertTrue( ( (Object[]) (decodeResult[2]) ).length == 2);
		assertTrue( ( (Object[]) ( (Object[]) (decodeResult[1]) )[0]).length == 0);
		assertTrue( ( (Object[]) ( (Object[]) (decodeResult[2]) )[0]).length == 0);
		assertTrue( ( (Object[]) ( (Object[]) (decodeResult[2]) )[1]).length == 1);
		assertTrue( ( (Object[]) ( (Object[]) ( (Object[]) (decodeResult[2]) )[1] )[0]).length == 0);		
	}
	
	@Test
	public void testRlpEncode() {
	
		assertEquals(result01, asHex(RlpEncoder.encode(test01)));
		assertEquals(result02, asHex(RlpEncoder.encode(test02)));
		assertEquals(result03, asHex(RlpEncoder.encode(test03)));
		assertEquals(result04, asHex(RlpEncoder.encode(test04)));
		assertEquals(result05, asHex(RlpEncoder.encode(test05)));
		assertEquals(result06, asHex(RlpEncoder.encode(test06)));
		assertEquals(result07, asHex(RlpEncoder.encode(test07)));
		assertEquals(result08, asHex(RlpEncoder.encode(test08)));
		assertEquals(result09, asHex(RlpEncoder.encode(test09)));
		assertEquals(result10, asHex(RlpEncoder.encode(test10)));
		assertEquals(result11, asHex(RlpEncoder.encode(test11)));
		assertEquals(result12, asHex(RlpEncoder.encode(test12)));
		assertEquals(result13, asHex(RlpEncoder.encode(test13)));
		assertEquals(result14, asHex(RlpEncoder.encode(test14)));
		assertEquals(result15, asHex(RlpEncoder.encode(test15)));
		assertEquals(result16, asHex(RlpEncoder.encode(test16)));
	}

	@Test
	public void testRlpDecode() {
		int pos = 0;
		byte[] decodedByte;
		byte[] decodedData;
		Object[] decodedList; 
		
		decodedByte = (byte[]) RlpEncoder.decode(fromHex(result01), pos).getDecoded();
		assertEquals(test01, toInt(decodedByte));
		
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result02), pos).getDecoded();
		assertEquals(test02, bytesToAscii(decodedData));

		decodedData = (byte[]) RlpEncoder.decode(fromHex(result03), pos).getDecoded();
		assertEquals(test03, bytesToAscii(decodedData));
		
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result04), pos).getDecoded();
		assertEquals(test04, bytesToAscii(decodedData));
		
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result05), pos).getDecoded();
		assertEquals(test05, bytesToAscii(decodedData));
		
		decodedList = (Object[]) RlpEncoder.decode(fromHex(result06), pos).getDecoded();
		assertEquals(test06[0], bytesToAscii((byte[]) decodedList[0]));
		assertEquals(test06[1], bytesToAscii((byte[]) decodedList[1]));
		
		decodedList = (Object[]) RlpEncoder.decode(fromHex(result07), pos).getDecoded();
		assertEquals(test07[0], bytesToAscii((byte[]) decodedList[0]));
		assertEquals(test07[1], bytesToAscii((byte[]) decodedList[1]));
		assertEquals(test07[2], bytesToAscii((byte[]) decodedList[2]));
		
		// 1
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result08), pos).getDecoded();
		assertEquals(test08, toInt(decodedData));

		// 10
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result09), pos).getDecoded();
		assertEquals(test09, toInt(decodedData));

		// 100
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result10), pos).getDecoded();
		assertEquals(test10, toInt(decodedData));

		// 1000 
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result11), pos).getDecoded();
		assertEquals(test11, toInt(decodedData));
		
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result12), pos).getDecoded();
		assertTrue(test12.compareTo(new BigInteger(decodedData)) == 0);
		
		decodedData = (byte[]) RlpEncoder.decode(fromHex(result13), pos).getDecoded();
		assertTrue(test13.compareTo(new BigInteger(decodedData)) == 0);
		
		// Need to test with different expected value, because decoding doesn't recognize types
		Object testObject1 = RlpEncoder.decode(fromHex(result14), pos).getDecoded();
		assertTrue(DeepEquals.deepEquals(expected14, testObject1));
		
		Object testObject2 = RlpEncoder.decode(fromHex(result15), pos).getDecoded();
		assertTrue(DeepEquals.deepEquals(test15, testObject2));
		
		// Need to test with different expected value, because decoding doesn't recognize types
		Object testObject3 = RlpEncoder.decode(fromHex(result16), pos).getDecoded();
		assertTrue(DeepEquals.deepEquals(expected16, testObject3));
	}

	@Test
	public void testEncodeLength() {
		int length;
		int offset;
		byte[] encodedLength;
		String expected;

		// length < 56
		length = 1; offset = 128;
		encodedLength = RlpEncoder.encodeLength(length, offset);
		expected = "81"; 
		assertEquals(expected, asHex(encodedLength));

		// 56 > length < 2^64
		length = 56; offset = 192;
		encodedLength = RlpEncoder.encodeLength(length, offset);
		expected = "f838";
		assertEquals(expected, asHex(encodedLength));

		// length > 2^64
		// TODO: Fix this test - when casting double to int, information gets lost since 'int' is max (2^31)-1
		double maxLength = Math.pow(256, 8); offset = 192;
		try {
			encodedLength = RlpEncoder.encodeLength( (int) maxLength, offset);
			System.out.println("length: " + length + ", offset: " + offset + ", encoded: " + Arrays.toString(encodedLength));
			fail("Expecting RuntimeException: 'Input too long'");
		} catch(RuntimeException e) {
			// Success!
		}
	}
	
	// Code from: http://stackoverflow.com/a/9855338/459349
	protected final static char[] hexArray = "0123456789abcdef".toCharArray();
	private static String asHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	// Code from: http://stackoverflow.com/a/4785776/459349
    private String bytesToAscii(byte[] b) {
    	String hex = asHex(b);
	    StringBuilder output = new StringBuilder();
	    for (int i = 0; i < hex.length(); i+=2) {
	        String str = hex.substring(i, i+2);
	        output.append((char)Integer.parseInt(str, 16));
	    }
	    return output.toString();
    }

	// Code from: http://stackoverflow.com/a/140861/459349
	public static byte[] fromHex(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
