package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 10:23
 */

public class Env {

    private byte[] currentCoinbase;
    private byte[] currentDifficlty;
    private byte[] currentGasLimit;
    private byte[] currentNumber;
    private byte[] currentTimestamp;
    private byte[] previousHash;


    /*
        e.g:
            "currentCoinbase" : "2adc25665018aa1fe0e6bc666dac8fc2697ff9ba",
            "currentDifficulty" : "256",
            "currentGasLimit" : "1000000",
            "currentNumber" : "0",
            "currentTimestamp" : 1,
            "previousHash" : "5e20a0453cecd065ea59c37ac63e079ee08998b6045136a8ce6635c7912ec0b6"
  */
    public Env(JSONObject env) {

        String coinbase   = env.get("currentCoinbase").toString();
        String difficulty = env.get("currentDifficulty").toString();
        String timestamp  = env.get("currentTimestamp").toString();
        String number      = env.get("currentNumber").toString();
        String gasLimit    = env.get("currentGasLimit").toString();
        String prevHash   = env.get("previousHash").toString();

        this.currentCoinbase  = Hex.decode(coinbase);
        this.currentDifficlty = ByteUtil.bigIntegerToBytes( new BigInteger(difficulty) );
        this.currentGasLimit  = ByteUtil.bigIntegerToBytes( new BigInteger(gasLimit) );
        this.currentNumber    = ByteUtil.bigIntegerToBytes( new BigInteger(number) );
        this.currentTimestamp = ByteUtil.bigIntegerToBytes( new BigInteger(timestamp) );
        this.previousHash     = Hex.decode(prevHash);

    }

    public byte[] getCurrentCoinbase() {
        return currentCoinbase;
    }

    public byte[] getCurrentDifficlty() {
        return currentDifficlty;
    }

    public byte[] getCurrentGasLimit() {
        return currentGasLimit;
    }

    public byte[] getCurrentNumber() {
        return currentNumber;
    }

    public byte[] getCurrentTimestamp() {
        return currentTimestamp;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    @Override
    public String toString() {
        return "Env{" +
                "currentCoinbase=" + Hex.toHexString(currentCoinbase) +
                ", currentDifficlty=" + Hex.toHexString(currentDifficlty) +
                ", currentGasLimit=" + Hex.toHexString(currentGasLimit) +
                ", currentNumber=" + Hex.toHexString(currentNumber) +
                ", currentTimestamp=" + Hex.toHexString(currentTimestamp) +
                ", previousHash=" + Hex.toHexString(previousHash) +
                '}';
    }
}
