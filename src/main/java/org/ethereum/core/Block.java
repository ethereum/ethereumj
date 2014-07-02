package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The block in Ethereum is the collection of relevant pieces of information 
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known 
 * to have a parent equal to the present block’s parent’s parent 
 * (such blocks are known as uncles).
 *
 * www.ethereumJ.com
 * @authors: Roman Mandeleil,
 *           Nick Savers
 * Created on: 20/05/2014 10:44
 */
public class Block {

	private static Logger logger = LoggerFactory.getLogger(Block.class);
	
	/* A scalar value equal to the mininum limit of gas expenditure per block */
	private static long MIN_GAS_LIMIT = 125000L;
    public  static BigInteger coinbaseReward = BigInteger.valueOf(1500000000000000000L);

	private BlockHeader header;
	
    /* Transactions */     
	private List<TransactionReceipt> txReceiptList = new ArrayList<TransactionReceipt>();
	private List<Transaction> transactionsList = new ArrayList<Transaction>();
	
	/* Uncles */
    private List<Block> uncleList = new ArrayList<Block>();

    /* Private */ 	
	
	private byte[] rlpEncoded;
    private boolean parsed = false;
    
    private Trie txsState;
    
    /* Constructors */
    
    public Block(byte[] rawData) {
    	logger.debug("RLP encoded [ " + Hex.toHexString(rawData) + " ]");
        this.rlpEncoded = rawData;
        this.parsed = false;
    }
    
