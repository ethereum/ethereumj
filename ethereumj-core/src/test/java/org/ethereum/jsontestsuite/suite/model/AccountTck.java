package org.ethereum.jsontestsuite.suite.model;

import java.util.*;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class AccountTck {

    String balance;
    String code;
    String nonce;

    Map<String, String> storage = new HashMap<>();

    String privateKey;

    public AccountTck() {
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
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

    public Map<String, String> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, String> storage) {
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
