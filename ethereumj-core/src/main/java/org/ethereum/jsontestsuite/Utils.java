package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 15.12.2014
 */
public class Utils {



    public static byte[] parseData(String data) {
        if (data == null) return ByteUtil.EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) data = data.substring(2);
        return Hex.decode(data);
    }

    public static byte[] parseNumericData(String data){

        if (data == null) return ByteUtil.EMPTY_BYTE_ARRAY;
        byte[] dataB = new BigInteger(data, 10).toByteArray();
        return ByteUtil.stripLeadingZeroes(dataB);
    }

    public static long parseLong(String data) {
        return data.equals("") ? 0 : Long.parseLong(data);
    }

    public static byte parseByte(String data) {
        return data.equals("") ? 0 : Byte.parseByte(data);
    }


    public static String parseUnidentifiedBase(String number) {
        if (number.startsWith("0x"))
          number = new BigInteger(number.substring(2), 16).toString(10);
        return number;
    }
}
