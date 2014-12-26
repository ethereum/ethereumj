package org.ethereum.jsontestsuite;

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
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 11:59
 */
public class Helper {

    private static Logger logger = LoggerFactory.getLogger("misc");

    public static byte[] parseDataArray(JSONArray valArray) {

        // value can be:
        //   1. 324234 number
        //   2. "0xAB3F23A" - hex string
        //   3. "239472398472" - big number

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < valArray.size(); ++i) {

            Object val = valArray.get(i);
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
