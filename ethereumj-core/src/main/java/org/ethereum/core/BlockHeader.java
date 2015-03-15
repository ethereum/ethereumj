package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Block header is a value object containing
 * the basic information of a block
 */
public class BlockHeader {


    /* The SHA3 256-bit hash of the parent block, in its entirety */
    private byte[] parentHash;
    /* The SHA3 256-bit hash of the uncles list portion of this block */
    private byte[] unclesHash;
    /* The 160-bit address to which all fees collected from the
     * successful mining of this block be transferred; formally */
    private byte[] coinbase;
    /* The SHA3 256-bit hash of the root node of the state trie,
     * after all transactions are executed and finalisations applied */
    private byte[] stateRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction in the transaction
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] txTrieRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_recipe)]
     * of the block */
    private byte[] receiptTrieRoot;

    /*todo: comment it when you know what the fuck it is*/
    private byte[] logsBloom;
    /* A scalar value corresponding to the difficulty level of this block.
     * This can be calculated from the previous block’s difficulty level
     * and the timestamp */
    private byte[] difficulty;
    /* A scalar value equal to the reasonable output of Unix's time()
     * at this block's inception */
    private long timestamp;
    /* A scalar value equal to the number of ancestor blocks.
     * The genesis block has a number of zero */
    private long number;
    /* A scalar value equal to the current limit of gas expenditure per block */
    private long gasLimit;
    /* A scalar value equal to the total gas used in transactions in this block */
    private long gasUsed;


    private byte[] mixHash;

    /* An arbitrary byte array containing data relevant to this block.
     * With the exception of the genesis block, this must be 32 bytes or fewer */
    private byte[] extraData;
    /* A 256-bit hash which proves that a sufficient amount
     * of computation has been carried out on this block */
    private byte[] nonce;

    public BlockHeader(RLPList rlpHeader) {

        this.parentHash = rlpHeader.get(0).getRLPData();
        this.unclesHash = rlpHeader.get(1).getRLPData();
        this.coinbase = rlpHeader.get(2).getRLPData();
        this.stateRoot = rlpHeader.get(3).getRLPData();

        this.txTrieRoot = rlpHeader.get(4).getRLPData();
        if (this.txTrieRoot == null)
            this.txTrieRoot = EMPTY_TRIE_HASH;

        this.receiptTrieRoot = rlpHeader.get(5).getRLPData();
        if (this.receiptTrieRoot == null)
            this.receiptTrieRoot = EMPTY_TRIE_HASH;

        this.logsBloom = rlpHeader.get(6).getRLPData();
        this.difficulty = rlpHeader.get(7).getRLPData();

        byte[] nrBytes = rlpHeader.get(8).getRLPData();
        byte[] glBytes = rlpHeader.get(9).getRLPData();
        byte[] guBytes = rlpHeader.get(10).getRLPData();
        byte[] tsBytes = rlpHeader.get(11).getRLPData();

        this.number = nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue();

        this.gasLimit = glBytes == null ? 0 : (new BigInteger(1, glBytes)).longValue();
        this.gasUsed = guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue();
        this.timestamp = tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue();

        this.extraData = rlpHeader.get(12).getRLPData();
        this.mixHash = rlpHeader.get(13).getRLPData();
        this.nonce = rlpHeader.get(14).getRLPData();
    }

    public BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                       byte[] logsBloom, byte[] difficulty, long number,
                       long gasLimit, long gasUsed, long timestamp,
                       byte[] extraData, byte[] mixHash, byte[] nonce) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.difficulty = difficulty;
        this.number = number;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.mixHash = mixHash;
        this.nonce = nonce;
        this.stateRoot = HashUtil.EMPTY_TRIE_HASH;
    }


    /**
     * Calculate Difficulty
     * See Yellow Paper: http://www.gavwood.com/Paper.pdf - page 5, 4.3.4 (24)
     *
     * @return byte array value of the difficulty
     */
    public byte[] calcDifficulty() {
        if (this.isGenesis())
            return Genesis.DIFFICULTY;
        else { //todo find the right way to calc difficulty
//          Block parent = this.getParent();
//          long parentDifficulty = new BigInteger(1, parent.getDifficulty()).longValue();
//          long newDifficulty = this.getTimestamp() < parent.getTimestamp() + 5 ? parentDifficulty - (parentDifficulty >> 10) : (parentDifficulty + (parentDifficulty >> 10));
//          return BigIntegers.asUnsignedByteArray(BigInteger.valueOf(newDifficulty));
            return this.getDifficulty();
        }
    }


    public boolean isGenesis() {
        return this.getNumber() == Genesis.NUMBER;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public byte[] getUnclesHash() {
        return unclesHash;
    }

    public void setUnclesHash(byte[] unclesHash) {
        this.unclesHash = unclesHash;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(byte[] coinbase) {
        this.coinbase = coinbase;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    public byte[] getTxTrieRoot() {
        return txTrieRoot;
    }

    public void setReceiptsRoot(byte[] receiptTrieRoot) {
        this.receiptTrieRoot = receiptTrieRoot;
    }

    public byte[] getReceiptsRoot() {
        return receiptTrieRoot;
    }

    public void setTransactionsRoot(byte[] stateRoot) {
        this.txTrieRoot = stateRoot;
    }


    public byte[] getLogsBloom() {
        return logsBloom;
    }

    public byte[] getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(byte[] difficulty) {
        this.difficulty = difficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public byte[] getMixHash() {
        return mixHash;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public byte[] getEncoded() {
        return this.getEncoded(true); // with nonce
    }

    public byte[] getEncodedWithoutNonce() {
        return this.getEncoded(false);
    }

    public byte[] getEncoded(boolean withNonce) {
        byte[] parentHash = RLP.encodeElement(this.parentHash);

        byte[] unclesHash = RLP.encodeElement(this.unclesHash);
        byte[] coinbase = RLP.encodeElement(this.coinbase);

        byte[] stateRoot = RLP.encodeElement(this.stateRoot);

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot = RLP.encodeElement(this.txTrieRoot);

        if (receiptTrieRoot == null) this.receiptTrieRoot = EMPTY_TRIE_HASH;
        byte[] receiptTrieRoot = RLP.encodeElement(this.receiptTrieRoot);

        byte[] logsBloom = RLP.encodeElement(this.logsBloom);
        byte[] difficulty = RLP.encodeElement(this.difficulty);
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        byte[] gasLimit = RLP.encodeBigInteger(BigInteger.valueOf(this.gasLimit));
        byte[] gasUsed = RLP.encodeBigInteger(BigInteger.valueOf(this.gasUsed));
        byte[] timestamp = RLP.encodeBigInteger(BigInteger.valueOf(this.timestamp));

        byte[] extraData = RLP.encodeElement(this.extraData);
        byte[] mixHash = RLP.encodeElement(this.mixHash);
        if (withNonce) {
            byte[] nonce = RLP.encodeElement(this.nonce);
            return RLP.encodeList(parentHash, unclesHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, timestamp, extraData, mixHash, nonce);
        } else {
            return RLP.encodeList(parentHash, unclesHash, coinbase,
                    stateRoot, txTrieRoot, receiptTrieRoot, logsBloom, difficulty, number,
                    gasLimit, gasUsed, timestamp, extraData, mixHash);
        }
    }

    public String toString() {
        return toStringWithSuffix("\n");
    }

    private String toStringWithSuffix(final String suffix) {
        StringBuilder toStringBuff = new StringBuilder();
        toStringBuff.append("  parentHash=").append(toHexString(parentHash)).append(suffix);
        toStringBuff.append("  unclesHash=").append(toHexString(unclesHash)).append(suffix);
        toStringBuff.append("  coinbase=").append(toHexString(coinbase)).append(suffix);
        toStringBuff.append("  stateRoot=").append(toHexString(stateRoot)).append(suffix);
        toStringBuff.append("  txTrieHash=").append(toHexString(txTrieRoot)).append(suffix);
        toStringBuff.append("  receiptsTrieHash=").append(toHexString(receiptTrieRoot)).append(suffix);
        toStringBuff.append("  difficulty=").append(toHexString(difficulty)).append(suffix);
        toStringBuff.append("  number=").append(number).append(suffix);
        toStringBuff.append("  gasLimit=").append(gasLimit).append(suffix);
        toStringBuff.append("  gasUsed=").append(gasUsed).append(suffix);
        toStringBuff.append("  timestamp=").append(timestamp).append(" (").append(Utils.longToDateTime(timestamp)).append(")").append(suffix);
        toStringBuff.append("  extraData=").append(toHexString(extraData)).append(suffix);
        toStringBuff.append("  mixHash=").append(toHexString(mixHash)).append(suffix);
        toStringBuff.append("  nonce=").append(toHexString(nonce)).append(suffix);
        return toStringBuff.toString();
    }

    public String toFlatString() {
        return toStringWithSuffix("");
    }

}
