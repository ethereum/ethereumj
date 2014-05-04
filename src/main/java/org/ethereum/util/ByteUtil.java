package org.ethereum.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLongs;

public class ByteUtil {

    /** The string that prefixes all text messages signed using Bitcoin keys. */
    public static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
    public static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES = BITCOIN_SIGNED_MESSAGE_HEADER.getBytes();
	
    /**
     * Creates a copy of bytes and appends b to the end of it
     */
    public static byte[] appendByte(byte[] bytes, byte b) {
        byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
        result[result.length - 1] = b;
        return result;
    }

//    /**
//     * Returns the given byte array hex encoded.
//     */
//    public static String bytesToHexString(byte[] bytes) {
//        StringBuffer buf = new StringBuffer(bytes.length * 2);
//        for (byte b : bytes) {
//            String s = Integer.toString(0xFF & b, 16);
//            if (s.length() < 2)
//                buf.append('0');
//            buf.append(s);
//        }
//        return buf.toString();
//    }
    
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
     * <p>Given a textual message, returns a byte buffer formatted as follows:</p>
     *
     * <tt><p>[24] "Bitcoin Signed Message:\n" [message.length as a varint] message</p></tt>
     */
    public static byte[] formatForBitcoinSigning(String message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
            byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));
            bos.write(encodeInt(messageBytes.length));
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }
    
    /**
     * <p>Given a textual message, returns a byte buffer formatted as follows:</p>
     */
    public static byte[] formatForEthereumSigning(byte[] message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(encodeInt(message.length));
            bos.write(message);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
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
