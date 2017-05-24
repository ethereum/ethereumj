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
import org.ethereum.vm.DataWord;

import org.json.simple.JSONObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
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

        this.balance = TestCase.toBigInt(balance).toByteArray();

        if (code != null && code.length() > 2)
            this.code = Hex.decode(code.substring(2));
        else
            this.code = ByteUtil.EMPTY_BYTE_ARRAY;

        this.nonce = TestCase.toBigInt(nonce).toByteArray();

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

    public List<String> compareToReal(org.ethereum.core.AccountState state, ContractDetailsImpl details) {

        List<String> results = new ArrayList<>();

        BigInteger expectedBalance = new BigInteger(1, this.getBalance());
        if (!state.getBalance().equals(expectedBalance)) {
            String formattedString = String.format("Account: %s: has unexpected balance, expected balance: %s found balance: %s",
                    Hex.toHexString(this.address), expectedBalance.toString(), state.getBalance().toString());
            results.add(formattedString);
        }

        BigInteger expectedNonce = new BigInteger(1, this.getNonce());
        if (!state.getNonce().equals(expectedNonce)) {
            state.getNonce();
            this.getNonce();
            String formattedString = String.format("Account: %s: has unexpected nonce, expected nonce: %s found nonce: %s",
                    Hex.toHexString(this.address), expectedNonce.toString(), state.getNonce().toString());
            results.add(formattedString);
        }

        if (!Arrays.equals(details.getCode(), this.getCode())) {
            String formattedString = String.format("Account: %s: has unexpected nonce, expected nonce: %s found nonce: %s",
                    Hex.toHexString(this.address), Hex.toHexString(this.getCode()), Hex.toHexString(details.getCode()));
            results.add(formattedString);
        }


        // compare storage
        Set<DataWord> keys = details.getStorage().keySet();
        Set<DataWord> expectedKeys = this.getStorage().keySet();
        Set<DataWord> checked = new HashSet<>();

        for (DataWord key : keys) {

            DataWord value = details.getStorage().get(key);
            DataWord expectedValue = this.getStorage().get(key);
            if (expectedValue == null) {

                String formattedString = String.format("Account: %s: has unexpected storage data: %s = %s",
                        Hex.toHexString(this.address),
                        key.toString(),
                        value.toString());

                results.add(formattedString);

                continue;
            }

            if (!expectedValue.equals(value)) {

                String formattedString = String.format("Account: %s: has unexpected value, for key: %s , expectedValue: %s real value: %s",
                        Hex.toHexString(this.address), key.toString(),
                        expectedValue.toString(), value.toString());
                results.add(formattedString);
                continue;
            }

            checked.add(key);
        }

        for (DataWord key : expectedKeys) {
            if (!checked.contains(key)) {
                String formattedString = String.format("Account: %s: doesn't exist expected storage key: %s",
                        Hex.toHexString(this.address), key.toString());
                results.add(formattedString);
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
