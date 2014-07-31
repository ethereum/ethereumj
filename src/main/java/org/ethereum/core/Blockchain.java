package org.ethereum.core;

import org.ethereum.db.DatabaseImpl;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.AdvancedDeviceUtils;
import org.ethereum.util.ByteUtil;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.core.Denomination.SZABO;

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
   
    private Block lastBlock;

    // keep the index of the chain for
    // convenient usage, <block_number, block_hash>
    private Map<Long, byte[]> index = new HashMap<>();

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

    public void add(Block block) {

		if (block == null)
			return;

        // if it is the first block to add
        // make sure the parent is genesis
		if (index.isEmpty()
				&& !Arrays.equals(Genesis.getInstance().getHash(),
						block.getParentHash())) {
			return;
		}
        // if there is some blocks already keep chain continuity
        if (!index.isEmpty()) {
            String hashLast = Hex.toHexString(getLastBlock().getHash());
            String blockParentHash = Hex.toHexString(block.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }
        
        if (block.getNumber() >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            AdvancedDeviceUtils.adjustDetailedTracing(block.getNumber());
        }

        this.processBlock(block);
        
        // Remove all wallet transactions as they already approved by the net
        for (Transaction tx : block.getTransactionsList()) {
            if (logger.isDebugEnabled())
                logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString( tx.getHash()));
            WorldManager.getInstance().removeWalletTransaction(tx);
        }

        EthereumListener listener = WorldManager.getInstance().getListener();
        if (listener != null)
            listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));
        
/*
        if (lastBlock.getNumber() >= 30) {
            System.out.println("** checkpoint **");

            this.close();
            WorldManager.getInstance().getRepository().close();
            System.exit(1);
        }
*/
    }
    
    public void processBlock(Block block) {
    	if(block.isValid()) {
            if (!block.isGenesis()) {
        		for (Transaction tx : block.getTransactionsList())
        			// TODO: refactor the wallet pending transactions to the world manager
        			WorldManager.getInstance().addWalletTransaction(tx);
                	WorldManager.getInstance().applyBlock(block);
                	WorldManager.getInstance().getWallet().processBlock(block);
            }
            this.storeBlock(block);		
    	} else {
    		logger.warn("Invalid block with nr: {}", block.getNumber());
    	}
    }
    
    private void storeBlock(Block block) {
        /* Debug check to see if the state is still as expected */
        if(logger.isWarnEnabled()) {
            String blockStateRootHash = Hex.toHexString(block.getStateRoot());
            String worldStateRootHash = Hex.toHexString(WorldManager.getInstance().getRepository().getWorldState().getRootHash());
            if(!blockStateRootHash.equals(worldStateRootHash)){
                    logger.error("ERROR: STATE CONFLICT! block: {} worldstate {} mismatch", block.getNumber(), worldStateRootHash);
                    // Last conflict on block 1157 -> worldstate b1d9a978451ef04c1639011d9516473d51c608dbd25906c89be791707008d2de
                    System.exit(-1); // Don't add block
            }
        }
    	
		this.chainDb.put(ByteUtil.longToBytes(block.getNumber()), block.getEncoded());
		this.index.put(block.getNumber(), block.getHash());
		this.setLastBlock(block);
		
        if (logger.isDebugEnabled())
			logger.debug("block added {}", block.toFlatString());
		logger.info("*** Block chain size: [ {} ]", this.getSize());
    }
    
    public long getGasPrice() {
        // In case of the genesis block we don't want to rely on the min gas price
        return lastBlock.isGenesis() ? lastBlock.getMinGasPrice() : INITIAL_MIN_GAS_PRICE;
    }

    public byte[] getLatestBlockHash() {
            if (index.isEmpty())
                return Genesis.getInstance().getHash();
            else
                return getLastBlock().getHash();
    }
    
	public void load() {
		DBIterator iterator = chainDb.iterator();
		try {
			if (!iterator.hasNext()) {
                logger.info("DB is empty - adding Genesis");
                this.lastBlock = Genesis.getInstance();
                this.storeBlock(lastBlock);
                logger.debug("Block #{} -> {}", Genesis.NUMBER, lastBlock.toFlatString());
            } else {
            	logger.debug("Displaying blocks stored in DB sorted on blocknumber");
            	for (iterator.seekToFirst(); iterator.hasNext();) {
    	            this.lastBlock = new Block(iterator.next().getValue());
    	            this.index.put(lastBlock.getNumber(), lastBlock.getHash());
    	            logger.debug("Block #{} -> {}", lastBlock.getNumber(), lastBlock.toFlatString());
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
	
    public void close() {
        if (this.chainDb != null)
            chainDb.close();
    }
}
