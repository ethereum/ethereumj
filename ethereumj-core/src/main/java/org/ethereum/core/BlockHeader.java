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

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;

/**
 * Block header is a value object containing
 * the basic information of a block
 */
final public class BlockHeader implements IBlockHeader {

    /** The SHA3 256-bit hash of the parent block, in its entirety */
    private byte[] parentHash;
    /* The SHA3 256-bit hash of the uncles list portion of this block */
    private byte[] unclesHash;
    /** The 160-bit address to which all fees collected from the
     * successful mining of this block be transferred; formally */
    private byte[] coinbase;
    /** The SHA3 256-bit hash of the root node of the state trie,
     * after all transactions are executed and finalisations applied */
    private byte[] stateRoot;
    /** The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction in the transaction
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] txTrieRoot;
    /** The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] receiptTrieRoot;

    /*todo: comment it when you know what the fuck it is*/
    private byte[] logsBloom;
    /** A scalar value corresponding to the difficulty level of this block.
     * This can be calculated from the previous block’s difficulty level
     * and the timestamp */
    private byte[] difficulty;
    /** A scalar value equal to the reasonable output of Unix's time()
     * at this block's inception */
    private long timestamp;
    /** A scalar value equal to the number of ancestor blocks.
     * The genesis block has a number of zero */
    private long number;
    /** A scalar value equal to the current limit of gas expenditure per block */
    private byte[] gasLimit;
    /** A scalar value equal to the total gas used in transactions in this block */
    private long gasUsed;


    private byte[] mixHash;

    /** An arbitrary byte array containing data relevant to this block.
     * With the exception of the genesis block, this must be 32 bytes or fewer */
    private byte[] extraData;
    /** A 256-bit hash which proves that a sufficient amount
     * of computation has been carried out on this block */
    private byte[] nonce;

    private byte[] hashCache;

    BlockHeader(byte... encoded) {
        this((RLPList) RLP.decode2(encoded).get(0));
    }

    BlockHeader(RLPList rlpHeader) {

        this.setParentHash(rlpHeader.get(0).getRLPData());
        this.setUnclesHash(rlpHeader.get(1).getRLPData());
        this.setCoinbase(rlpHeader.get(2).getRLPData());
        this.setStateRoot(rlpHeader.get(3).getRLPData());

        this.setTxTrieRoot(rlpHeader.get(4).getRLPData());
        if (this.getTxTrieRoot() == null)
            this.setTxTrieRoot(EMPTY_TRIE_HASH);

        this.setReceiptTrieRoot(rlpHeader.get(5).getRLPData());
        if (this.getReceiptTrieRoot() == null)
            this.setReceiptTrieRoot(EMPTY_TRIE_HASH);

        this.setLogsBloom(rlpHeader.get(6).getRLPData());
        this.setDifficulty(rlpHeader.get(7).getRLPData());

        byte[] nrBytes = rlpHeader.get(8).getRLPData();
        byte[] glBytes = rlpHeader.get(9).getRLPData();
        byte[] guBytes = rlpHeader.get(10).getRLPData();
        byte[] tsBytes = rlpHeader.get(11).getRLPData();

        this.setNumber(nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue());

        this.setGasLimit(glBytes);
        this.setGasUsed(guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue());
        this.setTimestamp(tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue());

        this.setExtraData(rlpHeader.get(12).getRLPData());
        this.setMixHash(rlpHeader.get(13).getRLPData());
        this.setNonce(rlpHeader.get(14).getRLPData());
    }

    BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                byte[] logsBloom, byte[] difficulty, long number,
                byte[] gasLimit, long gasUsed, long timestamp,
                byte[] mixHash, byte[] nonce, byte... extraData) {
        this.setParentHash(parentHash);
        this.setUnclesHash(unclesHash);
        this.setCoinbase(coinbase);
        this.setLogsBloom(logsBloom);
        this.setDifficulty(difficulty);
        this.setNumber(number);
        this.setGasLimit(gasLimit);
        this.setGasUsed(gasUsed);
        this.setTimestamp(timestamp);
        this.setExtraData(extraData);
        this.setMixHash(mixHash);
        this.setNonce(nonce);
        this.setStateRoot(HashUtil.EMPTY_TRIE_HASH);
    }

    @Override
    public boolean isGenesis() {
        return this.getNumber() == Genesis.NUMBER;
    }

    @Override
    public byte[] getParentHash() {
        return parentHash;
    }

    @Override
    public void setParentHash(byte[] parentHash) {
        this.parentHash = parentHash;
    }

    @Override
    public byte[] getUnclesHash() {
        return unclesHash;
    }

    @Override
    public void setUnclesHash(byte[] unclesHash) {
        this.unclesHash = unclesHash;
        hashCache = null;
    }

    @Override
    public byte[] getCoinbase() {
        return coinbase;
    }

    @Override
    public void setCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
        hashCache = null;
    }

    @Override
    public byte[] getStateRoot() {
        return stateRoot;
    }

    @Override
    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
        hashCache = null;
    }

    @Override
    public byte[] getTxTrieRoot() {
        return txTrieRoot;
    }

    @Override
    public void setTxTrieRoot(byte[] txTrieRoot) {
        this.txTrieRoot = txTrieRoot;
    }

    @Override
    public byte[] getReceiptsRoot() {
        return getReceiptTrieRoot();
    }

    @Override
    public void setReceiptsRoot(byte[] receiptTrieRoot) {
        this.setReceiptTrieRoot(receiptTrieRoot);
        hashCache = null;
    }

    @Override
    public void setTransactionsRoot(byte[] stateRoot) {
        this.setTxTrieRoot(stateRoot);
        hashCache = null;
    }

    @Override
    public byte[] getLogsBloom() {
        return logsBloom;
    }

    @Override
    public void setLogsBloom(byte[] logsBloom) {
        this.logsBloom = logsBloom;
        hashCache = null;
    }

    @Override
    public byte[] getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(byte[] difficulty) {
        this.difficulty = difficulty;
        hashCache = null;
    }

    @Override
    public BigInteger getDifficultyBI() {
        return new BigInteger(1, getDifficulty());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        hashCache = null;
    }

    @Override
    public long getNumber() {
        return number;
    }

    @Override
    public void setNumber(long number) {
        this.number = number;
        hashCache = null;
    }

    @Override
    public byte[] getGasLimit() {
        return gasLimit;
    }

    @Override
    public void setGasLimit(byte[] gasLimit) {
        this.gasLimit = gasLimit;
        hashCache = null;
    }

    @Override
    public long getGasUsed() {
        return gasUsed;
    }

    @Override
    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
        hashCache = null;
    }

    @Override
    public byte[] getMixHash() {
        return mixHash;
    }

    @Override
    public void setMixHash(byte[] mixHash) {
        this.mixHash = mixHash;
        hashCache = null;
    }

    @Override
    public byte[] getExtraData() {
        return extraData;
    }

    @Override
    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
        hashCache = null;
    }

    @Override
    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
        hashCache = null;
    }

    @Override
    public byte[] getHash() {
        if (hashCache == null) {
            hashCache = HashUtil.sha3(getEncoded());
        }
        return hashCache;
    }

    @Override
    public byte[] getEncoded() {
        return this.getEncoded(true); // with nonce
    }

    @Override
    public byte[] getEncodedWithoutNonce() {
        return this.getEncoded(false);
    }

    @Override
    public byte[] getEncoded(boolean withNonce) {
        byte[] parentHash = RLP.encodeElement(this.getParentHash());

        byte[] unclesHash = RLP.encodeElement(this.getUnclesHash());
        byte[] coinbase = RLP.encodeElement(this.getCoinbase());

        byte[] stateRoot = RLP.encodeElement(this.getStateRoot());

        if (getTxTrieRoot() == null) this.setTxTrieRoot(EMPTY_TRIE_HASH);
        byte[] txTrieRoot = RLP.encodeElement(this.getTxTrieRoot());

        if (getReceiptTrieRoot() == null) this.setReceiptTrieRoot(EMPTY_TRIE_HASH);
        byte[] receiptTrieRoot = RLP.encodeElement(this.getReceiptTrieRoot());

        byte[] logsBloom = RLP.encodeElement(this.getLogsBloom());
        byte[] difficulty = RLP.encodeBigInteger(new BigInteger(1, this.getDifficulty()));
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.getNumber()));
        byte[] gasLimit = RLP.encodeElement(this.getGasLimit());
        byte[] gasUsed = RLP.encodeBigInteger(BigInteger.valueOf(this.getGasUsed()));
        byte[] timestamp = RLP.encodeBigInteger(BigInteger.valueOf(this.getTimestamp()));

        byte[] extraData = RLP.encodeElement(this.getExtraData());
        if (withNonce) {
            byte[] mixHash = RLP.encodeElement(this.getMixHash());
            byte[] nonce = RLP.encodeElement(this.getNonce());
            return RLP.encodeList(parentHash, unclesHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, timestamp, extraData, mixHash, nonce);
        } else {
            return RLP.encodeList(parentHash, unclesHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, timestamp, extraData);
        }
    }

    @Override
    public byte[] getUnclesEncoded(List<IBlockHeader> uncleList) {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (IBlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    @Override
    public byte[] getPowBoundary() {
        return BigIntegers.asUnsignedByteArray(32, BigInteger.ONE.shiftLeft(256).divide(getDifficultyBI()));
    }

    public BigInteger calcDifficulty(BlockchainNetConfig config, BlockHeader parent) {
        return config.getConfigForBlock(getNumber()).
                calcDifficulty(this, parent);
    }

    public String toString() {
        return View.toStringWithSuffix(this, "\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IBlockHeader that = (IBlockHeader) o;
        return FastByteComparisons.equal(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getHash());
    }

    @Override
    public byte[] getReceiptTrieRoot() {
        return receiptTrieRoot;
    }

    @Override
    public void setReceiptTrieRoot(byte[] receiptTrieRoot) {
        this.receiptTrieRoot = receiptTrieRoot;
    }

}