	public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
			byte[] difficulty, long number, long minGasPrice, long gasLimit,
			long gasUsed, long timestamp, byte[] extraData, byte[] nonce,
			List<Transaction> transactionsList, List<Block> uncleList) {
		this.header = new BlockHeader(parentHash, unclesHash, coinbase,
				difficulty, number, minGasPrice, gasLimit, gasUsed,
				timestamp, extraData, nonce);
        this.txsState = new Trie(null);

        byte[] stateRoot = WorldManager.instance.getRepository().getRootHash();
        this.header.setStateRoot(stateRoot);

        this.header.setTxTrieRoot(txsState.getRootHash());
        this.transactionsList = transactionsList;
        this.uncleList = uncleList;
        this.parsed = true;
    }

    private void parseRLP() {

        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);
        
        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.header = new BlockHeader(header);
        
        // Parse Transactions
        RLPList txReceipts = (RLPList) block.get(1);
        this.processTxs(txReceipts);

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks) {
            Block blockData = new Block(rawUncle.getRLPData());
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public byte[] getHash() {
        if (!parsed) parseRLP();
       	return HashUtil.sha3(this.getEncoded());
    }

    public Block getParent() {
		byte[] rlpEncoded = WorldManager.instance.chainDB.get(ByteUtil
				.longToBytes(this.getNumber() - 1));
    	return new Block(rlpEncoded);
    }
    
    public byte[] getParentHash() {
        if (!parsed) parseRLP();
        return this.header.getParentHash();
    }

    public byte[] getUnclesHash() {
        if (!parsed) parseRLP();
        return this.header.getUnclesHash();
    }

    public byte[] getCoinbase() {
        if (!parsed) parseRLP();
        return this.header.getCoinbase();
    }

    public byte[] getStateRoot() {
        if (!parsed) parseRLP();
        return this.header.getStateRoot();
    }
    
    public void setStateRoot(byte[] stateRoot) {
        if (!parsed) parseRLP();
        this.header.setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        if (!parsed) parseRLP();
        return this.header.getTxTrieRoot();
    }

    public byte[] getDifficulty() {
        if (!parsed) parseRLP();
        return this.header.getDifficulty();
    }

    public long getTimestamp() {
        if (!parsed) parseRLP();
        return this.header.getTimestamp();
    }
    
    public long getNumber() {
    	if (!parsed) parseRLP();
		return this.header.getNumber();
	}

	public long getMinGasPrice() {
		if (!parsed) parseRLP();
		return this.header.getMinGasPrice();
	}
	
	public boolean isGenesis() {
		return this.getNumber() == Genesis.NUMBER;
	}

	public long getGasLimit() {
		if (!parsed) parseRLP();
		return this.header.getGasLimit();
	}

	public long getGasUsed() {
		if (!parsed) parseRLP();
		return this.header.getGasUsed();
	}

	public byte[] getExtraData() {
        if (!parsed) parseRLP();
        return this.header.getExtraData();
    }

    public byte[] getNonce() {
        if (!parsed) parseRLP();
        return this.header.getNonce();
    }
    
    public void setNonce(byte[] nonce) {
        this.header.setNonce(nonce);
        rlpEncoded = null;
    }
    
    public Trie getTxsState() {
    	return this.txsState;
    }

    public List<Transaction> getTransactionsList() {
        if (!parsed) parseRLP();
        if (transactionsList == null) {
        	this.transactionsList = new ArrayList<>();
        }
        return transactionsList;
    }

    public List<TransactionReceipt> getTxReceiptList() {
        if (!parsed) parseRLP();
        if (transactionsList == null) {
            this.txReceiptList = new ArrayList<>();
        }
        return txReceiptList;
    }

    public List<Block> getUncleList() {
        if (!parsed) parseRLP();
        if (uncleList == null) {
        	this.uncleList = new ArrayList<>();
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
        toStringBuff.append(" hash=" + ByteUtil.toHexString(this.getHash())).append("\n");
        toStringBuff.append(header.toString());
        
        for (TransactionReceipt txReceipt : getTxReceiptList()) {
            toStringBuff.append("\n");
            toStringBuff.append(txReceipt.toString());
        }
        toStringBuff.append("\n ]");

        return toStringBuff.toString();
    }

    public String toFlatString() {
        if (!parsed) parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [");
        toStringBuff.append(" hash=" + ByteUtil.toHexString(this.getHash())).append("");
        toStringBuff.append(header.toFlatString());
        
        for (Transaction tx : getTransactionsList()) {

            toStringBuff.append("\n");
            toStringBuff.append( tx.toString() );
        }

        toStringBuff.append("]");
        return toStringBuff.toString();
    }
    
    private void processTxs(RLPList txReceipts) {

        this.txsState = new Trie(null);
        for (int i = 0; i < txReceipts.size(); i++) {
        	RLPElement rlpTxReceipt = txReceipts.get(i);
            RLPElement txData = ((RLPList)rlpTxReceipt).get(0);

            Transaction tx = new Transaction(txData.getRLPData());
            this.addAndProcessTransaction(i, tx);
            
            // YP 4.3.1
            RLPElement cummGas    = ((RLPList)rlpTxReceipt).get(1);
            RLPElement pstTxState = ((RLPList)rlpTxReceipt).get(2);

            TransactionReceipt txReceipt =
                new TransactionReceipt(tx, cummGas.getRLPData(), pstTxState.getRLPData());
            txReceiptList.add(txReceipt);
        }
        this.header.setTxTrieRoot(txsState.getRootHash());
    }
    
    private void addAndProcessTransaction(int counter, Transaction tx) {
        this.transactionsList.add(tx);
        this.txsState.update(RLP.encodeInt(counter), tx.getEncoded());
        
        /* Figure out type of tx
         * 1. Contract creation
         * 		- perform code
         * 		- create state object
         * 		- add contract body to DB, 
         * 2. Contract call			
         * 		- perform code
         * 		- update state object
         * 3. Account to account	- 
         * 		- update state object
         */
        
//        this.allAccountsState.update();
    }
    
	/**
	 * This mechanism enforces a homeostasis in terms of the time between blocks; 
	 * a smaller period between the last two blocks results in an increase in the 
	 * difficulty level and thus additional computation required, lengthening the 
	 * likely next period. Conversely, if the period is too large, the difficulty, 
	 * and expected time to the next block, is reduced.
	 */
    public boolean isValid() {
    	boolean isValid = true;

    	if(!this.isGenesis()) {
	    	// verify difficulty meets requirements
	    	isValid = this.getDifficulty() == this.calcDifficulty();
	    	isValid = this.validateNonce();
	    	// verify gasLimit meets requirements
	    	isValid = this.getGasLimit() == this.calcGasLimit();
	    	// verify timestamp meets requirements
	    	isValid = this.getTimestamp() > this.getParent().getTimestamp();
	    	// verify extraData doesn't exceed 1024 bytes
	    	isValid = this.getExtraData() == null || this.getExtraData().length <= 1024;
    	}
    	if(!isValid)
    		logger.warn("!!!Invalid block!!!");
    	return isValid;
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
			long newDifficulty = this.header.getTimestamp() >= parent.getTimestamp() + 42 ? parentDifficulty - (parentDifficulty >> 10) : (parentDifficulty + (parentDifficulty >> 10));
			return BigIntegers.asUnsignedByteArray(BigInteger.valueOf(newDifficulty));
		}
	}
	
	/**
	 * Verify that block is valid for its difficulty
	 * 
	 * @return
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

	public byte[] getEncoded() {
		if(rlpEncoded == null) {
			byte[] header = this.header.getEncoded();
	        byte[] transactions = RLP.encodeList();
	        byte[] uncles = RLP.encodeList();       
	        
	        this.rlpEncoded = RLP.encodeList(header, transactions, uncles);
		}
		return rlpEncoded;
	}
	
	public byte[] getEncodedWithoutNonce() {
		if (!parsed) parseRLP();
		byte[] header = this.header.getEncodedWithoutNonce();
        byte[] transactions = RLP.encodeList();
        byte[] uncles = RLP.encodeList();       
        
        return RLP.encodeList(header, transactions, uncles);
	}
}
