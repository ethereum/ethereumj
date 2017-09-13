/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.ZERO_BYTE_ARRAY;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.crypto.ECKey.MissingPrivateKeyException;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

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

    public static final int HASH_LENGTH = 32;
    public static final int ADDRESS_LENGTH = 20;

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

    /**
     * Since EIP-155, we could encode chainId in V
     */
    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;
    private Integer chainId = null;

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

    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       Integer chainId) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.receiveAddress = receiveAddress;
        if (ByteUtil.isSingleZero(value)) {
            this.value = EMPTY_BYTE_ARRAY;
        } else {
            this.value = value;
        }
        this.data = data;
        this.chainId = chainId;

        if (receiveAddress == null) {
            this.receiveAddress = ByteUtil.EMPTY_BYTE_ARRAY;
        }

        parsed = true;
    }

    /**
     * Warning: this transaction would not be protected by replay-attack protection mechanism
     * Use {@link Transaction#Transaction(byte[], byte[], byte[], byte[], byte[], byte[], Integer)} constructor instead
     * and specify the desired chainID
     */
    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data) {
        this(nonce, gasPrice, gasLimit, receiveAddress, value, data, null);
    }

    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       byte[] r, byte[] s, byte v, Integer chainId) {
        this(nonce, gasPrice, gasLimit, receiveAddress, value, data, chainId);
        this.signature = ECDSASignature.fromComponents(r, s, v);
    }

    /**
     * Warning: this transaction would not be protected by replay-attack protection mechanism
     * Use {@link Transaction#Transaction(byte[], byte[], byte[], byte[], byte[], byte[], byte[], byte[], byte, Integer)}
     * constructor instead and specify the desired chainID
     */
    public Transaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       byte[] r, byte[] s, byte v) {
        this(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, null);
    }


    private Integer extractChainIdFromV(BigInteger bv) {
        if (bv.bitLength() > 31) return Integer.MAX_VALUE; // chainId is limited to 31 bits, longer are not valid for now
        long v = bv.longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return null;
        return (int) ((v - CHAIN_ID_INC) / 2);
    }

    private byte getRealV(BigInteger bv) {
        if (bv.bitLength() > 31) return 0; // chainId is limited to 31 bits, longer are not valid for now
        long v = bv.longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return (byte) v;
        byte realV = LOWER_REAL_V;
        int inc = 0;
        if ((int) v % 2 == 0) inc = 1;
        return (byte) (realV + inc);
    }

    public long transactionCost(BlockchainNetConfig config, Block block){

        rlpParse();

        return config.getConfigForBlock(block.getNumber()).
                getTransactionCost(this);
    }

    public synchronized void verify() {
        rlpParse();
        validate();
    }

    public synchronized void rlpParse() {
        if (parsed) return;
        try {
            RLPList decodedTxList = RLP.decode2(rlpEncoded);
            RLPList transaction = (RLPList) decodedTxList.get(0);

            // Basic verification
            if (transaction.size() > 9 ) throw new RuntimeException("Too many RLP elements");
            for (RLPElement rlpElement : transaction) {
                if (!(rlpElement instanceof RLPItem))
                    throw new RuntimeException("Transaction RLP elements shouldn't be lists");
            }

            this.nonce = transaction.get(0).getRLPData();
            this.gasPrice = transaction.get(1).getRLPData();
            this.gasLimit = transaction.get(2).getRLPData();
            this.receiveAddress = transaction.get(3).getRLPData();
            this.value = transaction.get(4).getRLPData();
            this.data = transaction.get(5).getRLPData();
            // only parse signature in case tx is signed
            if (transaction.get(6).getRLPData() != null) {
                byte[] vData =  transaction.get(6).getRLPData();
                BigInteger v = ByteUtil.bytesToBigInteger(vData);
                this.chainId = extractChainIdFromV(v);
                byte[] r = transaction.get(7).getRLPData();
                byte[] s = transaction.get(8).getRLPData();
                this.signature = ECDSASignature.fromComponents(r, s, getRealV(v));
            } else {
                logger.debug("RLP encoded tx is not signed!");
            }
            this.parsed = true;
            this.hash = getHash();
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    private void validate() {
        if (getNonce().length > HASH_LENGTH) throw new RuntimeException("Nonce is not valid");
        if (receiveAddress != null && receiveAddress.length != 0 && receiveAddress.length != ADDRESS_LENGTH)
            throw new RuntimeException("Receive address is not valid");
        if (gasLimit.length > HASH_LENGTH)
            throw new RuntimeException("Gas Limit is not valid");
        if (gasPrice != null && gasPrice.length > HASH_LENGTH)
            throw new RuntimeException("Gas Price is not valid");
        if (value != null  && value.length > HASH_LENGTH)
            throw new RuntimeException("Value is not valid");
        if (getSignature() != null) {
            if (BigIntegers.asUnsignedByteArray(signature.r).length > HASH_LENGTH)
                throw new RuntimeException("Signature R is not valid");
            if (BigIntegers.asUnsignedByteArray(signature.s).length > HASH_LENGTH)
                throw new RuntimeException("Signature S is not valid");
            if (getSender() != null && getSender().length != ADDRESS_LENGTH)
                throw new RuntimeException("Sender is not valid");
        }
    }

    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {
        if (!isEmpty(hash)) return hash;

        rlpParse();
        byte[] plainMsg = this.getEncoded();
        return HashUtil.sha3(plainMsg);
    }

    public byte[] getRawHash() {
        rlpParse();
        byte[] plainMsg = this.getEncodedRaw();
        return HashUtil.sha3(plainMsg);
    }


    public byte[] getNonce() {
        rlpParse();

        return nonce == null ? ZERO_BYTE_ARRAY : nonce;
    }

    protected void setNonce(byte[] nonce) {
        this.nonce = nonce;
        parsed = true;
    }

    public boolean isValueTx() {
        rlpParse();
        return value != null;
    }

    public byte[] getValue() {
        rlpParse();
        return value == null ? ZERO_BYTE_ARRAY : value;
    }

    protected void setValue(byte[] value) {
        this.value = value;
        parsed = true;
    }

    public byte[] getReceiveAddress() {
        rlpParse();
        return receiveAddress;
    }

    protected void setReceiveAddress(byte[] receiveAddress) {
        this.receiveAddress = receiveAddress;
        parsed = true;
    }

    public byte[] getGasPrice() {
        rlpParse();
        return gasPrice == null ? ZERO_BYTE_ARRAY : gasPrice;
    }

    protected void setGasPrice(byte[] gasPrice) {
        this.gasPrice = gasPrice;
        parsed = true;
    }

    public byte[] getGasLimit() {
        rlpParse();
        return gasLimit == null ? ZERO_BYTE_ARRAY : gasLimit;
    }

    protected void setGasLimit(byte[] gasLimit) {
        this.gasLimit = gasLimit;
        parsed = true;
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
        rlpParse();
        return data;
    }

    protected void setData(byte[] data) {
        this.data = data;
        parsed = true;
    }

    public ECDSASignature getSignature() {
        rlpParse();
        return signature;
    }

    public byte[] getContractAddress() {
        if (!isContractCreation()) return null;
        return HashUtil.calcNewAddr(this.getSender(), this.getNonce());
    }

    public boolean isContractCreation() {
        rlpParse();
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
            if (sendAddress == null && getSignature() != null) {
                sendAddress = ECKey.signatureToAddress(getRawHash(), getSignature());
            }
            return sendAddress;
        } catch (SignatureException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Integer getChainId() {
        rlpParse();
        return chainId == null ? null : (int) chainId;
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
        rlpParse();
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
                ", sendAddress=" + ByteUtil.toHexString(getSender())  +
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

        rlpParse();
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

        // Since EIP-155 use chainId for v
        if (chainId == null) {
            rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress,
                    value, data);
        } else {
            byte[] v, r, s;
            v = RLP.encodeInt(chainId);
            r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
            rlpRaw = RLP.encodeList(nonce, gasPrice, gasLimit, receiveAddress,
                    value, data, v, r, s);
        }
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
            int encodeV;
            if (chainId == null) {
                encodeV = signature.v;
            } else {
                encodeV = signature.v - LOWER_REAL_V;
                encodeV += chainId * 2 + CHAIN_ID_INC;
            }
            v = RLP.encodeInt(encodeV);
            r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
            s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
        } else {
            // Since EIP-155 use chainId for v
            v = chainId == null ? RLP.encodeElement(EMPTY_BYTE_ARRAY) : RLP.encodeInt(chainId);
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

    /**
     * @deprecated Use {@link Transaction#createDefault(String, BigInteger, BigInteger, Integer)} instead
     */
    public static Transaction createDefault(String to, BigInteger amount, BigInteger nonce){
        return create(to, amount, nonce, DEFAULT_GAS_PRICE, DEFAULT_BALANCE_GAS);
    }

    public static Transaction createDefault(String to, BigInteger amount, BigInteger nonce, Integer chainId){
        return create(to, amount, nonce, DEFAULT_GAS_PRICE, DEFAULT_BALANCE_GAS, chainId);
    }

    /**
     * @deprecated use {@link Transaction#create(String, BigInteger, BigInteger, BigInteger, BigInteger, Integer)} instead
     */
    public static Transaction create(String to, BigInteger amount, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit){
        return new Transaction(BigIntegers.asUnsignedByteArray(nonce),
                BigIntegers.asUnsignedByteArray(gasPrice),
                BigIntegers.asUnsignedByteArray(gasLimit),
                Hex.decode(to),
                BigIntegers.asUnsignedByteArray(amount),
                null);
    }

    public static Transaction create(String to, BigInteger amount, BigInteger nonce, BigInteger gasPrice,
                                     BigInteger gasLimit, Integer chainId){
        return new Transaction(BigIntegers.asUnsignedByteArray(nonce),
                BigIntegers.asUnsignedByteArray(gasPrice),
                BigIntegers.asUnsignedByteArray(gasLimit),
                Hex.decode(to),
                BigIntegers.asUnsignedByteArray(amount),
                null,
                chainId);
    }
}
