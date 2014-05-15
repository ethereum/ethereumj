package org.ethereum.core;

import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

/**
 * A transaction (formally, T ) is a single cryptographically 
 * signed instruction sent by an actor external to Ethereum. 
 * An external actor can be a person (via a mobile device or desktop computer) 
 * or could be from a piece of automated software running on a server. 
 * There are two types of transactions: those which result in message calls 
 * and those which result in the creation of new contracts.
 */
public class Transaction {

    private RLPList rawData;
    private boolean parsed = false;

    /* creation contract tx
     * [ nonce, endowment, 0, gasPrice, gasDeposit (for init), body, init, signature(v, r, s) ]
     * or simple send tx
     * [ nonce, value, receiveAddress, gasPrice, gasDeposit, data, signature(v, r, s) ]
     */
    
    /* SHA3 hash of the rlpEncoded transaction */
    private byte[] hash;
    /* a counter used to make sure each transaction can only be processed once */
    private byte[] nonce;
    /* the amount of ether to transfer (calculated as wei) */
    private byte[] value;
    /* the address of the destination account 
     * In creation transaction the receive address is - 0 */
    private byte[] receiveAddress;   
	/* the amount of ether to pay as a transaction fee 
	 * to the miner for each unit of gas */
    private byte[] gasPrice;
	/* the amount of "gas" to allow for the computation. 
	 * Gas is the fuel of the computational engine; 
	 * every computational step taken and every byte added 
	 * to the state or transaction list consumes some gas. */
    private byte[] gasLimit;
	/* An unlimited size byte array specifying 
	 * either input [data] of the message call 
	 * or the [body] for a new contract */
    private byte[] data;
	/* Initialisation code for a new contract */
    private byte[] init;
	/* the elliptic curve signature 
	 * (including public key recovery bits) */
    private ECDSASignature signature;

    public Transaction(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }

    public Transaction(byte[] nonce, byte[] value, byte[] recieveAddress, byte[] gasPrice, byte[] gas, byte[] data, byte v, byte[] r, byte[] s) {
        this.nonce = nonce;
        this.value = value;
        this.receiveAddress = recieveAddress;
        this.gasPrice = gasPrice;
        this.gasLimit = gas;
        this.data = data;
        this.signature = ECDSASignature.fromComponents(r, s, v);
        parsed = true;
    }

    public void rlpParse(){

        RLPList params = (RLPList) rawData.get(0);

        this.hash = HashUtil.sha3(rawData.getRLPData());
        this.nonce =          ((RLPItem) params.get(0)).getData();
        this.value =          ((RLPItem) params.get(1)).getData();
        this.receiveAddress = ((RLPItem) params.get(2)).getData();
        this.gasPrice =       ((RLPItem) params.get(3)).getData();
        this.gasLimit =       ((RLPItem) params.get(4)).getData();
        this.data =           ((RLPItem) params.get(5)).getData();

        if (params.size() == 9){  // Simple transaction
        	byte v =		((RLPItem) params.get(6)).getData()[0];
            byte[] r =		((RLPItem) params.get(7)).getData();
            byte[] s =		((RLPItem) params.get(8)).getData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else if (params.size() == 10){ // Contract creation transaction
            this.init =     ((RLPItem) params.get(6)).getData();
            byte v =		((RLPItem) params.get(7)).getData()[0];
            byte[] r =		((RLPItem) params.get(8)).getData();
            byte[] s =		((RLPItem) params.get(9)).getData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else throw new RuntimeException("Wrong tx data element list size");
        this.parsed = true;
    }

    public RLPList getRawData() {
        return rawData;
    }

    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {
        if (!parsed) rlpParse();
        return hash;
    }

    public byte[] getNonce() {
        if (!parsed) rlpParse();
        return nonce;
    }

    public byte[] getValue() {
        if (!parsed) rlpParse();
        return value;
    }

    public byte[] getReceiveAddress() {
        if (!parsed) rlpParse();
        return receiveAddress;
    }

    public byte[] getGasPrice() {
        if (!parsed) rlpParse();
        return gasPrice;
    }

    public byte[] getGasLimit() {
        if (!parsed) rlpParse();
        return gasLimit;
    }

    public byte[] getData() {
        if (!parsed) rlpParse();
        return data;
    }

    public byte[] getInit() {
        if (!parsed) rlpParse();
        return init;
    }

    public ECDSASignature getSignature() {
        if (!parsed) rlpParse();
        return signature;
    }
    
	public boolean isContract() {
		return this.receiveAddress.length == 0;
	}

	/********* 
	 * Crypto
	 */
    
	public ECKey getKey() {
		byte[] hash = this.getHash();	
		return ECKey.recoverFromSignature(signature.v, signature, hash, true);
	}
	
	public byte[] sender() {
		ECKey eckey = this.getKey();
		// Validate the returned key.
		// Return null if public key isn't in a correct format
		if (!eckey.isPubKeyCanonical()) {
			return null;
		}
		return eckey.getAddress();
	}

	public void sign(byte[] privKeyBytes) throws Exception {
		byte[] hash = this.getHash();
		ECKey key = ECKey.fromPrivate(privKeyBytes);
		this.signature = key.sign(hash);
	}

    @Override
    public String toString() {
        if (!parsed) rlpParse();
        return "TransactionData [" +  " hash=" + Utils.toHexString(hash) +
                "  nonce=" + Utils.toHexString(nonce) +
                ", value=" + Utils.toHexString(value) +
                ", receiveAddress=" + Utils.toHexString(receiveAddress) +
                ", gasPrice=" + Utils.toHexString(gasPrice) +
                ", gas=" + Utils.toHexString(gasLimit) +
                ", data=" + Utils.toHexString(data) +
                ", init=" + Utils.toHexString(init) +
                ", signatureV=" + signature.v +
                ", signatureR=" + Utils.toHexString(signature.r.toByteArray()) +
                ", signatureS=" + Utils.toHexString(signature.s.toByteArray()) +
                ']';
    }
}
