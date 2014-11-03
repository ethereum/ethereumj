package org.ethereum.core;

import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class AccountState {

    private byte[] rlpEncoded;
    
    /* A value equal to the number of transactions sent
     * from this address, or, in the case of contract accounts, 
     * the number of contract-creations made by this account */
    private BigInteger nonce;
    
    /* A scalar value equal to the number of Wei owned by this address */
    private BigInteger balance;
    
    /* A 256-bit hash of the root node of a trie structure 
     * that encodes the storage contents of the contract, 
     * itself a simple mapping between byte arrays of size 32. 
     * The hash is formally denoted σ[a] s . 
     * 
     * Since I typically wish to refer not to the trie’s root hash 
     * but to the underlying set of key/value pairs stored within,
     * I define a convenient equivalence TRIE (σ[a] s ) ≡ σ[a] s . 
     * It shall be understood that σ[a] s is not a ‘physical’ member 
     * of the account and does not contribute to its later serialisation */
    private byte[] stateRoot = EMPTY_TRIE_HASH;
    
    /* The hash of the EVM code of this contract—this is the code 
     * that gets executed should this address receive a message call; 
     * it is immutable and thus, unlike all other fields, cannot be changed 
     * after construction. All such code fragments are contained in 
     * the state database under their corresponding hashes for later 
     * retrieval */
    private byte[] codeHash = EMPTY_DATA_HASH;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        this.nonce = nonce;
        this.balance = balance;
    }
    
    public AccountState(byte[] rlpData) {
        this.rlpEncoded = rlpData;

		RLPList items 	= (RLPList) RLP.decode2(rlpEncoded).get(0);
		this.nonce = items.get(0).getRLPData() == null ? BigInteger.ZERO
				: new BigInteger(1, items.get(0).getRLPData());
		this.balance = items.get(1).getRLPData() == null ? BigInteger.ZERO
				: new BigInteger(1, items.get(1).getRLPData());
		this.stateRoot 	= items.get(2).getRLPData();
		this.codeHash 	= items.get(3).getRLPData();
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        rlpEncoded = null;
        this.stateRoot = stateRoot;
    }

    public void incrementNonce() {
        rlpEncoded = null;
        this.nonce = nonce.add(BigInteger.ONE);
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(byte[] codeHash) {
        rlpEncoded = null;
        this.codeHash = codeHash;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger addToBalance(BigInteger value) {
        if (value.signum() != 0) rlpEncoded = null;
        this.balance = balance.add(value);
        return this.balance;
    }

	public void subFromBalance(BigInteger value) {
        if (value.signum() != 0) rlpEncoded = null;
        this.balance = balance.subtract(value);
	}
    
    public byte[] getEncoded() {
		if(rlpEncoded == null) {
	        byte[] nonce		= RLP.encodeBigInteger(this.nonce);
	        byte[] balance		= RLP.encodeBigInteger(this.balance);
	        byte[] stateRoot	= RLP.encodeElement(this.stateRoot);
	        byte[] codeHash		= RLP.encodeElement(this.codeHash);
	        this.rlpEncoded = RLP.encodeList(nonce, balance, stateRoot, codeHash);
		}
		return rlpEncoded;
    }
    
    public String toString() {
    	String ret =  "Nonce: " 		+ this.getNonce().toString() 							+ "\n" + 
    				  "Balance: " 		+ Denomination.toFriendlyString(getBalance()) 			+ "\n" +
    				  "State Root: " 	+ Hex.toHexString(this.getStateRoot()) 	+ "\n" +
    				  "Code Hash: " 	+ Hex.toHexString(this.getCodeHash());
    	return ret;
    }
}
