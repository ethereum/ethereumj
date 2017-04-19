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

import org.json.simple.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigInteger;

import java.util.regex.Pattern;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class Helper {

    private static Logger logger = LoggerFactory.getLogger("misc");

    public static byte[] parseDataArray(JSONArray valArray) {

        // value can be:
        //   1. 324234 number
        //   2. "0xAB3F23A" - hex string
        //   3. "239472398472" - big number

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (Object val : valArray) {

            if (val instanceof String) {

                // Hex num
                boolean hexVal = Pattern.matches("0[xX][0-9a-fA-F]+", val.toString());
                if (hexVal) {
                    String number = ((String) val).substring(2);
                    if (number.length() % 2 == 1) number = "0" + number;
                    byte[] data = Hex.decode(number);
                    try {
                        bos.write(data);
                    } catch (IOException e) {
                        logger.error("should not happen", e);
                    }
                } else {

                    // BigInt num
                    boolean isNumeric = Pattern.matches("[0-9a-fA-F]+", val.toString());
                    if (!isNumeric) throw new Error("Wrong test case JSON format");
                    else {
                        BigInteger value = new BigInteger(val.toString());
                        try {
                            bos.write(value.toByteArray());
                        } catch (IOException e) {
                            logger.error("should not happen", e);
                        }
                    }
                }
            } else if (val instanceof Long) {

                // Simple long
                byte[] data = ByteUtil.bigIntegerToBytes(BigInteger.valueOf((Long) val));
                try {
                    bos.write(data);
                } catch (IOException e) {
                    logger.error("should not happen", e);
                }
            } else {
                throw new Error("Wrong test case JSON format");
            }
        }
        return bos.toByteArray();
    }
}
