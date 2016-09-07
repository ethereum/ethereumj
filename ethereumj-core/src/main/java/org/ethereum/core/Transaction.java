package org.ethereum.core;

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.crypto.ECKey.MissingPrivateKeyException;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.ZERO_BYTE_ARRAY;

/**
 * A transaction (formally, T) is a single cryptographically
 * signed instruction sent by an actor external to Ethereum.
 * An external actor can be a person (via a mobile device or desktop computer)
 * or could be from a piece of automated software running on a server.
 * There are two types of transactions: those which result in message calls
 * and those which result in the creation of new contracts.
 */
public class Transaction {

    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private static final BigInteger DEFAULT_GAS_PRICE = new BigInteger("10000000000000");
    private static final BigInteger DEFAULT_BALANCE_GAS = new BigInteger("21000");

    /* SHA3 hash of the RLP encoded transaction */
    private byte[] hash;

    /* a counter used to make sure each transaction can only be processed once */
    protected byte[] nonce;

    /* the amount of ether to transfer (calculated as wei) */
    protected byte[] value;

    /* the address of the destination account
     * In creation transaction the receive address is - 0 */
    protected byte[] receiveAddress;

    /* the amount of ether to pay as a transaction fee
     * to the miner for each unit of gas */
    protected byte[] gasPrice;

    /* the amount of "gas" to allow for the computation.
     * Gas is the fuel of the computational engine;
     * every computational step taken and every byte added
     * to the state or transaction list consumes some gas. */
    protected byte[] gasLimit;

    /* An unlimited size byte array specifying
     * input [data] of the message call or
     * Initialization code for a new contract */
    protected byte[] data;

    /* the elliptic curve signature
     * (including public key recovery bits) */
    private ECDSASignature signature;

    protected byte[] sendAddress;

    /* Tx in encoded form */
    protected byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed
     * from the RLP-encoded data */
    protected boolean parsed = false;

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

        if (receiveAddress == null) {
            this.receiveAddress = ByteUtil.EMPTY_BYTE_ARRAY;
        }

