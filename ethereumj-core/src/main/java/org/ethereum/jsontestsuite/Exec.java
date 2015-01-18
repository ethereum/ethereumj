package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;

import org.json.simple.JSONObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class Exec {

    private final byte[] address;
    private final byte[] caller;
    private final byte[] data;
    private final byte[] code;

    private final byte[] gas;
    private final byte[] gasPrice;

    private final byte[] origin;
    private final byte[] value;

    /*
     e.g:
            "address" : "0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6",
            "caller" : "cd1722f3947def4cf144679da39c4c32bdc35681",
            "data" : [
            ],

            "code" : [ 96,0,96,0,96,0,96,0,96,74,51,96,200,92,3,241 ],

            "gas" : 10000,
            "gasPrice" : 100000000000000,
            "origin" : "cd1722f3947def4cf144679da39c4c32bdc35681",
            "value" : 1000000000000000000
   */
    public Exec(JSONObject exec) {

        String address = exec.get("address").toString();
        String caller = exec.get("caller").toString();

        String code = exec.get("code").toString();
        String data = exec.get("data").toString();

        String gas = exec.get("gas").toString();
        String gasPrice = exec.get("gasPrice").toString();
        String origin = exec.get("origin").toString();

        String value = exec.get("value").toString();

        this.address = Hex.decode(address);
        this.caller = Hex.decode(caller);

        if (code != null && code.length() > 2)
            this.code = Hex.decode(code.substring(2));
        else
            this.code = ByteUtil.EMPTY_BYTE_ARRAY;

        if (data != null && data.length() > 2)
            this.data = Hex.decode(data.substring(2));
        else
            this.data = ByteUtil.EMPTY_BYTE_ARRAY;

        this.gas = ByteUtil.bigIntegerToBytes(new BigInteger(gas));
        this.gasPrice = ByteUtil.bigIntegerToBytes(new BigInteger(gasPrice));

        this.origin = Hex.decode(origin);
        this.value = ByteUtil.bigIntegerToBytes(new BigInteger(value));
    }


    public byte[] getAddress() {
        return address;
    }

    public byte[] getCaller() {
        return caller;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getCode() {
        return code;
    }

    public byte[] getGas() {
        return gas;
    }

    public byte[] getGasPrice() {
        return gasPrice;
    }

    public byte[] getOrigin() {
        return origin;
    }

    public byte[] getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "Exec{" +
                "address=" + Hex.toHexString(address) +
                ", caller=" + Hex.toHexString(caller) +
                ", data=" + Hex.toHexString(data) +
                ", code=" + Hex.toHexString(data) +
                ", gas=" + Hex.toHexString(gas) +
                ", gasPrice=" + Hex.toHexString(gasPrice) +
                ", origin=" + Hex.toHexString(origin) +
                ", value=" + Hex.toHexString(value) +
                '}';
    }
}
