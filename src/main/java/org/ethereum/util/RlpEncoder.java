package org.ethereum.util;

import java.math.BigInteger;
import java.util.ArrayList;

import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;

import java.util.List;

/**
 * Recursive Length Prefix (RLP) encoding. 
 * 
 * The purpose of RLP is to encode arbitrarily nested arrays of binary data, and
 * RLP is the main encoding method used to serialize objects in Ethereum. The
 * only purpose of RLP is to encode structure; encoding specific atomic data
 * types (eg. strings, ints, floats) is left up to higher-order protocols; in
 * Ethereum the standard is that integers are represented in big endian binary
 * form. If one wishes to use RLP to encode a dictionary, the two suggested
 * canonical forms are to either use [[k1,v1],[k2,v2]...] with keys in
 * lexicographic order or to use the higher-level Patricia Tree encoding as
 * Ethereum does.
 * 
 * The RLP encoding function takes in an item. An item is defined as follows:
 * 
 * - A string (ie. byte array) is an item - A list of items is an item
 * 
 * For example, an empty string is an item, as is the string containing the word
 * "cat", a list containing any number of strings, as well as more complex data
 * structures like ["cat",["puppy","cow"],"horse",[[]],"pig",[""],"sheep"]. Note
 * that in the context of the rest of this article, "string" will be used as a
 * synonym for "a certain number of bytes of binary data"; no special encodings
 * are used and no knowledge about the content of the strings is implied.
 * 
 * See: https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-RLP
 */
public class RlpEncoder extends CompactEncoder {

	/** Allow for content up to size of 2^64 bytes **/
	private static double MAX_ITEM_LENGTH = Math.pow(256, 8);

	/**
	[5:30:35 PM] Vitalik Buterin: 56 bytes maximizes the benefit of both options
	[5:30:41 PM] Vitalik Buterin: if we went with 60
	[5:31:03 PM] Vitalik Buterin: then we would have only had 4 slots for long strings so RLP would not have been able to store objects above 4gb
	[5:31:08 PM] Vitalik Buterin: if we went with 48
	[5:31:18 PM] Vitalik Buterin: then RLP would be fine for 2^128 space, but that's way too much
	[5:31:32 PM] Vitalik Buterin: so 56 and 2^64 space seems like the right place to put the cutoff
	[5:31:44 PM] Vitalik Buterin: also, that's where Bitcoin's varint does the cutof
	**/
	private static int SIZE_THRESHOLD = 56;
	
	/** RLP encoding rules are defined as follows: */
	
	/*
	 * For a single byte whose value is in the [0x00, 0x7f] range, that byte is
	 * its own RLP encoding.
	 */

	/*
	 * If a string is 0-55 bytes long, the RLP encoding consists of a single
	 * byte with value 0x80 plus the length of the string followed by the
	 * string. The range of the first byte is thus [0x80, 0xb7].
	 */
	private static int offsetShortItem = 0x80;

	/*
	 * If a string is more than 55 bytes long, the RLP encoding consists of a
	 * single byte with value 0xb7 plus the length of the length of the string
	 * in binary form, followed by the length of the string, followed by the
	 * string. For example, a length-1024 string would be encoded as
	 * \xb9\x04\x00 followed by the string. The range of the first byte is thus
	 * [0xb8, 0xbf].
	 */
	private static int offsetLongItem = 0xb8;

	/*
	 * If the total payload of a list (i.e. the combined length of all its
	 * items) is 0-55 bytes long, the RLP encoding consists of a single byte
	 * with value 0xc0 plus the length of the list followed by the concatenation
	 * of the RLP encodings of the items. The range of the first byte is thus
	 * [0xc0, 0xf7].
	 */
	private static int offsetShortList = 0xc0;

	/*
	 * If the total payload of a list is more than 55 bytes long, the RLP
	 * encoding consists of a single byte with value 0xf7 plus the length of the
	 * length of the list in binary form, followed by the length of the list,
	 * followed by the concatenation of the RLP encodings of the items. The
	 * range of the first byte is thus [0xf8, 0xff].
	 */
	private static int offsetLongList = 0xf8;
	private static int maxPrefix = 0xff;

