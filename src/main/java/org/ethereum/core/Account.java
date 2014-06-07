package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.Utils;

/**
 * Representation of an actual account or contract
 */
public class Account {

	private ECKey ecKey;
	private byte[] address;
	private AccountState state;
	
	public Account() {
		this.ecKey = new ECKey(Utils.getRandom());
		this.state = new AccountState();
	}
	
	public Account(ECKey ecKey) {
		this.ecKey = ecKey;
		this.state = new AccountState();
	}

	public Account(ECKey ecKey, BigInteger nonce, BigInteger balance) {
		this.ecKey = ecKey;
		this.state = new AccountState(nonce, balance);
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

	public AccountState getState() {
		return state;
	}

	public void setState(AccountState state) {
		this.state = state;
	}	
}
