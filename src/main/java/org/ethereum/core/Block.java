package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The block in Ethereum is the collection of relevant pieces of information 
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known 
 * to have a parent equal to the present block’s parent’s parent 
 * (such blocks are known as uncles).
 */
public class Block {

	private static int LIMIT_FACTOR = (int) Math.pow(2, 16);
	private static double EMA_FACTOR = 1.5;
	/* A scalar value equal to the current limit of gas expenditure per block */
	private static int GAS_LIMIT = (int) Math.pow(10, 6);

	private RLPList rawData;
	private byte[] encodedBlock;
    private boolean parsed = false;

    private byte[] hash;
    
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
     * populated with each transaction in the transactions list portion
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

    private List<Transaction> transactionsList = new ArrayList<Transaction>();
    private List<Block> uncleList = new ArrayList<Block>();

    public Block(RLPList rawData) {
        this.rawData = rawData;
        this.parsed = false;
    }
    
    public Block(byte[] encodedBlock) {
        this.encodedBlock = encodedBlock;
    }
    
	public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
			byte[] stateRoot, byte[] txTrieRoot, byte[] difficulty,
			long timestamp, long number, long minGasPrice, long gasLimit,
			long gasUsed, byte[] extraData, byte[] nonce,
			List<Transaction> transactionsList, List<Block> uncleList) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.stateRoot = stateRoot;
        this.txTrieRoot = txTrieRoot;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
        this.number = number;
        this.minGasPrice = minGasPrice;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.extraData = extraData;
        this.nonce = nonce;
        this.transactionsList = transactionsList;
        this.uncleList = uncleList;
        this.parsed = true;
    }

	// [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
	// difficulty, timestamp, number, minGasPrice, gasLimit, gasUsed, 
	// extradata, nonce]
    private void parseRLP() {

        this.hash = HashUtil.sha3(rawData.getRLPData());

        RLPList params = (RLPList) rawData.get(0);

        this.parentHash     = ((RLPItem) params.get(0)).getData();
        this.unclesHash     = ((RLPItem) params.get(1)).getData();
        this.coinbase       = ((RLPItem) params.get(2)).getData();
        this.stateRoot      = ((RLPItem) params.get(3)).getData();
        this.txTrieRoot     = ((RLPItem) params.get(4)).getData();
        this.difficulty     = ((RLPItem) params.get(5)).getData();

        byte[] tsBytes      = ((RLPItem) params.get(6)).getData();       
        byte[] nrBytes      = ((RLPItem) params.get(7)).getData();
        byte[] gpBytes      = ((RLPItem) params.get(8)).getData();
        byte[] glBytes      = ((RLPItem) params.get(9)).getData();
        byte[] guBytes      = ((RLPItem) params.get(10)).getData();

        this.timestamp      =  (new BigInteger(tsBytes)).longValue();
        this.number 		= (new BigInteger(nrBytes)).longValue();
        this.minGasPrice 	= (new BigInteger(gpBytes)).longValue();
        this.gasLimit 		= (new BigInteger(glBytes)).longValue();
        this.gasUsed 		= (new BigInteger(guBytes)).longValue();
        
        this.extraData       = ((RLPItem) params.get(11)).getData();
        this.nonce           = ((RLPItem) params.get(12)).getData();

        // parse transactions
        RLPList transactions = (RLPList) rawData.get(1);
        for (RLPElement rlpTx : transactions){
            Transaction tx = new Transaction((RLPList)rlpTx);
            this.transactionsList.add(tx);
        }
        // parse uncles
        RLPList uncleBlocks = (RLPList) rawData.get(2);
        for (RLPElement rawUncle : uncleBlocks){
            Block blockData = new Block((RLPList)rawUncle);
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public byte[] getHash(){
        if (!parsed) parseRLP();
        return hash;
    }

    public Block getParent() {
    	// TODO: Implement
    	return null;
    }
    
    public byte[] getParentHash() {
        if (!parsed) parseRLP();
        return parentHash;
    }

    public byte[] getUnclesHash() {
        if (!parsed) parseRLP();
        return unclesHash;
    }

    public byte[] getCoinbase() {
        if (!parsed) parseRLP();
        return coinbase;
    }

    public byte[] getStateRoot() {
        if (!parsed) parseRLP();
        return stateRoot;
    }

    public byte[] getTxTrieRoot() {
        if (!parsed) parseRLP();
        return txTrieRoot;
    }

    public byte[] getDifficulty() {
        if (!parsed) parseRLP();
        return difficulty;
    }

    public long getTimestamp() {
        if (!parsed) parseRLP();
        return timestamp;
    }
    
    public long getNumber() {
		return number;
	}

	public long getMinGasPrice() {
		return minGasPrice;
	}

	public long getGasLimit() {
		return gasLimit;
	}

	public long getGasUsed() {
		return gasUsed;
	}

	public byte[] getExtraData() {
        if (!parsed) parseRLP();
        return extraData;
    }

    public byte[] getNonce() {
        if (!parsed) parseRLP();
        return nonce;
    }

    public List<Transaction> getTransactionsList() {
        if (!parsed) parseRLP();
        return transactionsList;
    }

    public List<Block> getUncleList() {
        if (!parsed) parseRLP();
        return uncleList;
    }

	// [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
	// difficulty, timestamp, number, minGasPrice, gasLimit, gasUsed, 
	// extradata, nonce]
    @Override
    public String toString() {
        if (!parsed) parseRLP();

        return "BlockData [" +  
        		"  hash=" 			+ Utils.toHexString(hash) +
                "  parentHash=" 	+ Utils.toHexString(parentHash) +
                ", unclesHash=" 	+ Utils.toHexString(unclesHash) +
                ", coinbase=" 		+ Utils.toHexString(coinbase) +
                ", stateHash=" 		+ Utils.toHexString(stateRoot) +
                ", txTrieHash=" 	+ Utils.toHexString(txTrieRoot) +
                ", difficulty=" 	+ Utils.toHexString(difficulty) +
                ", timestamp=" 		+ timestamp +
                ", number=" 		+ number +
                ", minGasPrice=" 	+ minGasPrice +
                ", gasLimit=" 		+ gasLimit +
                ", gasUsed=" 		+ gasUsed +
                ", extraData=" 		+ Utils.toHexString(extraData) +
                ", nonce=" 			+ Utils.toHexString(nonce) +
                ']';
    }
    
	/**
	 * Because every transaction published into the blockchain imposes on the
	 * network the cost of needing to download and verify it, there is a need
	 * for some regulatory mechanism to prevent abuse.
	 * 
	 *  To solve this we simply institute a floating cap:
	 *   
	 *  	No block can have more operations than BLK_LIMIT_FACTOR times 
	 *  	the long-term exponential moving average. 
	 *  
	 *  Specifically:
	 *  
	 *  	blk.oplimit = floor((blk.parent.oplimit * (EMAFACTOR - 1) 
	 *  		+ floor(GAS_LIMIT * BLK_LIMIT_FACTOR)) / EMA_FACTOR)
	 * 
	 * BLK_LIMIT_FACTOR and EMA_FACTOR are constants that will be set 
	 * to 65536 and 1.5 for the time being, but will likely be changed 
	 * after further analysis.
	 * 
	 * @return
	 */
	public double getOplimit() {
		return Math.floor((this.getParent().getOplimit() * (EMA_FACTOR - 1) 
						+ Math.floor(GAS_LIMIT * LIMIT_FACTOR)) / EMA_FACTOR);
	}

	public byte[] getEncoded() {
		if (this.encodedBlock == null) parseRLP();
		return this.encodedBlock;
	}
	
	public byte[] hash() {
		return HashUtil.sha3(this.getEncoded());
	}
}
