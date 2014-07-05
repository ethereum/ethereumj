package org.ethereum.core;

import org.ethereum.db.DatabaseImpl;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.submit.WalletTransaction;
import org.ethereum.util.ByteUtil;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.*;

import static org.ethereum.core.Denomination.*;

/**
 * The Ethereum blockchain is in many ways similar to the Bitcoin blockchain, 
 * although it does have some differences. 
 * 
 * The main difference between Ethereum and Bitcoin with regard to the blockchain architecture 
 * is that, unlike Bitcoin, Ethereum blocks contain a copy of both the transaction list 
 * and the most recent state. Aside from that, two other values, the block number and 
 * the difficulty, are also stored in the block. 
 * 
 * The block validation algorithm in Ethereum is as follows:
 * <ol>
 * <li>Check if the previous block referenced exists and is valid.</li>
 * <li>Check that the timestamp of the block is greater than that of the referenced previous block and less than 15 minutes into the future</li>
 * <li>Check that the block number, difficulty, transaction root, uncle root and gas limit (various low-level Ethereum-specific concepts) are valid.</li>
 * <li>Check that the proof of work on the block is valid.</li>
 * <li>Let S[0] be the STATE_ROOT of the previous block.</li>
 * <li>Let TX be the block's transaction list, with n transactions. 
 * 	For all in in 0...n-1, set S[i+1] = APPLY(S[i],TX[i]). 
 * If any applications returns an error, or if the total gas consumed in the block 
 * up until this point exceeds the GASLIMIT, return an error.</li>
 * <li>Let S_FINAL be S[n], but adding the block reward paid to the miner.</li>
 * <li>Check if S_FINAL is the same as the STATE_ROOT. If it is, the block is valid; otherwise, it is not valid.</li>
 * </ol>
 * See <a href="https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-White-Paper#blockchain-and-mining">Ethereum Whitepaper</a>
 *
 *
 * www.ethereumJ.com
 * @authors: Roman Mandeleil,
 *           Nick Savers
 * Created on: 20/05/2014 10:44
 *
 */
public class Blockchain {

	private static Logger logger = LoggerFactory.getLogger("blockchain");
	
	// to avoid using minGasPrice=0 from Genesis for the wallet
	private static long INITIAL_MIN_GAS_PRICE = 10 * SZABO.longValue();
		
	private DatabaseImpl chainDb;
	private Wallet wallet;
	
    private long gasPrice = 1000;
    private Block lastBlock;

    // keep the index of the chain for
    // convenient usage, <block_number, block_hash>
    private Map<Long, byte[]> index = new HashMap<>();

    // This map of transaction designed
    // to approve the tx by external trusted peer
    private Map<String, WalletTransaction> walletTransactions =
            Collections.synchronizedMap(new HashMap<String, WalletTransaction>());

	public Blockchain() {
		this.chainDb = new DatabaseImpl("blockchain");
	}

	public Block getLastBlock() {
		return lastBlock;
	}

    public void setLastBlock(Block block) {
    	this.lastBlock = block;
    }

    public int getSize() {
        return index.size();
    }

    public Block getByNumber(long blockNr) {
        return new Block(chainDb.get(ByteUtil.longToBytes(blockNr)));
    }

    public void addBlocks(List<Block> blocks) {

		if (blocks.isEmpty())
			return;

        Block firstBlockToAdd = blocks.get(blocks.size() - 1);

        // if it is the first block to add
        // check that the parent is the genesis
		if (index.isEmpty()
				&& !Arrays.equals(StaticMessages.GENESIS_HASH,
						firstBlockToAdd.getParentHash())) {
			return;
		}
        // if there is some blocks already keep chain continuity
        if (!index.isEmpty()) {
            String hashLast = Hex.toHexString(getLastBlock().getHash());
            String blockParentHash = Hex.toHexString(firstBlockToAdd.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }
        for (int i = blocks.size() - 1; i >= 0 ; --i) {
            this.addBlock(blocks.get(i));
        }
        // Remove all wallet transactions as they already approved by the net
        for (Block block : blocks) {
            for (Transaction tx : block.getTransactionsList()) {
                if (logger.isDebugEnabled())
                    logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString( tx.getHash()));
                this.removeWalletTransaction(tx);
            }
        }
        logger.info("*** Block chain size: [ {} ]", this.getSize());
    }
    
    public void addBlock(Block block) {
    	if(block.isValid()) {

            if (!block.isGenesis())
                WorldManager.getInstance().applyBlock(block);

			this.chainDb.put(ByteUtil.longToBytes(block.getNumber()), block.getEncoded());
			this.index.put(block.getNumber(), block.getParentHash());
			
			this.wallet.processBlock(block);
			this.updateGasPrice(block);
			this.setLastBlock(block);
            if (logger.isDebugEnabled())
                logger.debug("block added to the chain with hash: {}", Hex.toHexString(block.getHash()));
    	} else {
    		logger.warn("Invalid block with nr: {}", block.getNumber());
    	}
    }
    
    public void updateGasPrice(Block block) {
        // In case of the genesis block we don't want to rely on the min gas price 
		this.gasPrice = block.isGenesis() ? block.getMinGasPrice() : INITIAL_MIN_GAS_PRICE;
    }
    
    public long getGasPrice() {
        return gasPrice;
    }

    /***********************************************************************
     *	1) the dialog put a pending transaction on the list
     *  2) the dialog send the transaction to a net
     *  3) wherever the transaction got in from the wire it will change to approve state
     *  4) only after the approve a) Wallet state changes
     *  5) After the block is received with that tx the pending been clean up
     */
    public WalletTransaction addWalletTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction placed hash: {}", hash );

        WalletTransaction walletTransaction =  this.walletTransactions.get(hash);
		if (walletTransaction != null)
			walletTransaction.incApproved();
		else {
			walletTransaction = new WalletTransaction(transaction);
			this.walletTransactions.put(hash, walletTransaction);
		}
        return walletTransaction;
    }

    public void removeWalletTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction removed with hash: {} ",  hash);
        walletTransactions.remove(hash);
    }
    
    public void setWallet(Wallet wallet)  {
    	this.wallet = wallet;
    }

    public byte[] getLatestBlockHash() {
            if (index.isEmpty())
                return StaticMessages.GENESIS_HASH;
            else
                return getLastBlock().getHash();
    }
    
	public void load() {
		DBIterator iterator = chainDb.iterator();
		try {
			if (!iterator.hasNext()) {
                logger.info("DB is empty - adding Genesis");
                this.lastBlock = Genesis.getInstance();
                this.addBlock(lastBlock);
                logger.debug("Block #{} -> {}", Genesis.NUMBER, lastBlock.toFlatString());
            } else {
            	logger.debug("Displaying blocks stored in DB sorted on blocknumber");
            	for (iterator.seekToFirst(); iterator.hasNext();) {
    	            this.lastBlock = new Block(iterator.next().getValue());
    	            this.index.put(lastBlock.getNumber(), lastBlock.getParentHash());
    	            logger.debug("Block #{} -> {}", lastBlock.getNumber(), lastBlock.toFlatString());
            	}
            }
			this.updateGasPrice(lastBlock);
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
    public void close() {
        if (this.chainDb != null)
            chainDb.close();
    }
}
