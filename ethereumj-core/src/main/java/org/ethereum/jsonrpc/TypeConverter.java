package org.ethereum.jsonrpc;

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Created by Ruben on 19/11/2015.
 */
public class TypeConverter {

    public static byte[] StringNumberAsBytes(String input) {
        return ByteUtil.bigIntegerToBytes(StringDecimalToBigInteger(input));
    }

    public static BigInteger StringNumberAsBigInt(String input) throws Exception {
        if (input.startsWith("0x"))
            return TypeConverter.StringHexToBigInteger(input);
        else
            return TypeConverter.StringDecimalToBigInteger(input);
    }

    public static BigInteger StringHexToBigInteger(String input) {
        String hexa = input.substring(2);
        return new BigInteger(hexa, 16);
    }

    private static BigInteger StringDecimalToBigInteger(String input) {
        return new BigInteger(input);
    }

    public static byte[] StringHexToByteArray(String x) throws Exception {
        if (x.startsWith("0x")) {
            x = x.substring(2);
        }
        if (x.length() % 2 != 0) x = "0" + x;
        return Hex.decode(x);
    }

    public static String toJsonHex(byte[] x) {
        return "0x"+Hex.toHexString(x);
    }

    public static String toJsonHex(String x) {
        return "0x"+x;
    }

    public static String toJsonHex(long n) {
        return "0x"+Long.toHexString(n);
    }

    public static String toJsonHex(BigInteger n) {
        return "0x"+ n.toString(16);
    }
}
