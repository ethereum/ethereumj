package org.ethereum.core;

import org.codehaus.plexus.util.FileUtils;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.AdvancedDeviceUtils;
import org.ethereum.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.core.Denomination.SZABO;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

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
 * www.etherJ.com
 * @author Roman Mandeleil,
 *           Nick Savers
 * Created on: 20/05/2014 10:44
 */
@Component
public class BlockchainImpl implements Blockchain {

    /* A scalar value equal to the mininum limit of gas expenditure per block */
    private static long MIN_GAS_LIMIT = 125000L;

	private static final Logger logger = LoggerFactory.getLogger("blockchain");
	private static final Logger stateLogger = LoggerFactory.getLogger("state");
	
	// to avoid using minGasPrice=0 from Genesis for the wallet
	private static final long INITIAL_MIN_GAS_PRICE = 10 * SZABO.longValue();

    @Autowired
	private Repository repository;
    private Repository track;

    @Autowired
    private BlockStore blockStore;

    private Block bestBlock;
    private BigInteger totalDifficulty = BigInteger.ZERO;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private BlockQueue blockQueue;

    @Autowired
    private ChannelManager channelManager;

    private boolean syncDoneCalled = false;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    private List<Chain> altChains = new ArrayList<>();
    private List<Block> garbage = new ArrayList<>();

    @Override
    public byte[] getBestBlockHash() {
        return getBestBlock().getHash();
    }
    
    @Override
    public long getSize() {
        return bestBlock.getNumber() + 1;
    }

    @Override
    public Block getBlockByNumber(long blockNr) {
    	return blockStore.getBlockByNumber(blockNr);
	}

    @Override
    public Block getBlockByHash(byte[] hash){
        return blockStore.getBlockByHash(hash);
    }

