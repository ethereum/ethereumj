package org.ethereum.core;

import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.util.AdvancedDeviceUtils;
import org.ethereum.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

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
 * See <a href="https://github.com/ethereum/wiki/wiki/White-Paper#blockchain-and-mining">Ethereum Whitepaper</a>
 *
 * www.ethereumJ.com
 * @authors: Roman Mandeleil,
 *           Nick Savers
 * Created on: 20/05/2014 10:44
 */
public class BlockchainImpl implements Blockchain {

	private static final Logger logger = LoggerFactory.getLogger("blockchain");
	private static final Logger stateLogger = LoggerFactory.getLogger("state");
	
	// to avoid using minGasPrice=0 from Genesis for the wallet
	private static final long INITIAL_MIN_GAS_PRICE = 10 * SZABO.longValue();

	private Repository repository;
    private Block lastBlock;
    private BigInteger totalDifficulty;
    
    // keep the index of the chain for
    // convenient usage, <block_number, block_hash>
    private final Map<Long, byte[]> blockCache = new HashMap<>();
	
    private final BlockQueue blockQueue = new BlockQueue();
    private boolean syncDoneCalled = false;

	public BlockchainImpl(Repository repository) {
		this.repository = repository;
	}
	
	@Override
    public long getGasPrice() {
        // In case of the genesis block we don't want to rely on the min gas price
        return lastBlock.isGenesis() ? lastBlock.getMinGasPrice() : INITIAL_MIN_GAS_PRICE;
    }

    @Override
    public byte[] getLatestBlockHash() {
        if (blockCache.isEmpty())
            return Genesis.getInstance().getHash();
        else
            return getLastBlock().getHash();
    }
    
    @Override
    public int getSize() {
        return blockCache.size();
    }

    @Override
    public Block getBlockByNumber(long blockNr) {
    	return repository.getBlock(blockNr);
	}

    @Override
    public void add(Block block) {

		if (block == null)
			return;

        // if it is the first block to add
        // make sure the parent is genesis
		if (blockCache.isEmpty()
				&& !Arrays.equals(Genesis.getInstance().getHash(),
						block.getParentHash())) {
			return;
		}
        // if there are some blocks already keep chain continuity
        if (!blockCache.isEmpty()) {
            String hashLast = Hex.toHexString(getLastBlock().getHash());
            String blockParentHash = Hex.toHexString(block.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }
        
        if (block.getNumber() >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            AdvancedDeviceUtils.adjustDetailedTracing(block.getNumber());
        }

        this.processBlock(block);
        
        // Remove all wallet transactions as they already approved by the net
        WorldManager.getInstance().getWallet().removeTransactions(block.getTransactionsList());

        EthereumListener listener = WorldManager.getInstance().getListener();
        if (listener != null)
            listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));

