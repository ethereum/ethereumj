package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 10:25
 */

public class CallCreate {

    byte[] data;
    byte[] destination;
    byte[] gasLimit;
    byte[] value;

/* e.g.
        "data" : [
                ],
        "destination" : "cd1722f3947def4cf144679da39c4c32bdc35681",
        "gasLimit" : 9792,
        "value" : 74
*/

    public CallCreate(JSONObject callCreateJSON) {

        String data        = (String)callCreateJSON.get("data");
        String    destination = (String)callCreateJSON.get("destination");
        Long      gasLimit    = (Long)callCreateJSON.get("gasLimit");
        Long      value       = (Long)callCreateJSON.get("value");

        if (data != null && data.length() > 2)
            this.data    = Hex.decode(data.substring(2));
        else
            this.data = new byte[0];


        this.destination = Hex.decode(destination);
        this.gasLimit    = ByteUtil.bigIntegerToBytes( BigInteger.valueOf(gasLimit) );
        this.value       = ByteUtil.bigIntegerToBytes( BigInteger.valueOf(value) );
    }


    public byte[] getData() {
        return data;
    }

    public byte[] getDestination() {
        return destination;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CallCreate{" +
                "data="          + Hex.toHexString(data) +
                ", destination=" + Hex.toHexString(destination) +
                ", gasLimit="    + Hex.toHexString(gasLimit) +
                ", value="       + Hex.toHexString(value) +
                '}';
    }
}