	public static byte[] encode(Object input) {
		Value val = new Value(input);
		if (val.isList()) {
			List<Object> inputArray = val.asList();
			if (inputArray.size() == 0) {
				return encodeLength(inputArray.size(), offsetShortList);
			}
			byte[] output = new byte[0];
			for (Object object : inputArray) {
				output = concatenate(output, encode(object));
			}
			byte[] prefix = encodeLength(output.length, offsetShortList);
			return concatenate(prefix, output);
		} else {
			byte[] inputAsHex = asHex(input); 
			if(inputAsHex.length == 1) {
				return inputAsHex;
			} else {
				byte[] firstByte = encodeLength(inputAsHex.length, offsetShortItem);
				return concatenate(firstByte, inputAsHex);
			}
		} 
	}
	
	public static DecodeResult decode(byte[] data, int pos) {
		if (data == null || data.length < 1) {
			return null;
		}
		
		int prefix = data[pos] & maxPrefix;
		if (prefix == offsetShortItem) {
			return new DecodeResult(pos+1, new byte[0]); // means no length or 0
		} else if (prefix < offsetShortItem) {
			return new DecodeResult(pos+1, new byte[] { data[pos] }); // byte is its own RLP encoding
		} else if (prefix < offsetLongItem){
			int len = prefix - offsetShortItem; // length of the encoded bytes
			return new DecodeResult(pos+1+len, copyOfRange(data, pos+1, pos+1+len));
		} else if (prefix < offsetShortList) {
			int lenlen = prefix - offsetLongItem + 1; // length of length the encoded bytes
			int lenbytes = toInt(copyOfRange(data, pos+1, pos+1+lenlen)); // length of encoded bytes
			return new DecodeResult(pos+1+lenlen+lenbytes, copyOfRange(data, pos+1+lenlen, pos+1+lenlen+lenbytes));
		} else if (prefix < offsetLongList) {
			int len = prefix - offsetShortList; // length of the encoded list
			int prevPos = pos; pos++;
			return decodeList(data, pos, prevPos, len);
		} else if (prefix < maxPrefix) {
			int lenlen = prefix - offsetLongList + 1; // length of length the encoded list
			int lenlist = toInt(copyOfRange(data, pos+1, pos+1+lenlen)); // length of encoded bytes
		    pos = pos + lenlen + 1;
		    int prevPos = lenlist;
		    return decodeList(data, pos, prevPos, lenlist);
		} else {
			throw new RuntimeException("Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
		}
	}
	
	/** Integer limitation goes up to 2^31-1 so length can never be bigger than MAX_ITEM_LENGTH */
	public static byte[] encodeLength(int length, int offset) {
		if (length < SIZE_THRESHOLD) {
			byte firstByte = (byte) (length + offset);
			return new byte[] { firstByte };
		} else if (length < MAX_ITEM_LENGTH) {
			byte[] binaryLength = BigInteger.valueOf(length).toByteArray();
			byte firstByte = (byte) (binaryLength.length + offset + SIZE_THRESHOLD - 1 );
			return concatenate(new byte[] { firstByte }, binaryLength);
		} else {
			throw new RuntimeException("Input too long");
		}
	}
	
	private static DecodeResult decodeList(byte[] data, int pos, int prevPos, int len) {
		List<Object> slice = new ArrayList<Object>();
		for (int i = 0; i < len;) {
			// Get the next item in the data list and append it
			DecodeResult result = decode(data, pos);
			slice.add(result.getDecoded());
			// Increment pos by the amount bytes in the previous read
			prevPos = result.getPos();
	        i += (prevPos - pos);
	        pos = prevPos;
		}
		return new DecodeResult(pos, slice.toArray());
	}
	
	public static byte[] asHex(Object input) {
		if (input instanceof byte[]) {
			return (byte[]) input;
		} else if (input instanceof String) {
			String inputString = (String) input;
			return inputString.getBytes();
		} else if(input instanceof Integer) {
			Integer inputInt = (Integer) input;	
			return (inputInt == 0) ? new byte[0] : BigInteger.valueOf(inputInt.longValue()).toByteArray();
		} else if(input instanceof BigInteger) {
			BigInteger inputBigInt = (BigInteger) input;
			return (inputBigInt == BigInteger.ZERO) ? new byte[0] : inputBigInt.toByteArray();
		} else if (input instanceof Value) {
			Value val = (Value) input;
			return asHex(val.asObj());
		}
		throw new RuntimeException("Unsupported type: Only accepting String, Integer and BigInteger for now");
	}
	
	/**
	 * Cast hex encoded value from byte[] to int
	 * 
	 * Limited to Integer.MAX_VALUE: 2^32-1
	 * 
	 * @param b array contains the hex values
	 * @return int value of all hex values together. 
	 */
	public static int toInt(byte[] b) {
		if (b == null || b.length == 0) {
			return 0;
		}
		return new BigInteger(b).intValue();
	}
}
