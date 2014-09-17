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
 * A transaction (formally, T) is a single cryptographically 
 * signed instruction sent by an actor external to Ethereum. 
 * An external actor can be a person (via a mobile device or desktop computer) 
 * or could be from a piece of automated software running on a server. 
 * There are two types of transactions: those which result in message calls 
 * and those which result in the creation of new contracts.
 */
public class Transaction {

	private static Logger logger = LoggerFactory.getLogger(Transaction.class);
	
    /* SHA3 hash of the RLP encoded transaction */
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
     * input [data] of the message call or
     * Initialization code for a new contract */
    private byte[] data;

    /* the elliptic curve signature
     * (including public key recovery bits) */
    private ECDSASignature signature;

    /* Tx in encoded form */
    private byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed 
     * from the RLP-encoded data */
    private boolean parsed = false;

    public Transaction(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }

    /* creation contract tx
     * [ nonce, gasPrice, gasLimit, "", endowment, init, signature(v, r, s) ]
     * or simple send tx
     * [ nonce, gasPrice, gasLimit, receiveAddress, value, data, signature(v, r, s) ]
     */
    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.receiveAddress = receiveAddress;
        this.value = value;
        this.data = data;

        if(receiveAddress == null) {
            this.receiveAddress = ByteUtil.EMPTY_BYTE_ARRAY;
        }
        parsed = true;
    }

    public void rlpParse() {

        RLPList decodedTxList = RLP.decode2(rlpEncoded);
        RLPList transaction =  (RLPList) decodedTxList.get(0);

        this.nonce =          ((RLPItem) transaction.get(0)).getRLPData();
        this.gasPrice =       ((RLPItem) transaction.get(1)).getRLPData();
        this.gasLimit =       ((RLPItem) transaction.get(2)).getRLPData();
        this.receiveAddress = ((RLPItem) transaction.get(3)).getRLPData();
        this.value =          ((RLPItem) transaction.get(4)).getRLPData();

        this.data =     ((RLPItem) transaction.get(5)).getRLPData();
        // only parse signature in case tx is signed
        if(((RLPItem) transaction.get(6)).getRLPData() != null) {
            byte v =		((RLPItem) transaction.get(6)).getRLPData()[0];
            byte[] r =		((RLPItem) transaction.get(7)).getRLPData();
            byte[] s =		((RLPItem) transaction.get(8)).getRLPData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else {
            logger.debug("RLP encoded tx is not signed!");
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

        if (nonce == null) return new byte[]{0};
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

    // TODO: performance improve multiply without BigInteger
    public BigInteger getTotalGasValueDebit() {
		return new BigInteger(1, gasLimit).multiply(new BigInteger(1, gasPrice));
    }

    public byte[] getData() {
        if (!parsed) rlpParse();
        return data;
    }

    public ECDSASignature getSignature() {
        if (!parsed) rlpParse();
        return signature;
    }

    public byte[] getContractAddress() {

        if (!isContractCreation()) return null;
        return HashUtil.calcNewAddr(this.getSender(), this.getNonce());
    }

    public boolean isContractCreation() {
        return Arrays.equals(this.receiveAddress, ByteUtil.EMPTY_BYTE_ARRAY);
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
                ", signatureV=" + signature.v +
                ", signatureR=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r)) +
                ", signatureS=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s)) +
                " ]";
    }

    public String toStylishString() {
        if (!parsed) rlpParse();
        return " <font color=\"${sub_header_color}\"> TransactionData </font>[" +  "<font color=\"${attribute_color}\"> hash</font>=" + ByteUtil.toHexString(hash) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> nonce</font>=" + ByteUtil.toHexString(nonce) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> gasPrice</font>=" + ByteUtil.toHexString(gasPrice) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> gas</font>=" + ByteUtil.toHexString(gasLimit) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> receiveAddress</font>=" + ByteUtil.toHexString(receiveAddress) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> value</font>=" + ByteUtil.toHexString(value) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> data</font>=" + ByteUtil.toHexString(data) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> signatureV</font>=" + signature.v + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> signatureR</font>=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r)) + "<br/>" +
                "->  , <font color=\"${attribute_color}\"> signatureS</font>=" + ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s)) + "<br/>" +
                " ]";
    }


    /**
     *  For signatures you have to keep also
     *  RLP of the transaction without any signature data
     */
    public byte[] getEncodedRaw() {

        if (!parsed) rlpParse();
        if (rlpRaw != null) return rlpRaw;

        byte[] nonce 				= RLP.encodeElement(this.nonce);
        byte[] gasPrice 			= RLP.encodeElement(this.gasPrice);
        byte[] gasLimit 			= RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress 		= RLP.encodeElement(this.receiveAddress);
        byte[] value 				= RLP.encodeElement(this.value);
        byte[] data 				= RLP.encodeElement(this.data);

		this.rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress,
				value, data);
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

		this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit,
				receiveAddress, value, data, v, r, s);
        return rlpEncoded;
    }

    @Override
    public int hashCode() {

        byte[] hash = this.getHash();
        int hashCode = 0;

        for (int i = 0; i < hash.length; ++i){
            hashCode += hash[i] * i;
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Transaction)) return false;
        Transaction tx = (Transaction)obj;

        return tx.hashCode() == this.hashCode();
    }
}