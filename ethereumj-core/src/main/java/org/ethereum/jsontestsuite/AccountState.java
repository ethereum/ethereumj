package org.ethereum.jsontestsuite;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * www.ethereumJ.com
 *
 * @author Roman Mandeleil
 * Created on: 28/06/2014 10:25
 */

public class AccountState {

    byte[] address;
    byte[] balance;
    byte[] code;
    byte[] nonce;

    Map<DataWord, DataWord> storage = new HashMap<>();


    public AccountState(byte[] address, JSONObject accountState) {

        this.address = address;
        String balance = accountState.get("balance").toString();
        String code = (String) accountState.get("code");
        String nonce = accountState.get("nonce").toString();

        JSONObject store = (JSONObject) accountState.get("storage");

        this.balance = new BigInteger(balance).toByteArray();

        if (code != null && code.length() > 2)
            this.code = Hex.decode(code.substring(2));
        else
            this.code = ByteUtil.EMPTY_BYTE_ARRAY;

        this.nonce = new BigInteger(nonce).toByteArray();

        int size = store.keySet().size();
        Object[] keys = store.keySet().toArray();
        for (int i = 0; i < size; ++i) {

            String keyS = keys[i].toString();
            String valS = store.get(keys[i]).toString();

            byte[] key = Utils.parseData(keyS);
            byte[] value = Utils.parseData(valS);
            storage.put(new DataWord(key), new DataWord(value));
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


    public Map<DataWord, DataWord> getStorage() {
        return storage;
    }

    public List<String> compareToReal(org.ethereum.core.AccountState state, ContractDetails details) {

        List<String> results = new ArrayList<>();

        BigInteger expectedBalance = new BigInteger(1, this.getBalance());
        if (!state.getBalance().equals(expectedBalance)) {
            String formatedString = String.format("Account: %s: has unexpected balance, expected balance: %s found balance: %s",
                    Hex.toHexString(this.address), expectedBalance.toString(), state.getBalance().toString());
            results.add(formatedString);
        }

        BigInteger expectedNonce = new BigInteger(1, this.getNonce());
        if (!state.getNonce().equals(expectedNonce)) {
            state.getNonce();
            this.getNonce();
            String formatedString = String.format("Account: %s: has unexpected nonce, expected nonce: %s found nonce: %s",
                    Hex.toHexString(this.address), expectedNonce.toString(), state.getNonce().toString());
            results.add(formatedString);
        }

        if (!Arrays.equals(details.getCode(),this.getCode())) {
            String formatedString = String.format("Account: %s: has unexpected nonce, expected nonce: %s found nonce: %s",
                    Hex.toHexString(this.address), Hex.toHexString(this.getCode()), Hex.toHexString(details.getCode()));
            results.add(formatedString);
        }


        // compare storage
        Set<DataWord> keys = details.getStorage().keySet();
        Set<DataWord> expectedKeys = this.getStorage().keySet();
        Set<DataWord> checked = new HashSet<>();

        for (DataWord key : keys) {

            DataWord value = details.getStorage().get(key);
            DataWord expectedValue = this.getStorage().get(key);
            if (expectedValue == null) {

                String formatedString = String.format("Account: %s: has unexpected storage data: %s = %s",
                        Hex.toHexString(this.address),
                        key.toString(),
                        value.toString());

                results.add(formatedString);

                continue;
            }

            if (!expectedValue.equals(value)) {

                String formatedString = String.format("Account: %s: has unexpected value, for key: %s , expectedValue: %s real value: %s",
                        Hex.toHexString(this.address), key.toString(),
                        expectedValue.toString(), value.toString());
                results.add(formatedString);
                continue;
            }

            checked.add(key);
        }

        for (DataWord key : expectedKeys) {
            if (!checked.contains(key)) {
                String formatedString = String.format("Account: %s: doesn't exist expected storage key: %s",
                        Hex.toHexString(this.address), key.toString());
                results.add(formatedString);
            }
        }

        return results;
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