        EthereumListener ethereumListener =  WorldManager.getInstance().getListener();
        if (ethereumListener != null){
            ethereumListener.onBlock(block);

            if (blockQueue.size() == 0 &&
                !syncDoneCalled &&
                WorldManager.getInstance().getActivePeer().isSyncDone()){
                syncDoneCalled = true;
                ethereumListener.onSyncDone();
            }
        }
    }
    
    private void processBlock(Block block) {    	
    	if(block.isValid()) {
            if (!block.isGenesis()) {
                if (!CONFIG.blockChainOnly()) {

                    Wallet wallet = WorldManager.getInstance().getWallet();

                    wallet.addTransactions(block.getTransactionsList());
                	this.applyBlock(block);
                    wallet.processBlock(block);
                }
            }
            this.storeBlock(block);
    	} else {
    		logger.warn("Invalid block with nr: {}", block.getNumber());
    	}
    }
    
	private void applyBlock(Block block) {

		int i = 0;
		long totalGasUsed = 0;
		for (TransactionReceipt txr : block.getTxReceiptList()) {
			stateLogger.debug("apply block: [{}] tx: [{}] ", block.getNumber(), i);
			totalGasUsed += applyTransaction(block, txr.getTransaction());
			if(block.getNumber() >= CONFIG.traceStartBlock())
				repository.dumpState(block, totalGasUsed, i++, txr.getTransaction().getHash());
			if(!Arrays.equals(this.repository.getWorldState().getRootHash(), txr.getPostTxState())) {
				stateLogger.warn("TX: STATE CONFLICT {}..: {}", Hex.toHexString(txr.getTransaction().getHash()).substring(0, 8),
						Hex.toHexString(this.repository.getWorldState().getRootHash()));
//            	repository.close();
//            	System.exit(-1); // Don't continue
            }
		}
		
		this.addReward(block);
		this.updateTotalDifficulty(block);
		
        if(block.getNumber() >= CONFIG.traceStartBlock())
        	repository.dumpState(block, totalGasUsed, 0, null);
	}

	/**
	 * Add reward to block- and every uncle coinbase 
	 * assuming the entire block is valid.
	 * 
	 * @param block object containing the header and uncles
	 */
	private void addReward(Block block) {
		// Create coinbase if doesn't exist yet
		if (repository.getAccountState(block.getCoinbase()) == null)
			repository.createAccount(block.getCoinbase());
		
		// Add standard block reward
		BigInteger totalBlockReward = Block.BLOCK_REWARD;
		
		// Add extra rewards based on number of uncles		
		if(block.getUncleList().size() > 0) {
			for (BlockHeader uncle : block.getUncleList()) {
				repository.addBalance(uncle.getCoinbase(), Block.UNCLE_REWARD);
			}
			totalBlockReward = totalBlockReward.add(Block.INCLUSION_REWARD
					.multiply(BigInteger.valueOf(block.getUncleList().size())));
		}
		repository.addBalance(block.getCoinbase(), totalBlockReward);
	}
    
	@Override
    public void storeBlock(Block block) {

        /* Debug check to see if the state is still as expected */
        if(logger.isWarnEnabled()) {
            String blockStateRootHash = Hex.toHexString(block.getStateRoot());
            String worldStateRootHash = Hex.toHexString(WorldManager.getInstance().getRepository().getWorldState().getRootHash());
            if(!blockStateRootHash.equals(worldStateRootHash)){
            	stateLogger.warn("BLOCK: STATE CONFLICT! block: {} worldstate {} mismatch", block.getNumber(), worldStateRootHash);
//                repository.close();
//                System.exit(-1); // Don't add block
            }
        }
    	
		this.repository.saveBlock(block);
		this.blockCache.put(block.getNumber(), block.getHash());
		this.setLastBlock(block);
		
        if (logger.isDebugEnabled())
			logger.debug("block added to the blockChain {}", block.toFlatString());
        if (block.getNumber() % 100 == 0)
        	logger.info("*** Last block added [ #{} ]", block.getNumber());
    }    
    
    /**
     * Apply the transaction to the world state.
     *
     * During this method changes to the repository are either permanent or possibly reverted by a VM exception.
     *  
     * @param block - the block which contains the transactions
     * @param tx - the transaction to be applied
     * @return gasUsed - the total amount of gas used for this transaction.
     */
	public long applyTransaction(Block block, Transaction tx) {

		byte[] coinbase = block.getCoinbase();

		// VALIDATE THE SENDER
		byte[] senderAddress = tx.getSender();
		AccountState senderAccount = repository.getAccountState(senderAddress);
		if (senderAccount == null) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("No such address: {}",
						Hex.toHexString(senderAddress));
			return 0;
		}

		// VALIDATE THE NONCE
		BigInteger nonce = senderAccount.getNonce();
		BigInteger txNonce = new BigInteger(1, tx.getNonce());
		if (nonce.compareTo(txNonce) != 0) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
						nonce, txNonce);
			return 0;
		}
		
		// UPDATE THE NONCE
		repository.increaseNonce(senderAddress);

		// FIND OUT THE TRANSACTION TYPE
		byte[] receiverAddress, code = null;
		boolean isContractCreation = tx.isContractCreation();
		if (isContractCreation) {
			receiverAddress = tx.getContractAddress();
			code = tx.getData(); // init code
		} else {
			receiverAddress = tx.getReceiveAddress();
			if (repository.getAccountState(receiverAddress) == null) {
				repository.createAccount(receiverAddress);
				if (stateLogger.isDebugEnabled())
					stateLogger.debug("new receiver account created address={}",
							Hex.toHexString(receiverAddress));
			} else {
				code = repository.getCode(receiverAddress);
				if (code != null) {
					if (stateLogger.isDebugEnabled())
						stateLogger.debug("calling for existing contract: address={}",
								Hex.toHexString(receiverAddress));
				}
			}
		}
		
		// THE SIMPLE VALUE/BALANCE CHANGE
		boolean isValueTx = tx.getValue() != null;
		if (isValueTx) {
			BigInteger txValue = new BigInteger(1, tx.getValue());
			if (senderAccount.getBalance().compareTo(txValue) >= 0) {
				senderAccount.subFromBalance(txValue); // balance will be read again below
				repository.addBalance(senderAddress, txValue.negate());
				
				if(!isContractCreation) // adding to new contract could be reverted
					repository.addBalance(receiverAddress, txValue);
				
				if (stateLogger.isDebugEnabled())
					stateLogger.debug("Update value balance \n "
							+ "sender={}, receiver={}, value={}",
							Hex.toHexString(senderAddress),
							Hex.toHexString(receiverAddress),
							new BigInteger(tx.getValue()));
			}
		}

		// GET TOTAL ETHER VALUE AVAILABLE FOR TX FEE
	    // TODO: performance improve multiply without BigInteger
		BigInteger gasPrice = new BigInteger(1, tx.getGasPrice());
		BigInteger gasDebit = new BigInteger(1, tx.getGasLimit()).multiply(gasPrice);
	
		// Debit the actual total gas value from the sender
		// the purchased gas will be available for 
		// the contract in the execution state, 
		// it can be retrieved using GAS op
		if (gasDebit.signum() == 1) {
			if (senderAccount.getBalance().compareTo(gasDebit) == -1) {
				logger.debug("No gas to start the execution: sender={}",
						Hex.toHexString(senderAddress));
				return 0;
			}
			repository.addBalance(senderAddress, gasDebit.negate());
            
            // The coinbase get the gas cost
            if (coinbase != null)
                repository.addBalance(coinbase, gasDebit);

			if (stateLogger.isDebugEnabled())
				stateLogger.debug(
						"Before contract execution debit the sender address with gas total cost, "
								+ "\n sender={} \n gas_debit= {}",
						Hex.toHexString(senderAddress), gasDebit);
		}
				
		// CREATE AND/OR EXECUTE CONTRACT
		long gasUsed = 0;
		if (isContractCreation || code != null) {
	
			// START TRACKING FOR REVERT CHANGES OPTION
			Repository trackRepository = repository.getTrack();
			trackRepository.startTracking();
			try {
				
				// CREATE NEW CONTRACT ADDRESS AND ADD TX VALUE
				if(isContractCreation) {
					if (isValueTx) // adding to balance also creates the account
						trackRepository.addBalance(receiverAddress, new BigInteger(1, tx.getValue()));
					else
						trackRepository.createAccount(receiverAddress);
					
					if(stateLogger.isDebugEnabled())
						stateLogger.debug("new contract created address={}",
								Hex.toHexString(receiverAddress));
				}
				
				Block currBlock =  (block == null) ? this.getLastBlock() : block;

				ProgramInvoke programInvoke = ProgramInvokeFactory
						.createProgramInvoke(tx, currBlock, trackRepository);
				
				VM vm = new VM();
				Program program = new Program(code, programInvoke);

                if (CONFIG.playVM())
				    vm.play(program);
				ProgramResult result = program.getResult();
				applyProgramResult(result, gasDebit, gasPrice, trackRepository,
						senderAddress, receiverAddress, coinbase, isContractCreation);
				gasUsed = result.getGasUsed();

			} catch (RuntimeException e) {
				trackRepository.rollback();
				return new BigInteger(1, tx.getGasLimit()).longValue();
			}
			trackRepository.commit();
		} else {
			// REFUND GASDEBIT EXCEPT FOR FEE (500 + 5*TXDATA)
			long dataCost = tx.getData() == null ? 0: tx.getData().length * GasCost.TXDATA;
			gasUsed = GasCost.TRANSACTION + dataCost;
			
			BigInteger refund = gasDebit.subtract(BigInteger.valueOf(gasUsed).multiply(gasPrice));
			if (refund.signum() > 0) {
				repository.addBalance(senderAddress, refund);
				repository.addBalance(coinbase, refund.negate());
			}
		}
		return gasUsed;
	}
	
	/**
	 * After any contract code finish the run the certain result should take
	 * place, according the given circumstances
	 * 
	 * @param result
	 * @param gasDebit
	 * @param senderAddress
	 * @param contractAddress
	 */
	private void applyProgramResult(ProgramResult result, BigInteger gasDebit,
			BigInteger gasPrice, Repository repository, byte[] senderAddress,
			byte[] contractAddress, byte[] coinbase, boolean initResults) {

		if (result.getException() != null
				&& result.getException() instanceof Program.OutOfGasException) {
			stateLogger.debug("contract run halted by OutOfGas: contract={}",
					Hex.toHexString(contractAddress));
			throw result.getException();
		}

		BigInteger refund = gasDebit.subtract(BigInteger.valueOf(
				result.getGasUsed()).multiply(gasPrice));

		if (refund.signum() > 0) {
			if (stateLogger.isDebugEnabled())
				stateLogger
						.debug("After contract execution the sender address refunded with gas leftover, "
								+ "\n sender={} \n contract={}  \n gas_refund= {}",
								Hex.toHexString(senderAddress),
								Hex.toHexString(contractAddress), refund);
			// gas refund
			repository.addBalance(senderAddress, refund);
			repository.addBalance(coinbase, refund.negate());
		}

		if (initResults) {
            // Save the code created by init
            byte[] bodyCode = null;
            if (result.getHReturn() != null && result.getHReturn().array().length > 0) {
                bodyCode = result.getHReturn().array();
            }

			if (bodyCode != null) {
				if (stateLogger.isDebugEnabled())
					stateLogger
							.debug("saving code of the contract to the db:\n contract={} code={}",
									Hex.toHexString(contractAddress),
									Hex.toHexString(bodyCode));
				repository.saveCode(contractAddress, bodyCode);
			}
        }

        // delete the marked to die accounts
        if (result.getDeleteAccounts() == null) return;
        for (DataWord address : result.getDeleteAccounts()){
            repository.delete(address.getNoLeadZeroesData());
        }
	}
	
	@Override
	public BlockQueue getQueue() {
        return blockQueue;
    }
    
	@Override
    public Map<Long, byte[]> getBlockCache() {
    	return this.blockCache;
    }
    
    @Override
	public Block getLastBlock() {
		return lastBlock;
	}

	@Override
    public void setLastBlock(Block block) {
    	this.lastBlock = block;
    }

    @Override
    public void close(){
        blockQueue.close();
    }

	@Override
	public BigInteger getTotalDifficulty() {
		return totalDifficulty;
	}

	@Override
	public void updateTotalDifficulty(Block block) {
		if (this.totalDifficulty == null)
			this.totalDifficulty = block.getCumulativeDifficulty();
		else
			this.totalDifficulty.add(block.getCumulativeDifficulty());
	}
}
