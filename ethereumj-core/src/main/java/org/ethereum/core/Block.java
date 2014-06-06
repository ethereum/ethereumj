package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Config;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.util.BigIntegers;

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

	/* A scalar value equal to the mininum limit of gas expenditure per block */
	private static long MIN_GAS_LIMIT = BigInteger.valueOf(10).pow(4).longValue();

	private byte[] rlpEncoded;
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

    private List<Transaction> transactionsList = new ArrayList<Transaction>();
    private List<TransactionRecipe> txRecipeList = new ArrayList<TransactionRecipe>();

    private List<Block> uncleList = new ArrayList<Block>();
    private Trie state;

    public Block(byte[] rawData) {
        this.rlpEncoded = rawData;
        this.parsed = false;
    }
    
	public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
			byte[] txTrieRoot, byte[] difficulty, long number,
			long minGasPrice, long gasLimit, long gasUsed, long timestamp,
			byte[] extraData, byte[] nonce, List<Transaction> transactionsList,
			List<Block> uncleList) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.state = new Trie(Config.STATE_DB.getDb());
        this.stateRoot = state.getRootHash();
        this.txTrieRoot = txTrieRoot;
        this.difficulty = difficulty;
        this.number = number;
        this.minGasPrice = minGasPrice;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.nonce = nonce;
        this.transactionsList = transactionsList;
        this.uncleList = uncleList;
        this.parsed = true;
    }

	// [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
	// difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,  
	// extradata, nonce]
    private void parseRLP() {

        RLPList params = (RLPList) RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);
        
        // Parse Header
        RLPList header = (RLPList) block.get(0);

        this.parentHash     = ((RLPItem) header.get(0)).getRLPData();
        this.unclesHash     = ((RLPItem) header.get(1)).getRLPData();
        this.coinbase       = ((RLPItem) header.get(2)).getRLPData();
        this.stateRoot      = ((RLPItem) header.get(3)).getRLPData();
        this.txTrieRoot     = ((RLPItem) header.get(4)).getRLPData();
        this.difficulty     = ((RLPItem) header.get(5)).getRLPData();

        byte[] nrBytes      = ((RLPItem) header.get(6)).getRLPData();
        byte[] gpBytes      = ((RLPItem) header.get(7)).getRLPData();
        byte[] glBytes      = ((RLPItem) header.get(8)).getRLPData();
        byte[] guBytes      = ((RLPItem) header.get(9)).getRLPData();
        byte[] tsBytes      = ((RLPItem) header.get(10)).getRLPData();
        
        this.number 		= nrBytes == null ? 0 : (new BigInteger(1, nrBytes)).longValue();
        this.minGasPrice 	= gpBytes == null ? 0 : (new BigInteger(1, gpBytes)).longValue();
        this.gasLimit 		= glBytes == null ? 0 : (new BigInteger(1, glBytes)).longValue();
        this.gasUsed 		= guBytes == null ? 0 : (new BigInteger(1, guBytes)).longValue();
        this.timestamp      = tsBytes == null ? 0 : (new BigInteger(1, tsBytes)).longValue();
        
        this.extraData       = ((RLPItem) header.get(11)).getRLPData();
        this.nonce           = ((RLPItem) header.get(12)).getRLPData();

        // Parse Transactions
        RLPList transactions = (RLPList) block.get(1);
        for (RLPElement rlpTx : transactions){

            RLPElement txData = ((RLPList)rlpTx).get(0);

            Transaction tx = new Transaction(txData.getRLPData());
            this.transactionsList.add(tx);

            // YP 4.3.1
            RLPElement cummGas    = ((RLPList)rlpTx).get(1);
            RLPElement pstTxState = ((RLPList)rlpTx).get(2);

            TransactionRecipe txRecipe =
                new TransactionRecipe(tx, cummGas.getRLPData(), pstTxState.getRLPData());
            txRecipeList.add(txRecipe);
        }

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks){
            Block blockData = new Block(rawUncle.getRLPData());
            this.uncleList.add(blockData);
        }
        this.parsed = true;
        this.hash  = this.getHash();
    }

    public byte[] getHash(){
        if (!parsed) parseRLP();
       	return HashUtil.sha3(this.getEncoded());
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
        return this.stateRoot;
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
    	if (!parsed) parseRLP();
		return number;
	}

	public long getMinGasPrice() {
		if (!parsed) parseRLP();
		return minGasPrice;
	}

	public long getGasLimit() {
		if (!parsed) parseRLP();
		return gasLimit;
	}

	public long getGasUsed() {
		if (!parsed) parseRLP();
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
        if (transactionsList == null) {
        	this.transactionsList = new ArrayList<Transaction>();
        }
        return transactionsList;
    }

    public List<TransactionRecipe> getTxRecipeList() {
        if (!parsed) parseRLP();
        if (transactionsList == null) {
            this.txRecipeList = new ArrayList<TransactionRecipe>();
        }
        return txRecipeList;
    }

    public List<Block> getUncleList() {
        if (!parsed) parseRLP();
        if (uncleList == null) {
        	this.uncleList = new ArrayList<Block>();
        }
        return uncleList;
    }

    private StringBuffer toStringBuff = new StringBuffer();
	// [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
	// difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,  
	// extradata, nonce]

    @Override
    public String toString() {
        if (!parsed) parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [\n");
        toStringBuff.append("  hash=" + ByteUtil.toHexString(hash)).append("\n");
        toStringBuff.append("  parentHash=" + ByteUtil.toHexString(parentHash)).append("\n");
        toStringBuff.append("  unclesHash=" + ByteUtil.toHexString(unclesHash)).append("\n");
        toStringBuff.append("  coinbase=" + ByteUtil.toHexString(coinbase)).append("\n");
        toStringBuff.append("  stateHash=" 		+ ByteUtil.toHexString(stateRoot)).append("\n");
        toStringBuff.append("  txTrieHash=" 	+ ByteUtil.toHexString(txTrieRoot)).append("\n");
        toStringBuff.append("  difficulty=" 	+ ByteUtil.toHexString(difficulty)).append("\n");
        toStringBuff.append("  number=" 		+ number).append("\n");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("\n");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("\n");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("\n");
        toStringBuff.append("  timestamp=" 		+ timestamp + " (" + Utils.longToDateTime(timestamp) + ")").append("\n");
        toStringBuff.append("  extraData=" 		+ ByteUtil.toHexString(extraData)).append("\n");
        toStringBuff.append("  nonce=" 			+ ByteUtil.toHexString(nonce)).append("\n");

        for (TransactionRecipe txRecipe : getTxRecipeList()){

            toStringBuff.append("\n");
            toStringBuff.append(txRecipe.toString());
        }

        toStringBuff.append("\n]");
        return toStringBuff.toString();
    }

    public String toFlatString(){
        if (!parsed) parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [");
        toStringBuff.append("  hash=" + ByteUtil.toHexString(hash)).append("");
        toStringBuff.append("  parentHash=" + ByteUtil.toHexString(parentHash)).append("");
        toStringBuff.append("  unclesHash=" + ByteUtil.toHexString(unclesHash)).append("");
        toStringBuff.append("  coinbase=" + ByteUtil.toHexString(coinbase)).append("");
        toStringBuff.append("  stateHash=" 		+ ByteUtil.toHexString(stateRoot)).append("");
        toStringBuff.append("  txTrieHash=" 	+ ByteUtil.toHexString(txTrieRoot)).append("");
        toStringBuff.append("  difficulty=" 	+ ByteUtil.toHexString(difficulty)).append("");
        toStringBuff.append("  number=" 		+ number).append("");
        toStringBuff.append("  minGasPrice=" 	+ minGasPrice).append("");
        toStringBuff.append("  gasLimit=" 		+ gasLimit).append("");
        toStringBuff.append("  gasUsed=" 		+ gasUsed).append("");
        toStringBuff.append("  timestamp=" 		+ timestamp).append("");
        toStringBuff.append("  extraData=" 		+ ByteUtil.toHexString(extraData)).append("");
        toStringBuff.append("  nonce=" 			+ ByteUtil.toHexString(nonce)).append("");

        for (Transaction tx : getTransactionsList()){

            toStringBuff.append("\n");
            toStringBuff.append( tx.toString() );
        }

        toStringBuff.append("]");
        return toStringBuff.toString();
    }
    
    public byte[] updateState(byte[] key, byte[] value) {
    	this.state.update(key, value);
    	return this.stateRoot = this.state.getRootHash();
    }
    
	/**
	 * This mechanism enforces a homeostasis in terms of the time between blocks; 
	 * a smaller period between the last two blocks results in an increase in the 
	 * difficulty level and thus additional computation required, lengthening the 
	 * likely next period. Conversely, if the period is too large, the difficulty, 
	 * and expected time to the next block, is reduced.
	 */
    private boolean isValid() {
    	boolean isValid = false;
    	
    	// verify difficulty meets requirements
    	isValid = this.getDifficulty() == this.calcDifficulty();
    	// verify gasLimit meets requirements
    	isValid = this.getGasLimit() == this.calcGasLimit();
    	// verify timestamp meets requirements
    	isValid = this.getTimestamp() > this.getParent().getTimestamp();
    	
    	return isValid;
    }
	
	/**
	 * Calculate GasLimit 
	 *  max(10000, (parent gas limit * (1024 - 1) + (parent gas used * 6 / 5)) / 1024)
	 *  
	 * @return
	 */
	public long calcGasLimit() {
		if (parentHash == null)
			return 1000000L;
		else {
			Block parent = this.getParent();
			return Math.max(MIN_GAS_LIMIT, (parent.gasLimit * (1024 - 1) + (parent.gasUsed * 6 / 5)) / 1024);
		}
	}
	
	public byte[] calcDifficulty() {
		if (parentHash == null)
			return Genesis.DIFFICULTY;
		else {
			Block parent = this.getParent();
			long parentDifficulty = new BigInteger(1, parent.difficulty).longValue();
			long newDifficulty = timestamp >= parent.timestamp + 42 ? parentDifficulty - (parentDifficulty >> 10) : (parentDifficulty + (parentDifficulty >> 10));
			return BigIntegers.asUnsignedByteArray(BigInteger.valueOf(newDifficulty));
		}
	}

	public byte[] getEncoded() {
		if(rlpEncoded == null) {

	        // TODO: Alternative clean way to encode, using RLP.encode() after it's optimized
	        // Object[] header = new Object[] { parentHash, unclesHash, coinbase,
	        // stateRoot, txTrieRoot, difficulty, number, minGasPrice,
	        // gasLimit, gasUsed, timestamp, extraData, nonce };
	        // Object[] transactions = this.getTransactionsList().toArray();
	        // Object[] uncles = this.getUncleList().toArray();        
	        // return RLP.encode(new Object[] { header, transactions, uncles });

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
	        byte[] nonce			= RLP.encodeElement(this.nonce);
	
	        byte[] header = RLP.encodeList(parentHash, unclesHash, coinbase,
					stateRoot, txTrieRoot, difficulty, number,
					minGasPrice, gasLimit, gasUsed, timestamp, extraData, nonce);
	        
	        byte[] transactions = RLP.encodeList();
	        byte[] uncles = RLP.encodeList();       
	        
	        this.rlpEncoded = RLP.encodeList(header, transactions, uncles);
		}
		return rlpEncoded;
	}
}
