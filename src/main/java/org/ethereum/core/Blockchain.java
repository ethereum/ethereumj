package org.ethereum.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ethereum.db.Config;
import org.ethereum.db.Database;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.submit.PendingTransaction;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class Blockchain extends ArrayList<Block> {

	private static Logger logger = LoggerFactory.getLogger(Blockchain.class);
	
	private Database db;
	private Wallet wallet;
    private long gasPrice = 1000;
    private Block lastBlock = new Genesis();

    private Map<BigInteger, PendingTransaction> pendingTransactions =
            Collections.synchronizedMap(new HashMap<BigInteger, PendingTransaction>());
	
	public Blockchain(Wallet wallet) {
		this.db = Config.CHAIN_DB;
		this.wallet = wallet;
		
		DBIterator iterator = db.iterator();
		try {
			for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
				byte[] value = iterator.peekNext().getValue();
				Block block = new Block(value);
				if(block.getNumber() > lastBlock.getNumber()) lastBlock = block;
				this.add(new Block(value));
			}
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Block getLastBlock() {
		return lastBlock;
	}
	
    public void addBlocks(List<Block> blocks) {

        // TODO: redesign this part when the state part and the genesis block is ready

        if (blocks.isEmpty()) return;

        Block firstBlockToAdd = blocks.get(blocks.size() - 1);

        // if it is the first block to add
        // check that the parent is the genesis
        if (this.isEmpty() &&
            !Arrays.equals(StaticMessages.GENESIS_HASH, firstBlockToAdd.getParentHash())){
             return;
        }	
        // if there is some blocks already keep chain continuity
        if (!this.isEmpty() ){
            Block lastBlock = this.get(this.size() - 1);
            String hashLast = Hex.toHexString(lastBlock.getHash());
            String blockParentHash = Hex.toHexString(firstBlockToAdd.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }
        for (int i = blocks.size() - 1; i >= 0 ; --i){
            Block block = blocks.get(i);
            this.add(block);
            if(block.getNumber() > lastBlock.getNumber()) lastBlock = block;
            db.put(block.getHash(), block.getEncoded());
            if (logger.isDebugEnabled())
                logger.debug("block added to the chain with hash: {}", Hex.toHexString(block.getHash()));
            this.gasPrice = block.getMinGasPrice();

            wallet.processBlock(block);
        }	
        // Remove all pending transactions as they already approved by the net
        for (Block block : blocks){
            for (Transaction tx : block.getTransactionsList()){
                if (logger.isDebugEnabled())
                    logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString( tx.getHash()));
                removePendingTransaction(tx);
            }
        }

        logger.info("*** Block chain size: [ {} ]", this.size());
    }
    
    /*
     *        1) the dialog put a pending transaction on the list
     *        2) the dialog send the transaction to a net
     *        3) wherever the transaction got for the wire in will change to approve state
     *        4) only after the approve a) Wallet state changes
     *        5) After the block is received with that tx the pending been clean up
    */
    public PendingTransaction addPendingTransaction(Transaction transaction) {

        BigInteger hash = new BigInteger(transaction.getHash());
        logger.info("pending transaction placed hash: {} ", hash.toString(16) );

        PendingTransaction pendingTransaction =  pendingTransactions.get(hash);
		if (pendingTransaction != null)
			pendingTransaction.incApproved();
		else {
			pendingTransaction = new PendingTransaction(transaction);
			pendingTransactions.put(hash, pendingTransaction);
		}
        return pendingTransaction;
    }

    public void removePendingTransaction(Transaction transaction){

        BigInteger hash = new BigInteger(transaction.getHash());
        logger.info("pending transaction removed with hash: {} ",  hash.toString(16) );
        pendingTransactions.remove(hash);
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public byte[] getLatestBlockHash(){
        if (this.isEmpty())
            return StaticMessages.GENESIS_HASH;
        else
          return lastBlock.getHash();
    }
}
