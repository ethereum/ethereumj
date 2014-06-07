package org.ethereum.util;

import java.io.ByteArrayOutputStream;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.copyOf;

import static org.ethereum.util.ByteUtil.appendByte;
import static org.spongycastle.util.Arrays.concatenate;
import static org.spongycastle.util.encoders.Hex.toHexString;

/** 
 * Compact encoding of hex sequence with optional terminator
 * 
 * The traditional compact way of encoding a hex string is to convert it into binary 
 * - that is, a string like 0f1248 would become three bytes 15, 18, 72. However, 
 * this approach has one slight problem: what if the length of the hex string is odd? 
 * In that case, there is no way to distinguish between, say, 0f1248 and f1248. 
 * 
 * Additionally, our application in the Merkle Patricia tree requires the additional feature 
 * that a hex string can also have a special "terminator symbol" at the end (denoted by the 'T'). 
 * A terminator symbol can occur only once, and only at the end. 
 * 
 * An alternative way of thinking about this to not think of there being a terminator symbol, 
 * but instead treat bit specifying the existence of the terminator symbol as a bit specifying 
 * that the given node encodes a final node, where the value is an actual value, rather than
 * the hash of yet another node.
 * 
 * To solve both of these issues, we force the first nibble of the final byte-stream to encode 
 * two flags, specifying oddness of length (ignoring the 'T' symbol) and terminator status; 
 * these are placed, respectively, into the two lowest significant bits of the first nibble. 
 * In the case of an even-length hex string, we must introduce a second nibble (of value zero)
 * to ensure the hex-string is even in length and thus is representable by a whole number of bytes. 
 *  
 * Examples:
 * > [ 1, 2, 3, 4, 5 ]
 * '\x11\x23\x45'
 * > [ 0, 1, 2, 3, 4, 5 ]
 * '\x00\x01\x23\x45'
 * > [ 0, 15, 1, 12, 11, 8, T ]
 * '\x20\x0f\x1c\xb8'
 * > [ 15, 1, 12, 11, 8, T ]
 * '\x3f\x1c\xb8'
 *
 */
public class CompactEncoder {

	private final static byte TERMINATOR = 16;
	private final static String hexBase = "0123456789abcdef";
	
	/**
	 * Pack nibbles to binary
	 *
	 * @param nibbles sequence. may have a terminator
	 * @return hex-encoded byte array
	 */
	public static byte[] packNibbles(byte[] nibbles) {
		int terminator = 0;
		
		if (nibbles[nibbles.length-1] == TERMINATOR) {
			terminator = 1;
			nibbles = copyOf(nibbles, nibbles.length-1);
		}
		int oddlen = nibbles.length % 2;
		int flag = 2*terminator + oddlen;
		if (oddlen != 0) {
			byte[] flags = new byte[] { (byte) flag};
			nibbles = concatenate(flags, nibbles);
		} else {
			byte[] flags = new byte[] { (byte) flag, 0};
			nibbles = concatenate(flags, nibbles);
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < nibbles.length; i += 2) {
			buffer.write( 16*nibbles[i] + nibbles[i+1] );
		}
		return buffer.toByteArray();
	}

	/**
	 * Unpack a binary string to its nibbles equivalent
	 * 
	 * @param str of binary data
	 * @return array of nibbles in byte-format  
	 */
	public static byte[] unpackToNibbles(byte[] str) {
		byte[] base = binToNibbles(str);
		base = copyOf(base, base.length-1);
		if (base[0] >= 2) {
			base = appendByte(base, TERMINATOR);
		}
		if (base[0]%2 == 1) {
			base = copyOfRange(base, 1, base.length);
		} else {
			base = copyOfRange(base, 2, base.length);
		}
		return base;
	}

	/**
	 * Transforms a binary array to hexadecimal format + terminator
	 *
	 * @return array with each individual nibble adding a terminator at the end 
	 */	
	public static byte[] binToNibbles(byte[] str) {
		byte[] hexSlice = new byte[0];
		String hexEncoded = toHexString(str);
		for (char value : hexEncoded.toCharArray()) {
			hexSlice = appendByte(hexSlice, (byte) hexBase.indexOf(value));
		}
		hexSlice = appendByte(hexSlice, TERMINATOR);

		return hexSlice;
	}
}
