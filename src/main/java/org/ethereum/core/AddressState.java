package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.Utils;

import java.math.BigInteger;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/05/2014 10:43
 */
public class AddressState {

    private ECKey ecKey;
    private BigInteger nonce;
    private BigInteger balance;

    public AddressState() {
        this(new ECKey(Utils.getRandom()));
    }

    public AddressState(ECKey ecKey) {
    	this(ecKey, BigInteger.ZERO, BigInteger.ZERO);
    }

    public AddressState(ECKey ecKey, BigInteger nonce, BigInteger balance) {
        this.ecKey = ecKey;
        this.nonce = nonce;
        this.balance = balance;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void incrementNonce(){
        this.nonce = nonce.add(BigInteger.ONE);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void addToBalance(BigInteger value){
        this.balance = balance.add(value);
    }
}
