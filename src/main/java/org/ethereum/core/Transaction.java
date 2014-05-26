package org.ethereum.core;

import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.ECKey.MissingPrivateKeyException;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * A transaction (formally, T ) is a single cryptographically 
 * signed instruction sent by an actor external to Ethereum. 
 * An external actor can be a person (via a mobile device or desktop computer) 
 * or could be from a piece of automated software running on a server. 
 * There are two types of transactions: those which result in message calls 
 * and those which result in the creation of new contracts.
 */
public class Transaction {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
    private static final int CALL_SIZE = 9;
    private static final int CONTRACT_SIZE = 10;
    public static final byte[] ZERO_ADDRESS = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
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

    /* Tx in encoded form */
    private byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed 
     * from the rlp-encoded data */
    private boolean parsed = false;

    public Transaction(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }

    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.receiveAddress = receiveAddress;
        this.value = value;
        if(receiveAddress == null || receiveAddress.length == 0) {
            this.receiveAddress = ZERO_ADDRESS;
            this.init = data;
        } else {
            this.data = data;
        }
        parsed = true;
    }

    public void rlpParse(){

        RLPList decodedTxList = RLP.decode2(rlpEncoded);
        RLPList transaction =  (RLPList) decodedTxList.get(0);

        this.nonce =          ((RLPItem) transaction.get(0)).getRLPData();
        this.gasPrice =       ((RLPItem) transaction.get(1)).getRLPData();
        this.gasLimit =       ((RLPItem) transaction.get(2)).getRLPData();
        this.receiveAddress = ((RLPItem) transaction.get(3)).getRLPData();
        this.value =          ((RLPItem) transaction.get(4)).getRLPData();


        if (isContract()){  // Simple transaction

            this.init =           ((RLPItem) transaction.get(5)).getRLPData();
        	// only parse signature in case tx is signed
        	if(((RLPItem) transaction.get(6)).getRLPData() != null) {
        		byte v =		((RLPItem) transaction.get(6)).getRLPData()[0];
                byte[] r =		((RLPItem) transaction.get(7)).getRLPData();
                byte[] s =		((RLPItem) transaction.get(8)).getRLPData();
                this.signature = ECDSASignature.fromComponents(r, s, v);
        	} else {
        		logger.debug("RLP encoded tx is not signed!");
        	}        	
        } else { // Contract creation transaction
            this.init =     ((RLPItem) transaction.get(5)).getRLPData();
            // only parse signature in case tx is signed
            if(((RLPItem) transaction.get(6)).getRLPData() != null) {
            	byte v =		((RLPItem) transaction.get(6)).getRLPData()[0];
                byte[] r =		((RLPItem) transaction.get(7)).getRLPData();
                byte[] s =		((RLPItem) transaction.get(8)).getRLPData();
                this.signature = ECDSASignature.fromComponents(r, s, v);
            } else {
            	logger.debug("RLP encoded tx is not signed!");
            }
        }
        this.parsed = true;
        this.hash  = this.getHash();
    }

    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {

        if (!parsed) rlpParse();
        byte[] plainMsg = this.getEncodedRaw();
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

    public byte[] getContractAddress(){

        if (!isContract()) return null;

        byte[] val1 = RLP.encodeElement(getSender());
        byte[] val2 = RLP.encodeElement(nonce);
        byte[] val  = HashUtil.sha3omit12(RLP.encodeList(val1, val2));

        return val;
    }

    public boolean isContract() {
        return Arrays.equals(this.receiveAddress, ZERO_ADDRESS);
    }

    /*********
     * Crypto
     */

    public ECKey getKey() {
        byte[] hash = this.getHash();
        return ECKey.recoverFromSignature(signature.v, signature, hash, true);
    }

    public byte[] getSender() {
		try {
			ECKey key = ECKey.signatureToKey(getHash(), getSignature().toBase64());
			return key.getAddress();
		} catch (SignatureException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
    }

    public void sign(byte[] privKeyBytes) throws MissingPrivateKeyException {
        byte[] hash = this.getHash();
        ECKey key = ECKey.fromPrivate(privKeyBytes).decompress();
        this.signature = key.sign(hash);
        this.rlpEncoded = null;
    }

    @Override
    public String toString() {
        if (!parsed) rlpParse();
        return "TransactionData [" +  " hash=" + ByteUtil.toHexString(hash) +
                "  nonce=" + ByteUtil.toHexString(nonce) +
                ", gasPrice=" + ByteUtil.toHexString(gasPrice) +
                ", gas=" + ByteUtil.toHexString(gasLimit) +
                ", receiveAddress=" + ByteUtil.toHexString(receiveAddress) +
                ", value=" + ByteUtil.toHexString(value) +
                ", data=" + ByteUtil.toHexString(data) +
                ", init=" + ByteUtil.toHexString(init) +
                ", signatureV=" + signature.v +
                ", signatureR=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r)) +
                ", signatureS=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s)) +
                ']';
    }

    /**
     *  For signature games you have to keep also
     *  rlp of the transaction without any signature data
     */
    public byte[] getEncodedRaw(){

        if (!parsed) rlpParse();
        if (rlpRaw != null) return rlpRaw;

        byte[] nonce 				= RLP.encodeElement(this.nonce);
        byte[] gasPrice 			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit 			= RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress 		= RLP.encodeElement(this.receiveAddress);
        byte[] value 				= RLP.encodeElement(this.value);
        byte[] data 				= RLP.encodeElement(this.data);

        if(isContract()) {
            byte[] init 			= RLP.encodeElement(this.init);
            this.rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value,
                     init);
        } else {
            this.rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value,
                    data);
        }
        return rlpRaw;
    }

    public byte[] getEncoded() {

        if(rlpEncoded != null) return rlpEncoded;

        byte[] nonce 				= RLP.encodeElement(this.nonce);
        byte[] gasPrice 			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit 			= RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress 		= RLP.encodeElement(this.receiveAddress);
        byte[] value 				= RLP.encodeElement(this.value);
        byte[] data 				= RLP.encodeElement(this.data);

        byte[] v, r, s;
        
        if(signature != null) {
            v = RLP.encodeByte( signature.v );
            r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
            s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
        } else {
        	v = RLP.encodeElement(new byte[0]);
        	r = RLP.encodeElement(new byte[0]);
        	s = RLP.encodeElement(new byte[0]);
        }

        if(isContract()) {
            byte[] init 			= RLP.encodeElement(this.init);
            this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value,
                    init, v, r, s);
        } else {
            this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress, value,
                    data, v, r, s);
        }
        return rlpEncoded;
    }


}