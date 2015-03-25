package org.ethereum.jsontestsuite.model;

public class EnvTck {

    String currentCoinbase;
    String currentDifficulty;
    String currentGasLimit;
    String currentNumber;
    String currentTimestamp;
    String previousHash;

    public EnvTck() {
    }

    public String getCurrentCoinbase() {
        return currentCoinbase;
    }

    public void setCurrentCoinbase(String currentCoinbase) {
        this.currentCoinbase = currentCoinbase;
    }

    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    public String getCurrentGasLimit() {
        return currentGasLimit;
    }

    public void setCurrentGasLimit(String currentGasLimit) {
        this.currentGasLimit = currentGasLimit;
    }

    public String getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(String currentNumber) {
        this.currentNumber = currentNumber;
    }

    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    public void setCurrentTimestamp(String currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
}