    @Override
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty){
        return blockStore.getListOfHashesStartFrom(hash, qty);
    }


    public void tryToConnect(Block block){

        recordBlock(block);

        if (logger.isDebugEnabled())
            logger.debug("Try connect block: {}",
                    Hex.toHexString(block.getEncoded()));

        if (blockStore.getBlockByHash(block.getHash()) != null){
            // retry of well known block
            return;
        }

        // The simple case got the block
        // to connect to the main chain
        if (bestBlock.isParentOf(block)){
            add(block);
            return;
        }

        // case when one of the alt chain probably
        // going to connect this block
        if (!hasParentOnTheChain(block)){

            Iterator<Chain> iterAltChains = altChains.iterator();
            boolean connected = false;
            while (iterAltChains.hasNext() && !connected){

                Chain chain = iterAltChains.next();
                connected = chain.tryToConnect(block);
                if (connected &&
                    chain.getTotalDifficulty().subtract(totalDifficulty).longValue() > 5000){


                    // todo: replay the alt on the main chain
                }
            }
            if (connected) return;
        }

        // The uncle block case: it is
        // start of alt chain: different
        // version of block we already
        // got on the main chain
        long gap = bestBlock.getNumber() - block.getNumber();
        if (hasParentOnTheChain(block) && gap <=0){

            logger.info("created alt chain by block.hash: [{}] ", block.getShortHash());
            Chain chain = new Chain();
            chain.setTotalDifficulty(totalDifficulty);
            chain.tryToConnect(block);
            altChains.add(chain);
            return;
        }


        // provisional, by the garbage will be
        // defined how to deal with it in the
        // future.
        garbage.add(block);

        // if there is too much garbage ask for re-sync
        if (garbage.size() > 20){
            worldManager.reset();
        }
    }


    @Override
    public void add(Block block) {

        track = repository.startTracking();
		if (block == null)
			return;

        // keep chain continuity
        if (!Arrays.equals(getBestBlock().getHash(),
                           block.getParentHash())) return;

        if (block.getNumber() >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            AdvancedDeviceUtils.adjustDetailedTracing(block.getNumber());
        }

        this.processBlock(block);
        track.commit();
        repository.flush(); // saving to the disc


        // Remove all wallet transactions as they already approved by the net
        worldManager.getWallet().removeTransactions(block.getTransactionsList());

        EthereumListener listener = worldManager.getListener();
        listener.trace(String.format("Block chain size: [ %d ]", this.getSize()));

        EthereumListener ethereumListener =  worldManager.getListener();
        ethereumListener.onBlock(block);

        if (blockQueue.size() == 0 &&
            !syncDoneCalled &&
            channelManager.isAllSync()){

            logger.info("Sync done");
            syncDoneCalled = true;
            ethereumListener.onSyncDone();
        }
    }


    public Block getParent(BlockHeader header){

        return blockStore.getBlockByHash(header.getParentHash());
    }

    /**
     * Calculate GasLimit
     * See Yellow Paper: http://www.gavwood.com/Paper.pdf - page 5, 4.3.4 (25)
     * @return long value of the gasLimit
     */
    public long calcGasLimit(BlockHeader header) {
        if (header.isGenesis())
            return Genesis.GAS_LIMIT;
        else {
            Block parent = getParent(header);
            return Math.max(MIN_GAS_LIMIT, (parent.getGasLimit() * (1024 - 1) + (parent.getGasUsed() * 6 / 5)) / 1024);
        }
    }


    public boolean isValid(BlockHeader header) {
        boolean isValid = false;
        // verify difficulty meets requirements
        isValid = header.getDifficulty() == header.calcDifficulty();
        // verify gasLimit meets requirements
        isValid = isValid && header.getGasLimit() == calcGasLimit(header);
        // verify timestamp meets requirements
        isValid = isValid && header.getTimestamp() > getParent(header).getTimestamp();
        // verify extraData doesn't exceed 1024 bytes
        isValid = isValid && header.getExtraData() == null || header.getExtraData().length <= 1024;
        return isValid;
    }


    /**
     * This mechanism enforces a homeostasis in terms of the time between blocks;
     * a smaller period between the last two blocks results in an increase in the
     * difficulty level and thus additional computation required, lengthening the
     * likely next period. Conversely, if the period is too large, the difficulty,
     * and expected time to the next block, is reduced.
     */
    private boolean isValid(Block block){

        boolean isValid = true;
        if (isValid) return (isValid); // todo get back to the real header validation

        if(!block.isGenesis()) {
            isValid = isValid(block.getHeader());

            for (BlockHeader uncle : block.getUncleList()) {
                // - They are valid headers (not necessarily valid blocks)
                isValid = isValid(uncle);
                // - Their parent is a kth generation ancestor for k in {2, 3, 4, 5, 6, 7}
                long generationGap = block.getNumber() - getParent(uncle).getNumber();
                isValid = generationGap > 1 && generationGap < 8;
                // - They were not uncles of the kth generation ancestor for k in {1, 2, 3, 4, 5, 6}
                generationGap = block.getNumber() - uncle.getNumber();
                isValid = generationGap > 0 && generationGap < 7;
            }
        }
        if(!isValid)
            logger.warn("WARNING: Invalid - {}", this);
        return isValid;

    }

    private void processBlock(Block block) {    	
    	if(isValid(block)) {
            if (!block.isGenesis()) {
                if (!CONFIG.blockChainOnly()) {
                    Wallet wallet = worldManager.getWallet();
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
		for (Transaction tx : block.getTransactionsList()) {
			stateLogger.info("apply block: [{}] tx: [{}] ", block.getNumber(), i);

            TransactionReceipt receipt = applyTransaction(block, tx);
			totalGasUsed += receipt.getCumulativeGasLong();
            receipt.setCumulativeGas(totalGasUsed);

            track.commit();

            receipt.setPostTxState(repository.getRoot());
            stateLogger.info("executed block: [{}] tx: [{}] \n  state: [{}]", block.getNumber(), i,
                    Hex.toHexString(repository.getRoot()));

            if(block.getNumber() >= CONFIG.traceStartBlock())
                repository.dumpState(block, totalGasUsed, i++, tx.getHash());

            // todo: (!!!). save the receipt
            // todo: cache all the receipts and
            // todo: save them to the disc together
            // todo: with the block afterwards
		}

		this.addReward(block);
        this.updateTotalDifficulty(block);

        track.commit();

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

		// Add standard block reward
		BigInteger totalBlockReward = Block.BLOCK_REWARD;
		
		// Add extra rewards based on number of uncles		
		if(block.getUncleList().size() > 0) {
			for (BlockHeader uncle : block.getUncleList()) {
				track.addBalance(uncle.getCoinbase(), Block.UNCLE_REWARD);
			}
			totalBlockReward = totalBlockReward.add(Block.INCLUSION_REWARD
					.multiply(BigInteger.valueOf(block.getUncleList().size())));
		}
        track.addBalance(block.getCoinbase(), totalBlockReward);
	}
    
	@Override
    public void storeBlock(Block block) {

        /* Debug check to see if the state is still as expected */
        if(logger.isWarnEnabled()) {
            String blockStateRootHash = Hex.toHexString(block.getStateRoot());
            String worldStateRootHash = Hex.toHexString(repository.getRoot());
            if(!blockStateRootHash.equals(worldStateRootHash)){

            	stateLogger.warn("BLOCK: STATE CONFLICT! block: {} worldstate {} mismatch", block.getNumber(), worldStateRootHash);

                // in case of rollback hard move the root
//                Block parentBlock = blockStore.getBlockByHash(block.getParentHash());
//                repository.syncToRoot(parentBlock.getStateRoot());
                // todo: after the rollback happens other block should be requested
            }
        }

        blockStore.saveBlock(block);
		this.setBestBlock(block);

        if (logger.isDebugEnabled())
			logger.debug("block added to the blockChain: index: [{}]", block.getNumber());
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
	public TransactionReceipt applyTransaction(Block block, Transaction tx) {

        logger.info("applyTransaction: [{}]", Hex.toHexString(tx.getHash()));

        TransactionReceipt receipt = new TransactionReceipt();

		byte[] coinbase = block.getCoinbase();

		// VALIDATE THE SENDER
		byte[] senderAddress = tx.getSender();
//		AccountState senderAccount = repository.getAccountState(senderAddress);
        logger.info("tx.sender: [{}]", Hex.toHexString(tx.getSender()));

		// VALIDATE THE NONCE
		BigInteger nonce = track.getNonce(senderAddress);
		BigInteger txNonce = new BigInteger(1, tx.getNonce());
		if (nonce.compareTo(txNonce) != 0) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
						nonce, txNonce);

            receipt.setCumulativeGas(0);
			return receipt;
		}
		
		// UPDATE THE NONCE
		track.increaseNonce(senderAddress);
        logger.info("increased tx.nonce to: [{}]", track.getNonce(senderAddress));

		// FIND OUT THE TRANSACTION TYPE
		byte[] receiverAddress, code = null;
		boolean isContractCreation = tx.isContractCreation();
		if (isContractCreation) {
			receiverAddress = tx.getContractAddress();
			code = tx.getData(); // init code
		} else {
			receiverAddress = tx.getReceiveAddress();
            code = track.getCode(receiverAddress);
            if (code != EMPTY_BYTE_ARRAY) {
                if (stateLogger.isDebugEnabled())
                    stateLogger.debug("calling for existing contract: address={}",
                            Hex.toHexString(receiverAddress));
            }
		}
		
		// THE SIMPLE VALUE/BALANCE CHANGE
		boolean isValueTx = tx.getValue() != null;
		if (isValueTx) {
			BigInteger txValue = new BigInteger(1, tx.getValue());
			if (track.getBalance(senderAddress).compareTo(txValue) >= 0) {

                track.addBalance(receiverAddress, txValue); // balance will be read again below
                track.addBalance(senderAddress, txValue.negate());
				
//				if(!isContractCreation) // adding to new contract could be reverted
//                    track.addBalance(receiverAddress, txValue); todo: find out what is that ?
				
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
        logger.info("Gas price limited to [{} wei]", gasDebit.toString());
	
		// Debit the actual total gas value from the sender
		// the purchased gas will be available for 
		// the contract in the execution state, 
		// it can be retrieved using GAS op
		if (gasDebit.signum() == 1) {
			if (track.getBalance(senderAddress).compareTo(gasDebit) == -1) {
				logger.debug("No gas to start the execution: sender={}",
						Hex.toHexString(senderAddress));

                receipt.setCumulativeGas(0);
				return receipt;
			}
			track.addBalance(senderAddress, gasDebit.negate());

            // The coinbase get the gas cost
            if (coinbase != null)
                track.addBalance(coinbase, gasDebit);

			if (stateLogger.isDebugEnabled())
				stateLogger.debug(
						"Before contract execution debit the sender address with gas total cost, "
								+ "\n sender={} \n gas_debit= {}",
						Hex.toHexString(senderAddress), gasDebit);
		}
				
		// CREATE AND/OR EXECUTE CONTRACT
		long gasUsed = 0;
		if (isContractCreation || code != EMPTY_BYTE_ARRAY) {
	
			// START TRACKING FOR REVERT CHANGES OPTION
			Repository trackTx = track.startTracking();
            logger.info("Start tracking VM run");
			try {
				
				// CREATE NEW CONTRACT ADDRESS AND ADD TX VALUE
				if(isContractCreation) {
                    trackTx.addBalance(receiverAddress, BigInteger.ZERO); // also creates account
					
					if(stateLogger.isDebugEnabled())
						stateLogger.debug("new contract created address={}",
								Hex.toHexString(receiverAddress));
				}
				
				Block currBlock =  (block == null) ? this.getBestBlock() : block;

				ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currBlock, trackTx);
				
				VM vm = new VM();
				Program program = new Program(code, programInvoke);

                if (CONFIG.playVM())
				    vm.play(program);

                // todo: recepit save logs
                // todo: receipt calc and save blooms

                program.saveProgramTraceToFile(Hex.toHexString(tx.getHash()));
				ProgramResult result = program.getResult();
				applyProgramResult(result, gasDebit, gasPrice, trackTx,
						senderAddress, receiverAddress, coinbase, isContractCreation);
				gasUsed = result.getGasUsed();

			} catch (RuntimeException e) {
                trackTx.rollback();

                receipt.setCumulativeGas(tx.getGasLimit());
                return receipt;
			}
            trackTx.commit();
		} else {
			// REFUND GASDEBIT EXCEPT FOR FEE (500 + 5*TX_NO_ZERO_DATA)
			long dataCost = tx.getData() == null ? 0: tx.getData().length * GasCost.TX_NO_ZERO_DATA;
			gasUsed = GasCost.TRANSACTION + dataCost;
			
			BigInteger refund = gasDebit.subtract(BigInteger.valueOf(gasUsed).multiply(gasPrice));
			if (refund.signum() > 0) {
				track.addBalance(senderAddress, refund);
                track.addBalance(coinbase, refund.negate());
			}
		}

        receipt.setCumulativeGas(gasUsed);
		return receipt;
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

    public boolean hasParentOnTheChain(Block block){
        return getParent(block.getHeader()) != null;
    }

    @Override
    public List<Chain> getAltChains(){
        return altChains;
    }

    @Override
    public List<Block> getGarbage(){
        return garbage;
    }


	@Override
	public BlockQueue getQueue() {
        return blockQueue;
    }

    @Override
    public void setBestBlock(Block block) {
        bestBlock = block;
    }

    @Override
    public Block getBestBlock() {
        return bestBlock;
    }

    @Override
    public void reset(){
        blockStore.reset();
        altChains = new ArrayList<>();
        garbage = new ArrayList<>();
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
	    this.totalDifficulty = totalDifficulty.add(block.getCumulativeDifficulty());
	}

    @Override
    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    private void recordBlock(Block block){

        if (!CONFIG.recordBlocks()) return;

        if (bestBlock.isGenesis()){
            try {FileUtils.deleteDirectory(CONFIG.dumpDir());} catch (IOException e) {}
        }

        String dir = CONFIG.dumpDir() + "/";

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + "_blocks_rec.txt");
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {

            dumpFile.getParentFile().mkdirs();
            if (!dumpFile.exists()) dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            if (bestBlock.isGenesis()){
                bw.write(Hex.toHexString(bestBlock.getEncoded()));
                bw.write("\n");
            }

            bw.write(Hex.toHexString(block.getEncoded()));
            bw.write("\n");

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





    }


}
