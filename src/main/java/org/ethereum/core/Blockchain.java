package org.ethereum.core;

import org.ethereum.db.Database;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.submit.WalletTransaction;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.*;

import static org.ethereum.core.Denomination.*;

public class Blockchain extends ArrayList<Block> {

	private static final long serialVersionUID = -143590724563460486L;

	private static Logger logger = LoggerFactory.getLogger(Blockchain.class);
	
	// to avoid using minGasPrice=0 from Genesis for the wallet
	private static long INITIAL_MIN_GAS_PRICE = 10 * SZABO.longValue();
		
	private Database db;
	private Wallet wallet;
	
    private long gasPrice = 1000;
    private Block lastBlock;

    // This map of transaction designed
    // to approve the tx by external trusted peer
    private Map<String, WalletTransaction> walletTransactions =
            Collections.synchronizedMap(new HashMap<String, WalletTransaction>());

	public Blockchain(Wallet wallet) {
		this.db = WorldManager.instance.chainDB;
		this.wallet = wallet;
		this.loadChain();
	}

	public Block getLastBlock() {
		return lastBlock;
	}
	
    public void addBlocks(List<Block> blocks) {

		if (blocks.isEmpty())
			return;

        Block firstBlockToAdd = blocks.get(blocks.size() - 1);

        // if it is the first block to add
        // check that the parent is the genesis
		if (this.isEmpty()
				&& !Arrays.equals(StaticMessages.GENESIS_HASH,
						firstBlockToAdd.getParentHash())) {
			return;
		}
        // if there is some blocks already keep chain continuity
        if (!this.isEmpty()) {
            Block lastBlock = this.get(this.size() - 1);
            String hashLast = Hex.toHexString(lastBlock.getHash());
            String blockParentHash = Hex.toHexString(firstBlockToAdd.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }
        for (int i = blocks.size() - 1; i >= 0 ; --i){
        	Block block = blocks.get(i);
            this.addBlock(block);
            db.put(block.getParentHash(), block.getEncoded());
            if (logger.isDebugEnabled())
                logger.debug("block added to the chain with hash: {}", Hex.toHexString(block.getHash()));
        }
        // Remove all wallet transactions as they already approved by the net
        for (Block block : blocks) {
            for (Transaction tx : block.getTransactionsList()) {
                if (logger.isDebugEnabled())
                    logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString( tx.getHash()));
                removeWalletTransaction(tx);
            }
        }
        logger.info("*** Block chain size: [ {} ]", this.size());
    }
    
    private void addBlock(Block block) {
    	if(block.isValid()) {
			this.wallet.processBlock(block);
	        // that is the genesis case , we don't want to rely
	        // on this price will use default 10000000000000
	        // todo: refactor this longValue some constant defaults class 10000000000000L
			this.gasPrice = block.isGenesis() ? INITIAL_MIN_GAS_PRICE : block.getMinGasPrice();
			if(lastBlock == null || block.getNumber() > lastBlock.getNumber())
				this.lastBlock = block;
			this.add(block);
    	}
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
        logger.info("pending transaction placed hash: {} ", hash );

        WalletTransaction walletTransaction =  this.walletTransactions.get(hash);
		if (walletTransaction != null)
			walletTransaction.incApproved();
		else {
			walletTransaction = new WalletTransaction(transaction);
			this.walletTransactions.put(hash, walletTransaction);
		}
        return walletTransaction;
    }

    public void removeWalletTransaction(Transaction transaction){
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction removed with hash: {} ",  hash );
        walletTransactions.remove(hash);
    }

    public byte[] getLatestBlockHash(){
		if (this.isEmpty())
			return StaticMessages.GENESIS_HASH;
		else
			return lastBlock.getHash();
    }
    
	public void loadChain() {
		DBIterator iterator = db.iterator();
		try {
			if (!iterator.hasNext()) {
				logger.info("DB is empty - adding Genesis");
				Block genesis = Genesis.getInstance();
				this.addBlock(genesis);
				logger.debug("Block: " + genesis.getNumber() + " ---> " + genesis.toFlatString());
				db.put(genesis.getParentHash(), genesis.getEncoded());
			} else {
				logger.debug("Displaying blocks stored in DB sorted on blocknumber");
				byte[] parentHash = Genesis.PARENT_HASH; // get Genesis block by parentHash
				for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
					this.addBlock(new Block(db.get(parentHash)));
					if (logger.isDebugEnabled())
						logger.debug("Block: " + lastBlock.getNumber() + " ---> " + lastBlock.toFlatString());
					parentHash = lastBlock.getHash();					
				}
			}
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
