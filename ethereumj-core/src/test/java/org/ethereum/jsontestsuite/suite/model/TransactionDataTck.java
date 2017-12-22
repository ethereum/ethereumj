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
package org.ethereum.jsontestsuite.suite.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDataTck {

    List<String> data;
    List<String> gasLimit;
    String gasPrice;
    String nonce;
    String secretKey;
    String r;
    String s;
    String to;
    String v;
    List<String> value;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public List<String> getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(List<String> gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public TransactionTck getTransaction(PostDataTck.Indexes idx) {

        // sanity check
        if (idx.getValue() >= value.size()) throw new IllegalArgumentException("Fail to get Tx.value by idx: " + idx.getValue());
        if (idx.getData() >= data.size()) throw new IllegalArgumentException("Fail to get Tx.data by idx: " + idx.getData());
        if (idx.getGas() >= gasLimit.size()) throw new IllegalArgumentException("Fail to get Tx.gasLimit by idx: " + idx.getGas());

        TransactionTck tx = new TransactionTck();

        tx.setValue(value.get(idx.getValue()));
        tx.setData(data.get(idx.getData()));
        tx.setGasLimit(gasLimit.get(idx.getGas()));
        tx.setGasPrice(gasPrice);
        tx.setNonce(nonce);
        tx.setSecretKey(secretKey);
        tx.setR(r);
        tx.setS(s);
        tx.setTo(to);
        tx.setV(v);

        return tx;
    }
}
