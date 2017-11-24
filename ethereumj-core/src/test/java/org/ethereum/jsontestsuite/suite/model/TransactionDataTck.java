package org.ethereum.jsontestsuite.suite.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
