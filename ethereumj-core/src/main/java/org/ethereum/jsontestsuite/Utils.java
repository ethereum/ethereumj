package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.Utils.unifiedNumericToBigInteger;

/**
 * @author Roman Mandeleil
 * @since 15.12.2014
 */
public class Utils {

    public static byte[] parseVarData(String data){
        if (data == null || data.equals("")) return EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) {
            data = data.substring(2);
            if (data.equals("")) return EMPTY_BYTE_ARRAY;

            if (data.length() % 2 == 1) data = "0" + data;

            return Hex.decode(data);
        }

        return parseNumericData(data);
    }


    public static byte[] parseData(String data) {
        if (data == null) return EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) data = data.substring(2);
        return Hex.decode(data);
    }

    public static byte[] parseNumericData(String data){

        if (data == null || data.equals("")) return EMPTY_BYTE_ARRAY;
        byte[] dataB = unifiedNumericToBigInteger(data).toByteArray();
        return ByteUtil.stripLeadingZeroes(dataB);
    }

    public static long parseLong(String data) {
        return data.equals("") ? 0 : Long.parseLong(data);
    }

    public static byte parseByte(String data) {
        if (data.startsWith("0x")) {
            data = data.substring(2);
            return data.equals("") ? 0 : Byte.parseByte(data, 16);
        } else
            return data.equals("") ? 0 : Byte.parseByte(data);
    }


    public static String parseUnidentifiedBase(String number) {
        if (number.startsWith("0x"))
          number = new BigInteger(number.substring(2), 16).toString(10);
        return number;
    }
}
