package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

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
    private byte[] stateRoot = new byte[0];
    
    /* The hash of the EVM code of this contract—this is the code 
     * that gets executed should this address receive a message call; 
     * it is immutable and thus, unlike all other fields, cannot be changed 
     * after construction. All such code fragments are contained in 
     * the state database under their corresponding hashes for later 
     * retrieval */
    private byte[] codeHash = HashUtil.sha3(new byte[0]);

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
		this.nonce 		= new BigInteger(1, ((items.get(0).getRLPData()) == null ? new byte[]{0} :
                                                                                   items.get(0).getRLPData()));
		this.balance 	= new BigInteger(1, ((items.get(1).getRLPData()) == null ? new byte[]{0} : 
																					items.get(1).getRLPData()));
		this.stateRoot 	= items.get(2).getRLPData();
		this.codeHash 	= items.get(3).getRLPData();
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void incrementNonce(){
        rlpEncoded = null;
        this.nonce = nonce.add(BigInteger.ONE);
    }

    public void setCodeHash(byte[] codeHash){
        rlpEncoded = null;
        this.codeHash = codeHash;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void addToBalance(BigInteger value){
        if (value.signum() != 0) rlpEncoded = null;
        this.balance = balance.add(value);
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
}
