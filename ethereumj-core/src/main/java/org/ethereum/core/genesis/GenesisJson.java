package org.ethereum.core.genesis;

import java.util.Map;

public class GenesisJson {

    String mixhash;
    String coinbase;
    String timestamp;
    String parentHash;
    String extraData;
    String gasLimit;
    String nonce;
    String difficulty;

    Map<String, AllocatedAccount> alloc;

    public GenesisJson() {
    }


    public String getMixhash() {
        return mixhash;
    }

    public void setMixhash(String mixhash) {
        this.mixhash = mixhash;
    }

    public String getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(String coinbase) {
        this.coinbase = coinbase;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Map<String, AllocatedAccount> getAlloc() {
        return alloc;
    }

    public void setAlloc(Map<String, AllocatedAccount> alloc) {
        this.alloc = alloc;
    }
}
