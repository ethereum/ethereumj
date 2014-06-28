package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.Utils;

/**
 * Representation of an actual account or contract
 */
public class Account extends AccountState {

	private ECKey ecKey;
	private byte[] address;
	
	public Account() {
		this.ecKey = new ECKey(Utils.getRandom());
	}
	
	public Account(ECKey ecKey) {
		this.ecKey = ecKey;
	}

	public Account(ECKey ecKey, BigInteger nonce, BigInteger balance) {
		super(nonce, balance);
		this.ecKey = ecKey;
	}	
	
    public ECKey getEcKey() {
        return ecKey;
    }

	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}
}
