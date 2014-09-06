package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.ECKey;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.Utils;

/**
 * Representation of an actual account or contract
 */
public class Account  {

	private ECKey ecKey;
	private byte[] address;
	
	public Account() {
		this.ecKey = new ECKey(Utils.getRandom());
        address = this.ecKey.getAddress();
	}
	
	public Account(ECKey ecKey) {
		this.ecKey = ecKey;
        address = this.ecKey.getAddress();
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

    public AccountState getAccountState(){
        AccountState accountState =
                WorldManager.getInstance().getRepository().getAccountState(this.address);

        return accountState;
    }

    public BigInteger getBalance() {

        AccountState accountState =
                WorldManager.getInstance().getRepository().getAccountState(this.address);

        if (accountState != null)
            return accountState.getBalance();

        return null;
    }


    public BigInteger getNonce() {

        AccountState accountState =
            WorldManager.getInstance().getRepository().getAccountState(this.address);

        if (accountState != null)
            return accountState.getNonce();

        return null;
    }
}
