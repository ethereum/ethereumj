package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethergit.com
 *
 * @author Roman Mandeleil
 * Created on: 15/12/2014 12:41
 */

public class Utils {


    public static byte[] parseData(String data) {
        if (data == null) return ByteUtil.EMPTY_BYTE_ARRAY;
        if (data.startsWith("0x")) data = data.substring(2);
        return Hex.decode(data);
    }

    public static long parseLong(String data) {
        return data.equals("") ? 0 : Long.parseLong(data);
    }

}