        parsed = true;
    }

    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data, byte[] r, byte[] s, byte v) {
        this(nonce, gasPrice, gasLimit, receiveAddress, value, data);
        this.signature = ECDSASignature.fromComponents(r, s, v);
    }

    public long transactionCost(BlockchainNetConfig config, Block block){

        if (!parsed) rlpParse();

        return config.getConfigForBlock(block.getNumber()).
                getTransactionCost(this);
    }

    public void rlpParse() {

        RLPList decodedTxList = RLP.decode2(rlpEncoded);
        RLPList transaction = (RLPList) decodedTxList.get(0);

        this.nonce = transaction.get(0).getRLPData();
        this.gasPrice = transaction.get(1).getRLPData();
        this.gasLimit = transaction.get(2).getRLPData();
        this.receiveAddress = transaction.get(3).getRLPData();
        this.value = transaction.get(4).getRLPData();
        this.data = transaction.get(5).getRLPData();
        // only parse signature in case tx is signed
        if (transaction.get(6).getRLPData() != null) {
            byte v = transaction.get(6).getRLPData()[0];
            byte[] r = transaction.get(7).getRLPData();
            byte[] s = transaction.get(8).getRLPData();
            this.signature = ECDSASignature.fromComponents(r, s, v);
        } else {
            logger.debug("RLP encoded tx is not signed!");
        }
        this.parsed = true;
        this.hash = getHash();
    }

    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {
        if (!isEmpty(hash)) return hash;

        if (!parsed) rlpParse();
        byte[] plainMsg = this.getEncoded();
        return HashUtil.sha3(plainMsg);
    }

    public byte[] getRawHash() {
        if (!parsed) rlpParse();
        byte[] plainMsg = this.getEncodedRaw();
        return HashUtil.sha3(plainMsg);
    }


    public byte[] getNonce() {
        if (!parsed) rlpParse();

        return nonce == null ? ZERO_BYTE_ARRAY : nonce;
    }

    public boolean isValueTx() {
        if (!parsed) rlpParse();
        return value != null;
    }

    public byte[] getValue() {
        if (!parsed) rlpParse();
        return value == null ? ZERO_BYTE_ARRAY : value;
    }

    public byte[] getReceiveAddress() {
        if (!parsed) rlpParse();
        return receiveAddress;
    }

    public byte[] getGasPrice() {
        if (!parsed) rlpParse();
        return gasPrice == null ? ZERO_BYTE_ARRAY : gasPrice;
    }

    public byte[] getGasLimit() {
        if (!parsed) rlpParse();
        return gasLimit;
    }

    public long nonZeroDataBytes() {
        if (data == null) return 0;
        int counter = 0;
        for (final byte aData : data) {
            if (aData != 0) ++counter;
        }
        return counter;
    }

    public long zeroDataBytes() {
        if (data == null) return 0;
        int counter = 0;
        for (final byte aData : data) {
            if (aData == 0) ++counter;
        }
        return counter;
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
        if (!parsed) rlpParse();
        return this.receiveAddress == null || Arrays.equals(this.receiveAddress,ByteUtil.EMPTY_BYTE_ARRAY);
    }

    /*
     * Crypto
     */

    public ECKey getKey() {
        byte[] hash = getRawHash();
        return ECKey.recoverFromSignature(signature.v, signature, hash);
    }

    public synchronized byte[] getSender() {
        try {
            if (sendAddress == null) {
                sendAddress = ECKey.signatureToAddress(getRawHash(), getSignature());
            }
            return sendAddress;
        } catch (SignatureException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @deprecated should prefer #sign(ECKey) over this method
     */
    public void sign(byte[] privKeyBytes) throws MissingPrivateKeyException {
        sign(ECKey.fromPrivate(privKeyBytes));
    }

    public void sign(ECKey key) throws MissingPrivateKeyException {
        this.signature = key.sign(this.getRawHash());
        this.rlpEncoded = null;
    }

    @Override
    public String toString() {
        return toString(Integer.MAX_VALUE);
    }

    public String toString(int maxDataSize) {
        if (!parsed) rlpParse();
        String dataS;
        if (data == null) {
            dataS = "";
        } else if (data.length < maxDataSize) {
            dataS = ByteUtil.toHexString(data);
        } else {
            dataS = ByteUtil.toHexString(Arrays.copyOfRange(data, 0, maxDataSize)) +
                    "... (" + data.length + " bytes)";
        }
        return "TransactionData [" + "hash=" + ByteUtil.toHexString(hash) +
                "  nonce=" + ByteUtil.toHexString(nonce) +
                ", gasPrice=" + ByteUtil.toHexString(gasPrice) +
                ", gas=" + ByteUtil.toHexString(gasLimit) +
                ", receiveAddress=" + ByteUtil.toHexString(receiveAddress) +
                ", value=" + ByteUtil.toHexString(value) +
                ", data=" + dataS +
                ", signatureV=" + (signature == null ? "" : signature.v) +
                ", signatureR=" + (signature == null ? "" : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.r))) +
                ", signatureS=" + (signature == null ? "" : ByteUtil.toHexString(BigIntegers.asUnsignedByteArray(signature.s))) +
                "]";
    }

    /**
     * For signatures you have to keep also
     * RLP of the transaction without any signature data
     */
    public byte[] getEncodedRaw() {

        if (!parsed) rlpParse();
        if (rlpRaw != null) return rlpRaw;

        // parse null as 0 for nonce
        byte[] nonce = null;
        if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
            nonce = RLP.encodeElement(null);
        } else {
            nonce = RLP.encodeElement(this.nonce);
        }
        byte[] gasPrice = RLP.encodeElement(this.gasPrice);
        byte[] gasLimit = RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress = RLP.encodeElement(this.receiveAddress);
        byte[] value = RLP.encodeElement(this.value);
        byte[] data = RLP.encodeElement(this.data);

        rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress,
                value, data);
        return rlpRaw;
    }

    public byte[] getEncoded() {

        if (rlpEncoded != null) return rlpEncoded;

        // parse null as 0 for nonce
        byte[] nonce = null;
        if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
            nonce = RLP.encodeElement(null);
        } else {
            nonce = RLP.encodeElement(this.nonce);
        }
        byte[] gasPrice = RLP.encodeElement(this.gasPrice);
        byte[] gasLimit = RLP.encodeElement(this.gasLimit);
        byte[] receiveAddress = RLP.encodeElement(this.receiveAddress);
        byte[] value = RLP.encodeElement(this.value);
        byte[] data = RLP.encodeElement(this.data);

        byte[] v, r, s;

        if (signature != null) {
            v = RLP.encodeByte(signature.v);
            r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
            s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
        } else {
            v = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        }

        this.rlpEncoded = RLP.encodeList(nonce, gasPrice, gasLimit,
                receiveAddress, value, data, v, r, s);

        this.hash = this.getHash();

        return rlpEncoded;
    }

    @Override
    public int hashCode() {

        byte[] hash = this.getHash();
        int hashCode = 0;

        for (int i = 0; i < hash.length; ++i) {
            hashCode += hash[i] * i;
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Transaction)) return false;
        Transaction tx = (Transaction) obj;

        return tx.hashCode() == this.hashCode();
    }

    public static Transaction createDefault(String to, BigInteger amount, BigInteger nonce){
        return create(to, amount, nonce, DEFAULT_GAS_PRICE, DEFAULT_BALANCE_GAS);
    }

    public static Transaction create(String to, BigInteger amount, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit){
        return new Transaction(BigIntegers.asUnsignedByteArray(nonce),
                BigIntegers.asUnsignedByteArray(gasPrice),
                BigIntegers.asUnsignedByteArray(gasLimit),
                Hex.decode(to),
                BigIntegers.asUnsignedByteArray(amount),
                null);
    }
}
