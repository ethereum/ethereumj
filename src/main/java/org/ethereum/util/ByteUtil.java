package org.ethereum.util;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLongs;

public class ByteUtil {

    /**
     * Creates a copy of bytes and appends b to the end of it
     */
    public static byte[] appendByte(byte[] bytes, byte b) {
        byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
        result[result.length - 1] = b;
        return result;
    }

    /**
     * The regular {@link java.math.BigInteger#toByteArray()} method isn't quite what we often need: it appends a
     * leading zero to indicate that the number is positive and may need padding.
     *
     * @param b the integer to format into a byte array
     * @param numBytes the desired size of the resulting byte array
     * @return numBytes byte long array.
     */
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null) {
            return null;
        }
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;        
    }
    
    /**
     * Calculate packet length
     * @param msg
     * @return byte-array with 4 elements
     */
    public static byte[] calcPacketLength(byte[] msg){
        int msgLen = msg.length;
        byte[] len = {
                (byte)((msgLen >> 24) & 0xFF),
                (byte)((msgLen >> 16) & 0xFF),
                (byte)((msgLen >>  8) & 0xFF),
                (byte)((msgLen      ) & 0xFF)};
        return len;
    }
    
	/**
	 * Cast hex encoded value from byte[] to int
	 * 
	 * Limited to Integer.MAX_VALUE: 2^32-1 (4 bytes)
	 * 
	 * @param b array contains the values
	 * @return unsigned positive int value. 
	 */
	public static int byteArrayToInt(byte[] b) {
		if (b == null || b.length == 0)
			return 0;
		return new BigInteger(1, b).intValue();
	}
        
    public static byte[] encodeInt(int value) {
        if (isLessThanUnsigned(value, 253)) {
            return new byte[]{(byte) value};
        } else if (isLessThanUnsigned(value, 65536)) {
            return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
        } else if (isLessThanUnsigned(value, UnsignedInteger.MAX_VALUE.longValue())) {
            byte[] bytes = new byte[5];
            bytes[0] = (byte) 254;
            uint32ToByteArrayLE(value, bytes, 1);
            return bytes;
        } else {
            byte[] bytes = new byte[9];
            bytes[0] = (byte) 255;
            uint32ToByteArrayLE(value, bytes, 1);
            uint32ToByteArrayLE(value >>> 32, bytes, 5);
            return bytes;
        }
    }

    /**
     * Work around lack of unsigned types in Java.
     */
    public static boolean isLessThanUnsigned(long n1, long n2) {
        return UnsignedLongs.compare(n1, n2) < 0;
    }
    
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset + 0] = (byte) (0xFF & (val >> 0));
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

}
