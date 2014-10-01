package org.ethereum.core;

import java.math.BigInteger;
import java.util.*;

import org.ethereum.crypto.ECKey;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.Utils;

/**
 * Representation of an actual account or contract
 */
public class Account  {

	private ECKey ecKey;
	private byte[] address;

    private Set<Transaction> pendingTransactions =
            Collections.synchronizedSet(new HashSet<Transaction>());

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

        BigInteger balance = BigInteger.ZERO;

        if (accountState != null)
            balance = accountState.getBalance();

        synchronized (pendingTransactions){
            if (!pendingTransactions.isEmpty()){

                for (Transaction tx : pendingTransactions){
                    if (Arrays.equals(this.address, tx.getSender())){
                        balance = balance.subtract(new BigInteger(1, tx.getValue()));
                    }

                    if (Arrays.equals(this.address, tx.getReceiveAddress())){
                        balance = balance.add(new BigInteger(1, tx.getValue()));
                    }
                }
                // todo: calculate the fee for pending
            }
        }
        return balance;
    }

    public BigInteger getNonce() {

        AccountState accountState =
            WorldManager.getInstance().getRepository().getAccountState(this.address);

        BigInteger nonce = BigInteger.ZERO;

        if (accountState != null)
            nonce =  accountState.getNonce();

        synchronized (pendingTransactions){
            if (!pendingTransactions.isEmpty()){

                for (Transaction tx : pendingTransactions){
                    if (Arrays.equals(this.address, tx.getSender())){
                        nonce = nonce.add(BigInteger.ONE);
                    }
                }
            }
        }
        return nonce;
    }
    
    public Set<Transaction> getPendingTransactins() {
    	return this.pendingTransactions;
    }

    public void addPendingTransaction(Transaction transaction){
        synchronized (pendingTransactions){
            pendingTransactions.add(transaction);
        }
    }

    public void clearAllPendingTransactions(){
        synchronized (pendingTransactions){
            pendingTransactions.clear();
        }
    }
}
