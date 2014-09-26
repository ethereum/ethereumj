package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.ByteUtil;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 10:25
 */

public class AccountState {

    byte[] address;
    byte[] balance;
    byte[] code;
    byte[] nonce;

    Map<ByteArrayWrapper, ByteArrayWrapper> storage = new HashMap<>();


    public AccountState(byte[] address, JSONObject accountState) {

        this.address = address;
        String    balance  = accountState.get("balance").toString();
        String    code     = (String)accountState.get("code");
        String    nonce    = accountState.get("nonce").toString();

        JSONObject store    = (JSONObject)accountState.get("storage");

        this.balance = new BigInteger(balance).toByteArray();

        if (code != null && code.length() > 2)
            this.code    = Hex.decode(code.substring(2));
        else
            this.code = new byte[0];

        this.nonce   = new BigInteger(nonce).toByteArray();

        int size = store.keySet().size();
        Object[] keys = store.keySet().toArray();
        for (int i = 0; i < size; ++i) {

            String keyS = keys[i].toString();
            String valS =  store.get(keys[i]).toString();

            ByteArrayWrapper key;
            boolean hexVal = Pattern.matches("0[xX][0-9a-fA-F]+", keyS);
            if (hexVal) {
                key = new ByteArrayWrapper(Hex.decode(keyS.substring(2)));
            } else {
                byte[] data = ByteUtil.bigIntegerToBytes(new BigInteger(keyS));
                key = new ByteArrayWrapper(data);
            }

            ByteArrayWrapper value;
            hexVal = Pattern.matches("0[xX][0-9a-fA-F]+", valS);
            if (hexVal) {
                value = new ByteArrayWrapper(Hex.decode(valS.substring(2)));
            } else {
                byte[] data = ByteUtil.bigIntegerToBytes(new BigInteger(valS));
                value = new ByteArrayWrapper(data);
            }
            storage.put(key, value);
        }
    }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getBalance() {
        return balance;
    }

    public BigInteger getBigIntegerBalance() {
        return new BigInteger(balance);
    }


    public byte[] getCode() {
        return code;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public long getNonceLong() {
        return new BigInteger(nonce).longValue();
    }


    public Map<ByteArrayWrapper, ByteArrayWrapper> getStorage() {
        return storage;
    }

    @Override
    public String toString() {
        return "AccountState{" +
                "address=" + Hex.toHexString(address) +
                ", balance=" + Hex.toHexString(balance) +
                ", code=" + Hex.toHexString(code) +
                ", nonce=" + Hex.toHexString(nonce) +
                ", storage=" + storage +
                '}';
    }
}
