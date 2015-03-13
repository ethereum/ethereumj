package org.ethereum.jsontestsuite.model;

import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class AccountTCK {

    String balance;
    String code;
    String nonce;

    Map<DataWord, DataWord> storage = new HashMap<>();

    public AccountTCK() {
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Map<DataWord, DataWord> getStorage() {
        return storage;
    }

    public void setStorage(Map<DataWord, DataWord> storage) {
        this.storage = storage;
    }

    @Override
    public String toString() {
        return "AccountState2{" +
                "balance='" + balance + '\'' +
                ", code='" + code + '\'' +
                ", nonce='" + nonce + '\'' +
                ", storage=" + storage +
                '}';
    }
}
