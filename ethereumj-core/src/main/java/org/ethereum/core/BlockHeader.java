package org.ethereum.core;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.*;

import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.*;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

/**
 * Block header is a value object containing 
 * the basic information of a block 
 */
public class BlockHeader {
	
	/* A scalar value equal to the mininum limit of gas expenditure per block */
	private static long MIN_GAS_LIMIT = 125000L;
	
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
     * populated with each transaction in the transaction
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_reciepe)]
     * of the block */
    private byte[] txTrieRoot;
    /* The SHA3 256-bit hash of the root node of the trie structure
     * populated with each transaction recipe in the transaction recipes
     * list portion, the trie is populate by [key, val] --> [rlp(index), rlp(tx_reciepe)]
     * of the block */
    private byte[] recieptTrieRoot;

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
        if(this.txTrieRoot == null)
        	this.txTrieRoot = EMPTY_TRIE_HASH;

        this.recieptTrieRoot     = ((RLPItem) rlpHeader.get(5)).getRLPData();
        if(this.recieptTrieRoot == null)
            this.recieptTrieRoot = EMPTY_TRIE_HASH;

        this.logsBloom      = ((RLPItem) rlpHeader.get(6)).getRLPData();
        this.difficulty     = ((RLPItem) rlpHeader.get(7)).getRLPData();
 
        byte[] nrBytes      = ((RLPItem) rlpHeader.get(8)).getRLPData();
        byte[] gpBytes      = ((RLPItem) rlpHeader.get(9)).getRLPData();
        byte[] glBytes      = ((RLPItem) rlpHeader.get(10)).getRLPData();
        byte[] guBytes      = ((RLPItem) rlpHeader.get(11)).getRLPData();
        byte[] tsBytes      = ((RLPItem) rlpHeader.get(12)).getRLPData();
        
        this.number 		= nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue();
        this.minGasPrice 	= gpBytes == null ? 0 : (new BigInteger(1, gpBytes)).longValue();
        this.gasLimit 		= glBytes == null ? 0 : (new BigInteger(1, glBytes)).longValue();
        this.gasUsed 		= guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue();
        this.timestamp      = tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue();
        
        this.extraData       = ((RLPItem) rlpHeader.get(13)).getRLPData();
        this.nonce           = ((RLPItem) rlpHeader.get(14)).getRLPData();

    }
    
	public BlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                       byte[]  logsBloom, byte[] difficulty, long number,
                       long minGasPrice, long gasLimit, long gasUsed, long timestamp,
                       byte[] extraData, byte[] nonce) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.logsBloom = logsBloom;
        this.difficulty = difficulty;
        this.number = number;
        this.minGasPrice = minGasPrice;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.nonce = nonce;
    }
	
	public boolean isValid() {
		boolean isValid = false;
    	// verify difficulty meets requirements
    	isValid = this.getDifficulty() == this.calcDifficulty();
    	isValid = this.validateNonce();
    	// verify gasLimit meets requirements
    	isValid = this.getGasLimit() == this.calcGasLimit();
    	// verify timestamp meets requirements
    	isValid = this.getTimestamp() > this.getParent().getTimestamp();
    	// verify extraData doesn't exceed 1024 bytes
    	isValid = this.getExtraData() == null || this.getExtraData().length <= 1024;
    	return isValid;
	}
    
	/**
	 * Calculate Difficulty 
	 * See Yellow Paper: http://www.gavwood.com/Paper.pdf - page 5, 4.3.4 (24)
	 * @return byte array value of the difficulty
	 */
	public byte[] calcDifficulty() {
		if (this.isGenesis())
			return Genesis.DIFFICULTY;
		else {
			Block parent = this.getParent();
			long parentDifficulty = new BigInteger(1, parent.getDifficulty()).longValue();
			long newDifficulty = this.getTimestamp() < parent.getTimestamp() + 5 ? parentDifficulty - (parentDifficulty >> 10) : (parentDifficulty + (parentDifficulty >> 10));
			return BigIntegers.asUnsignedByteArray(BigInteger.valueOf(newDifficulty));
		}
	}

	/**
	 * Calculate GasLimit 
	 * See Yellow Paper: http://www.gavwood.com/Paper.pdf - page 5, 4.3.4 (25)
	 * @return long value of the gasLimit
	 */
	public long calcGasLimit() {
		if (this.isGenesis())
			return Genesis.GAS_LIMIT;
		else {
			Block parent = this.getParent();
			return Math.max(MIN_GAS_LIMIT, (parent.getGasLimit() * (1024 - 1) + (parent.getGasUsed() * 6 / 5)) / 1024);
		}
	}
	
	/**
	 * Verify that block is valid for its difficulty
	 * 
	 * @return boolean
	 */
	public boolean validateNonce() {
		BigInteger max = BigInteger.valueOf(2).pow(256);
		byte[] target = BigIntegers.asUnsignedByteArray(32,
				max.divide(new BigInteger(1, this.getDifficulty())));
		byte[] hash = HashUtil.sha3(this.getEncodedWithoutNonce());
		byte[] concat = Arrays.concatenate(hash, this.getNonce());
		byte[] result = HashUtil.sha3(concat);
		return FastByteComparisons.compareTo(result, 0, 32, target, 0, 32) < 0;
	}

	public boolean isGenesis() {
		return this.getNumber() == Genesis.NUMBER;
	}
	
    public Block getParent() {
		return WorldManager.getInstance().getBlockchain().getBlockByNumber(this.getNumber() - 1);
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
    public byte[] getRecieptTrieRoot() {
        return recieptTrieRoot;
    }
    public void setRecieptTrieRoot(byte[] recieptTrieRoot) {
        this.recieptTrieRoot = recieptTrieRoot;
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

        if (txTrieRoot == null) this.txTrieRoot = EMPTY_TRIE_HASH;
        byte[] txTrieRoot	    = RLP.encodeElement(this.txTrieRoot);

        if (recieptTrieRoot == null) this.recieptTrieRoot = EMPTY_TRIE_HASH;
        byte[] recieptTrieRoot	= RLP.encodeElement(this.recieptTrieRoot);

        byte[] logsBloom        = RLP.encodeElement(this.logsBloom);
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
    				stateRoot, txTrieRoot, recieptTrieRoot, logsBloom, difficulty, number,
    				minGasPrice, gasLimit, gasUsed, timestamp, extraData, nonce);
        } else {
        	return RLP.encodeList(parentHash, unclesHash, coinbase,
    				stateRoot, txTrieRoot, recieptTrieRoot, logsBloom, difficulty, number,
    				minGasPrice, gasLimit, gasUsed, timestamp, extraData);
        }
	}
	
	private StringBuffer toStringBuff = new StringBuffer();
	
	public String toString() {

        toStringBuff.setLength(0);
        toStringBuff.append("  parentHash=" + toHexString(parentHash)).append("\n");
        toStringBuff.append("  unclesHash=" + toHexString(unclesHash)).append("\n");
        toStringBuff.append("  coinbase=" + toHexString(coinbase)).append("\n");
        toStringBuff.append("  stateRoot=" 		+ toHexString(stateRoot)).append("\n");
        toStringBuff.append("  txTrieHash=" 	+ toHexString(txTrieRoot)).append("\n");
        toStringBuff.append("  reciptsTrieHash=" 	+ toHexString(recieptTrieRoot)).append("\n");
        toStringBuff.append("  difficulty=" 	+ toHexString(difficulty)).append("\n");
        toStringBuff.append("  number=" 		+ number).append("\n");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("\n");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("\n");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("\n");
        toStringBuff.append("  timestamp=" 		+ timestamp + " (" + Utils.longToDateTime(timestamp) + ")").append("\n");
        toStringBuff.append("  extraData=" 		+ toHexString(extraData)).append("\n");
        toStringBuff.append("  nonce=" 			+ toHexString(nonce)).append("\n");
        return toStringBuff.toString();
	}
	
	public String toFlatString() {
        toStringBuff.append("  parentHash=" + toHexString(parentHash)).append("");
        toStringBuff.append("  unclesHash=" + toHexString(unclesHash)).append("");
        toStringBuff.append("  coinbase=" + toHexString(coinbase)).append("");
        toStringBuff.append("  stateRoot=" 		+ toHexString(stateRoot)).append("");
        toStringBuff.append("  txTrieHash=" 	+ toHexString(txTrieRoot)).append("");
        toStringBuff.append("  difficulty=" 	+ toHexString(difficulty)).append("");
        toStringBuff.append("  number=" 		+ number).append("");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("");
        toStringBuff.append("  timestamp=" 		+ timestamp).append("");
        toStringBuff.append("  extraData=" 		+ toHexString(extraData)).append("");
        toStringBuff.append("  nonce=" 			+ toHexString(nonce)).append("");
        return toStringBuff.toString();
	}

}
