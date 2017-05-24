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

import org.json.simple.JSONObject;

import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class Env {

    private final byte[] currentCoinbase;
    private final byte[] currentDifficulty;
    private final byte[] currentGasLimit;
    private final byte[] currentNumber;
    private final byte[] currentTimestamp;
    private final byte[] previousHash;


    public Env(byte[] currentCoinbase, byte[] currentDifficulty, byte[] 
            currentGasLimit, byte[] currentNumber, byte[] 
            currentTimestamp, byte[] previousHash) {
        this.currentCoinbase = currentCoinbase;
        this.currentDifficulty = currentDifficulty;
        this.currentGasLimit = currentGasLimit;
        this.currentNumber = currentNumber;
        this.currentTimestamp = currentTimestamp;
        this.previousHash = previousHash;
    }

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

        String coinbase = env.get("currentCoinbase").toString();
        String difficulty = env.get("currentDifficulty").toString();
        String timestamp = env.get("currentTimestamp").toString();
        String number = env.get("currentNumber").toString();
        String gasLimit = Utils.parseUnidentifiedBase(env.get("currentGasLimit").toString());
        Object previousHash = env.get("previousHash");
        String prevHash = previousHash == null ? "" : previousHash.toString();

        this.currentCoinbase = Hex.decode(coinbase);
        this.currentDifficulty = BigIntegers.asUnsignedByteArray(TestCase.toBigInt(difficulty) );
        this.currentGasLimit =   BigIntegers.asUnsignedByteArray(TestCase.toBigInt(gasLimit));
        this.currentNumber = TestCase.toBigInt(number).toByteArray();
        this.currentTimestamp = TestCase.toBigInt(timestamp).toByteArray();
        this.previousHash = Hex.decode(prevHash);

    }

    public byte[] getCurrentCoinbase() {
        return currentCoinbase;
    }

    public byte[] getCurrentDifficulty() {
        return currentDifficulty;
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
                ", currentDifficulty=" + Hex.toHexString(currentDifficulty) +
                ", currentGasLimit=" + Hex.toHexString(currentGasLimit) +
                ", currentNumber=" + Hex.toHexString(currentNumber) +
                ", currentTimestamp=" + Hex.toHexString(currentTimestamp) +
                ", previousHash=" + Hex.toHexString(previousHash) +
                '}';
    }
}
