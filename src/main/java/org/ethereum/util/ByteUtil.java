package org.ethereum.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.spongycastle.util.encoders.Hex;

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
		if (b == null)
			return null;
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;        
    }
    
    public static byte[] longToBytes(long l) {
    	return ByteBuffer.allocate(8).putLong(l).array();
    }
    
    public static String toHexString(byte[] data){
        if (data == null) return "null";
        else return Hex.toHexString(data);
    }
    
    // The packet size should be 4 byte long
    public static byte[] calcPacketSize(byte[] packet){

        byte[] size = new byte[4];

        size[3] = (byte)(packet.length >> 0 & 0xFF);
        size[2] = (byte)(packet.length >> 8 & 0xFF);
        size[1] = (byte)(packet.length >> 16 & 0xFF);
        size[0] = (byte)(packet.length >> 24 & 0xFF);

        return size;
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

    /**
     * Calculate the number of bytes need
     * to encode the number
     *
     * @param val - number
     * @return number of min bytes used to encode the number
     */
    public static int numBytes(String val) {

        BigInteger bInt = new BigInteger(val);
        int bytes = 0;

        while(!bInt.equals(BigInteger.ZERO)) {
            bInt = bInt.shiftRight(8);
            ++bytes;
        }
        if (bytes == 0) ++bytes;
        return bytes;
    }

    /**
     * @param arg - not more that 32 bits
     * @return - bytes of the value pad with complete to 32 zeroes
     */
	public static byte[] encodeValFor32Bits(Object arg) {

		byte[] data;

		// check if the string is numeric
		if (arg.toString().trim().matches("-?\\d+(\\.\\d+)?")) {
			data = new BigInteger(arg.toString().trim()).toByteArray();

               // check if it's hex number
		} else if (arg.toString().trim().matches("0[xX][0-9a-fA-F]+")){
            data = new BigInteger(arg.toString().trim().substring(2), 16).toByteArray();

        } else {
			data = arg.toString().trim().getBytes();
		}



		if (data.length > 32)
			throw new RuntimeException("values can't be more than 32 byte");

		byte[] val = new byte[32];

		int j = 0;
		for (int i = data.length; i > 0; --i) {
			val[31 - j] = data[i - 1];
			++j;
		}
		return val;
	}

	/**
	 * encode the values and concatenate together
	 */
	public static byte[] encodeDataList(Object... args) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (Object arg : args) {
			byte[] val = encodeValFor32Bits(arg);
			try {
				baos.write(val);
			} catch (IOException e) {
				throw new Error("Happen something that should never happen ", e);
			}
		}
		return baos.toByteArray();
	}
	
	/**
	 * increment byte array as a number until max is reached
	 */
	public static boolean increment(byte[] bytes) {
    	final int startIndex = 0;
        int i;
        for (i = bytes.length-1; i >= startIndex; i--) {
            bytes[i]++;
            if (bytes[i] != 0)
                break;
        }
        // we return false when all bytes are 0 again
        return (i >= startIndex || bytes[startIndex] != 0);
    }
}