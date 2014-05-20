package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * A transaction (formally, T ) is a single cryptographically 
 * signed instruction sent by an actor external to Ethereum. 
 * An external actor can be a person (via a mobile device or desktop computer) 
 * or could be from a piece of automated software running on a server. 
 * There are two types of transactions: those which result in message calls 
 * and those which result in the creation of new contracts.
 */
public class Transaction {

    private byte[] rlpEncoded;
    private byte[] unsignedRLPEncoded;

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
	 * input [data] of the message call */ 
    private byte[] data;

	/* Initialisation code for a new contract */
    private byte[] init;

	/* the elliptic curve signature
	 * (including public key recovery bits) */
    private ECDSASignature signature;

    public Transaction(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }

    public Transaction(byte[] nonce, byte[] value, byte[] recieveAddress, byte[] gasPrice, byte[] gas, byte[] data) {
        this.nonce = nonce;
        this.value = value;
        this.receiveAddress = recieveAddress;
        this.gasPrice = gasPrice;
        this.gasLimit = gas;
        if(recieveAddress == null) {
        	this.init = data;
        } else {
        	this.data = data;
        }
        parsed = true;
    }

    public void rlpParse(){

    	RLPList decodedTxList = RLP.decode2(rlpEncoded);
    	RLPList transaction =  (RLPList) decodedTxList.get(0);

    	this.hash  = HashUtil.sha3(rlpEncoded);
    	
    	/* Temporary order for an RLP encoded transaction in cpp client */ 
        this.nonce =          ((RLPItem) transaction.get(0)).getRLPData();
        this.gasPrice =       ((RLPItem) transaction.get(1)).getRLPData();
        this.gasLimit =       ((RLPItem) transaction.get(2)).getRLPData();
        this.receiveAddress = ((RLPItem) transaction.get(3)).getRLPData();
        this.value =          ((RLPItem) transaction.get(4)).getRLPData();
        this.data =           ((RLPItem) transaction.get(5)).getRLPData();
    	
        /* Order of the Yellow Paper / eth-go & pyethereum clients
        this.nonce =          ((RLPItem) transaction.get(0)).getRLPData();
        this.value =          ((RLPItem) transaction.get(1)).getRLPData();
        this.receiveAddress = ((RLPItem) transaction.get(2)).getRLPData();
        this.gasPrice =       ((RLPItem) transaction.get(3)).getRLPData();
        this.gasLimit =       ((RLPItem) transaction.get(4)).getRLPData();
        this.data =           ((RLPItem) transaction.get(5)).getRLPData();
        */

        if (transaction.size() == 9){  // Simple transaction
        	byte v =		((RLPItem) transaction.get(6)).getRLPData()[0];
            byte[] r =		((RLPItem) transaction.get(7)).getRLPData();
            byte[] s =		((RLPItem) transaction.get(8)).getRLPData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else if (transaction.size() == 10){ // Contract creation transaction
            this.init =     ((RLPItem) transaction.get(6)).getRLPData();
            byte v =		((RLPItem) transaction.get(7)).getRLPData()[0];
            byte[] r =		((RLPItem) transaction.get(8)).getRLPData();
            byte[] s =		((RLPItem) transaction.get(9)).getRLPData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else throw new RuntimeException("Wrong tx data element list size");
        this.parsed = true;
    }

    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {

        if (!parsed) rlpParse();
        byte[] plainMsg = this.getRlpUnsigned();

        return HashUtil.sha3(plainMsg);
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
		ECKey key = ECKey.fromPrivate(privKeyBytes).decompress();
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

    /**
     *  For signature games you have to keep also
     *  rlp of the transaction without any signature data
     */
    public byte[] getRlpUnsigned(){

        if (unsignedRLPEncoded != null) return unsignedRLPEncoded;

        byte[] nonce 				= RLP.encodeElement(this.nonce);
        byte[] gasPrice 			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit 			= RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress 		= RLP.encodeElement(this.receiveAddress);
        byte[] value 				= RLP.encodeElement(this.value);
        byte[] data 				= RLP.encodeElement(this.data);

        if(Arrays.equals(this.receiveAddress, new byte[0])) {
            byte[] init 			= RLP.encodeElement(this.init);
            this.unsignedRLPEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, value, receiveAddress,
                    data, init);
        } else {
            this.unsignedRLPEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, value, receiveAddress,
                    data);
        }

        return unsignedRLPEncoded;
    }

    public byte[] getEncoded() {

        if(rlpEncoded != null) return rlpEncoded;

        /* Temporary order for an RLP encoded transaction in cpp client */
        byte[] nonce 				= RLP.encodeElement(this.nonce);
        byte[] gasPrice 			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit 			= RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress 		= RLP.encodeElement(this.receiveAddress);
        byte[] value 				= RLP.encodeElement(this.value);
        byte[] data 				= RLP.encodeElement(this.data);

        byte[] v = null;
        byte[] r = null;
        byte[] s = null;


        v = RLP.encodeByte( signature.v );
        r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
        s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));

        if(Arrays.equals(this.receiveAddress, new byte[0])) {
            byte[] init 			= RLP.encodeElement(this.init);
            this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, value, receiveAddress,
                     data, init, v, r, s);
        } else {
            this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, value, receiveAddress,
                    data, v, r, s);
        }




        /* Order of the Yellow Paper / eth-go & pyethereum clients
        byte[] nonce			= RLP.encodeElement(this.nonce);
        byte[] value			= RLP.encodeElement(this.value);
        byte[] receiveAddress 	= RLP.encodeElement(this.receiveAddress);
        byte[] gasPrice			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit			= RLP.encodeElement(this.gasLimit);
        byte[] data				= RLP.encodeElement(this.data);
        byte[] init				= RLP.encodeElement(this.init);
        */

        return rlpEncoded;
    }
}
