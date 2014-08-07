package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

/**
 * Block header is a value object containing 
 * the basic information of a block 
 */
public class BlockHeader {
	
    /* The SHA3 256-bit hash of the parent block, in its entirety */
	private  byte[] parentHash;
    /* The SHA3 256-bit hash of the uncles list portion of this block */
    private byte[] unclesHash;
    /* The 160-bit address to which all fees collected from the 
     * successful mining of this block be transferred; formally */
    private byte[] coinbase;
    /* The SHA3 256-bit hash of the root node of the state trie, 
     * after all transactions are executed and finalisations applied */
    private byte[] stateRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure 
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_reciepe)]
     * of the block */
    private byte[] txTrieRoot;
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
    /* A scalar value equal to the minimum price of gas a transaction 
     * must have provided in order to be sufficient for inclusion 
     * by this miner in this block */
    private long minGasPrice;
    /* A scalar value equal to the current limit of gas expenditure per block */
    private long gasLimit;
    /* A scalar value equal to the total gas used in transactions in this block */
    private long gasUsed;
    /* An arbitrary byte array containing data relevant to this block. 
     * With the exception of the genesis block, this must be 32 bytes or fewer */
    private byte[] extraData;
    /* A 256-bit hash which proves that a sufficient amount 
     * of computation has been carried out on this block */
    private byte[] nonce;
    
    public BlockHeader(RLPList rlpHeader) {
        this.parentHash     = ((RLPItem) rlpHeader.get(0)).getRLPData();
        this.unclesHash     = ((RLPItem) rlpHeader.get(1)).getRLPData();
        this.coinbase       = ((RLPItem) rlpHeader.get(2)).getRLPData();
        this.stateRoot      = ((RLPItem) rlpHeader.get(3)).getRLPData();
        this.txTrieRoot     = ((RLPItem) rlpHeader.get(4)).getRLPData();
        this.difficulty     = ((RLPItem) rlpHeader.get(5)).getRLPData();

        byte[] nrBytes      = ((RLPItem) rlpHeader.get(6)).getRLPData();
        byte[] gpBytes      = ((RLPItem) rlpHeader.get(7)).getRLPData();
        byte[] glBytes      = ((RLPItem) rlpHeader.get(8)).getRLPData();
        byte[] guBytes      = ((RLPItem) rlpHeader.get(9)).getRLPData();
        byte[] tsBytes      = ((RLPItem) rlpHeader.get(10)).getRLPData();
        
        this.number 		= nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue();
        this.minGasPrice 	= gpBytes == null ? 0 : (new BigInteger(1, gpBytes)).longValue();
        this.gasLimit 		= glBytes == null ? 0 : (new BigInteger(1, glBytes)).longValue();
        this.gasUsed 		= guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue();
        this.timestamp      = tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue();
        
        this.extraData       = ((RLPItem) rlpHeader.get(11)).getRLPData();
        this.nonce           = ((RLPItem) rlpHeader.get(12)).getRLPData();
    }
    
	public BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
			byte[] difficulty, long number, long minGasPrice, long gasLimit,
			long gasUsed, long timestamp, byte[] extraData, byte[] nonce) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.difficulty = difficulty;
        this.number = number;
        this.minGasPrice = minGasPrice;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.nonce = nonce;
    }
    
	public byte[] getParentHash() {
		return parentHash;
	}
	public void setParentHash(byte[] parentHash) {
		this.parentHash = parentHash;
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
	public void setTxTrieRoot(byte[] txTrieRoot) {
		this.txTrieRoot = txTrieRoot;
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
	public long getMinGasPrice() {
		return minGasPrice;
	}
	public void setMinGasPrice(long minGasPrice) {
		this.minGasPrice = minGasPrice;
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
	public byte[] getExtraData() {
		return extraData;
	}
	public void setExtraData(byte[] extraData) {
		this.extraData = extraData;
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
        byte[] parentHash		= RLP.encodeElement(this.parentHash);
        byte[] unclesHash		= RLP.encodeElement(this.unclesHash);
        byte[] coinbase			= RLP.encodeElement(this.coinbase);
        byte[] stateRoot		= RLP.encodeElement(this.stateRoot);
        byte[] txTrieRoot		= RLP.encodeElement(this.txTrieRoot);
        byte[] difficulty		= RLP.encodeElement(this.difficulty);
        byte[] number			= RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        byte[] minGasPrice		= RLP.encodeBigInteger(BigInteger.valueOf(this.minGasPrice));
        byte[] gasLimit			= RLP.encodeBigInteger(BigInteger.valueOf(this.gasLimit));
        byte[] gasUsed			= RLP.encodeBigInteger(BigInteger.valueOf(this.gasUsed));
        byte[] timestamp		= RLP.encodeBigInteger(BigInteger.valueOf(this.timestamp));
        byte[] extraData		= RLP.encodeElement(this.extraData);
        if(withNonce) {
        	byte[] nonce			= RLP.encodeElement(this.nonce);
        	return RLP.encodeList(parentHash, unclesHash, coinbase,
    				stateRoot, txTrieRoot, difficulty, number,
    				minGasPrice, gasLimit, gasUsed, timestamp, extraData, nonce);
        } else {
        	return RLP.encodeList(parentHash, unclesHash, coinbase,
    				stateRoot, txTrieRoot, difficulty, number,
    				minGasPrice, gasLimit, gasUsed, timestamp, extraData);
        }
	}
	
	private StringBuffer toStringBuff = new StringBuffer();
	
	public String toString() {

        toStringBuff.setLength(0);
        toStringBuff.append("  parentHash=" + ByteUtil.toHexString(parentHash)).append("\n");
        toStringBuff.append("  unclesHash=" + ByteUtil.toHexString(unclesHash)).append("\n");
        toStringBuff.append("  coinbase=" + ByteUtil.toHexString(coinbase)).append("\n");
        toStringBuff.append("  stateRoot=" 		+ ByteUtil.toHexString(stateRoot)).append("\n");
        toStringBuff.append("  txTrieHash=" 	+ ByteUtil.toHexString(txTrieRoot)).append("\n");
        toStringBuff.append("  difficulty=" 	+ ByteUtil.toHexString(difficulty)).append("\n");
        toStringBuff.append("  number=" 		+ number).append("\n");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("\n");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("\n");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("\n");
        toStringBuff.append("  timestamp=" 		+ timestamp + " (" + Utils.longToDateTime(timestamp) + ")").append("\n");
        toStringBuff.append("  extraData=" 		+ ByteUtil.toHexString(extraData)).append("\n");
        toStringBuff.append("  nonce=" 			+ ByteUtil.toHexString(nonce)).append("\n");
        return toStringBuff.toString();
	}
	
	public String toFlatString() {
        toStringBuff.append("  parentHash=" + ByteUtil.toHexString(parentHash)).append("");
        toStringBuff.append("  unclesHash=" + ByteUtil.toHexString(unclesHash)).append("");
        toStringBuff.append("  coinbase=" + ByteUtil.toHexString(coinbase)).append("");
        toStringBuff.append("  stateRoot=" 		+ ByteUtil.toHexString(stateRoot)).append("");
        toStringBuff.append("  txTrieHash=" 	+ ByteUtil.toHexString(txTrieRoot)).append("");
        toStringBuff.append("  difficulty=" 	+ ByteUtil.toHexString(difficulty)).append("");
        toStringBuff.append("  number=" 		+ number).append("");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("");
        toStringBuff.append("  timestamp=" 		+ timestamp).append("");
        toStringBuff.append("  extraData=" 		+ ByteUtil.toHexString(extraData)).append("");
        toStringBuff.append("  nonce=" 			+ ByteUtil.toHexString(nonce)).append("");
        return toStringBuff.toString();
	}

    public String toStylishString() {

        toStringBuff.setLength(0);
        toStringBuff.append(", <font color=\"${attribute_color}\"> parentHash</font>=" + ByteUtil.toHexString(parentHash)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> unclesHash</font>=" + ByteUtil.toHexString(unclesHash)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> coinbase</font>=" + ByteUtil.toHexString(coinbase)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> stateRoot</font>=" 		+ ByteUtil.toHexString(stateRoot)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> txTrieHash</font>=" 	+ ByteUtil.toHexString(txTrieRoot)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> difficulty</font>=" 	+ ByteUtil.toHexString(difficulty)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> number</font>=" 		+ number).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> minGasPrice</font>=" 	+ minGasPrice).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> gasLimit</font>=" 		+ gasLimit).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> gasUsed</font>=" 		+ gasUsed).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> timestamp</font>=" 		+ timestamp + " (" + Utils.longToDateTime(timestamp) + ")").append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> extraData</font>=" 		+ ByteUtil.toHexString(extraData)).append("<br/>");
        toStringBuff.append(", <font color=\"${attribute_color}\"> nonce</font>=" 			+ ByteUtil.toHexString(nonce)).append("<br/>");
        return toStringBuff.toString();
    }

}
