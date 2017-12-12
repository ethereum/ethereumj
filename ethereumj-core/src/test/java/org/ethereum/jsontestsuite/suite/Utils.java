/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.jsontestsuite.suite;

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

        byte[] bytes;
        if (data.startsWith("0x")) {
            data = data.substring(2);
            if (data.equals("")) return EMPTY_BYTE_ARRAY;

            if (data.length() % 2 == 1) data = "0" + data;

            bytes = Hex.decode(data);
        } else {
            bytes = parseNumericData(data);
        }

        if (ByteUtil.firstNonZeroByte(bytes) == -1) {
            return EMPTY_BYTE_ARRAY;
        } else {
            return bytes;
        }
    }


    public static byte[] parseData(String data) {
        if (data == null) return EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) data = data.substring(2);
        if (data.length() % 2 != 0) data = "0" + data;
        return Hex.decode(data);
    }

    public static byte[] parseNumericData(String data){

        if (data == null || data.equals("")) return EMPTY_BYTE_ARRAY;
        byte[] dataB = unifiedNumericToBigInteger(data).toByteArray();
        return ByteUtil.stripLeadingZeroes(dataB);
    }

    public static long parseLong(String data) {
        boolean hex = data.startsWith("0x");
        if (hex) data = data.substring(2);
        if (data.equals("")) return 0;
        return new BigInteger(data, hex ? 16 : 10).longValue();
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
