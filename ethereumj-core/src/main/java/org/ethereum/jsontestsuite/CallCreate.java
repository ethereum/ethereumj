package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;

import org.json.simple.JSONObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class CallCreate {

    private final byte[] data;
    private final byte[] destination;
    private final byte[] gasLimit;
    private final byte[] value;

/* e.g.
        "data" : [
                ],
        "destination" : "cd1722f3947def4cf144679da39c4c32bdc35681",
        "gasLimit" : 9792,
        "value" : 74
*/

    public CallCreate(JSONObject callCreateJSON) {

        String data = callCreateJSON.get("data").toString();
        String destination = callCreateJSON.get("destination").toString();
        String gasLimit = callCreateJSON.get("gasLimit").toString();
        String value = callCreateJSON.get("value").toString();

        if (data != null && data.length() > 2)
            this.data = Hex.decode(data.substring(2));
        else
            this.data = ByteUtil.EMPTY_BYTE_ARRAY;

        this.destination = Hex.decode(destination);
        this.gasLimit = ByteUtil.bigIntegerToBytes(new BigInteger(gasLimit));
        this.value = ByteUtil.bigIntegerToBytes(new BigInteger(value));
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
                "data=" + Hex.toHexString(data) +
                ", destination=" + Hex.toHexString(destination) +
                ", gasLimit=" + Hex.toHexString(gasLimit) +
                ", value=" + Hex.toHexString(value) +
                '}';
    }
}
