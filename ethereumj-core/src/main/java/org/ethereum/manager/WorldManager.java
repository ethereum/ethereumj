package org.ethereum.manager;

import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Repository;
import org.ethereum.vm.Program;
import org.ethereum.vm.ProgramInvoke;
import org.ethereum.vm.ProgramInvokeFactory;
import org.ethereum.vm.ProgramResult;
import org.ethereum.vm.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * WorldManager is the main class to handle the processing of transactions and
 * managing the world state.
 * 
 * www.ethereumJ.com
 * @author: Roman Mandeleil 
 * Created on: 01/06/2014 10:44
 */
public class WorldManager {

	private Logger logger = LoggerFactory.getLogger("main");
	private Logger stateLogger = LoggerFactory.getLogger("state");

	private Blockchain blockchain;
	private Repository repository;
	private Wallet wallet;

	private Map<String, Transaction> pendingTransactions = Collections
			.synchronizedMap(new HashMap<String, Transaction>());

	private static WorldManager instance;

	public WorldManager() {
		this.blockchain = new Blockchain();
		this.repository = new Repository();
		
		this.wallet = new Wallet();
		
		byte[] cowAddr = HashUtil.sha3("cow".getBytes());
		ECKey key = ECKey.fromPrivate(cowAddr);
		wallet.importKey(cowAddr);

		AccountState state = wallet.getAccountState(key.getAddress());
		state.addToBalance(BigInteger.valueOf(2).pow(200));

		String secret = CONFIG.coinbaseSecret();
		byte[] cbAddr = HashUtil.sha3(secret.getBytes());
		wallet.importKey(cbAddr);

		blockchain.setWallet(wallet);
	}
	
	public static WorldManager getInstance() {
		if(instance == null) {
			instance = new WorldManager();
			instance.getBlockChain().load();
		}
		return instance;
	}

	public void applyTransaction(Transaction tx, byte[] coinbase) {

		// TODO: refactor the wallet pending transactions to the world manager
		if (blockchain != null)
			blockchain.addWalletTransaction(tx);

		// TODO: what is going on with simple wallet transfer ?

		// 1. VALIDATE THE NONCE
		byte[] senderAddress = tx.getSender();
		AccountState senderAccount = repository.getAccountState(senderAddress);

		if (senderAccount == null) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("No such address: {}",
						Hex.toHexString(senderAddress));
			return;
		}

		BigInteger nonce = senderAccount.getNonce();
		BigInteger txNonce = new BigInteger(tx.getNonce());
		if (nonce.compareTo(txNonce) != 0) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
						nonce, txNonce);
			return;
		}

		// 2.1 PERFORM THE GAS VALUE TX
		// (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)

		// first of all debit the gas from the issuer
		BigInteger gasDebit = tx.getTotalGasValueDebit();

		// The coinbase get the gas cost
        if (coinbase != null)
		    repository.addBalance(coinbase, gasDebit);

		byte[] receiverAddress;

		// Contract creation or existing Contract call
		if (tx.isContractCreation()) {

			receiverAddress = tx.getContractAddress();
			repository.createAccount(receiverAddress);
			stateLogger.info("New contract created address={}",
					Hex.toHexString(receiverAddress));
		} else {

			receiverAddress = tx.getReceiveAddress();
			AccountState receiverState = repository.getAccountState(receiverAddress);

			if (receiverState == null) {
				repository.createAccount(receiverAddress);
				if (stateLogger.isInfoEnabled())
					stateLogger.info("New receiver account created address={}",
							Hex.toHexString(receiverAddress));
			}
		}

		// 2.2 UPDATE THE NONCE
		// (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
		BigInteger balance = senderAccount.getBalance();
		if (balance.compareTo(BigInteger.ZERO) == 1) {
			repository.increaseNonce(senderAddress);
		}

		// actual gas value debit from the sender
		// the purchase gas will be available for the
		// contract in the execution state, and
		// can be validate using GAS op
		if (gasDebit.signum() == 1) {
			if (balance.compareTo(gasDebit) == -1) {
				logger.info("No gas to start the execution: sender={}",
						Hex.toHexString(senderAddress));
				return;
			}
			repository.addBalance(senderAddress, gasDebit.negate());
			if (stateLogger.isInfoEnabled())
				stateLogger.info(
						"Before contract execution debit the sender address with gas total cost, "
								+ "\n sender={} \n gas_debit= {}",
						Hex.toHexString(senderAddress), gasDebit);
		}

		// 3. START TRACKING FOR REVERT CHANGES OPTION !!!
		Repository trackRepository = repository.getTrack();
		trackRepository.startTracking();

		try {

			// 4. THE SIMPLE VALUE/BALANCE CHANGE
			if (tx.getValue() != null) {

				BigInteger senderBalance = senderAccount.getBalance();

				if (senderBalance.compareTo(new BigInteger(1, tx.getValue())) >= 0) {

					repository.addBalance(receiverAddress,
							new BigInteger(1, tx.getValue()));
					repository.addBalance(senderAddress,
							new BigInteger(1, tx.getValue()).negate());

					if (stateLogger.isInfoEnabled())
						stateLogger.info("Update value balance \n "
								+ "sender={}, receiver={}, value={}",
								Hex.toHexString(senderAddress),
								Hex.toHexString(receiverAddress),
								new BigInteger(tx.getValue()));
				}
			}

			byte[] code;
			// 3. FIND OUT THE TRANSACTION TYPE
			if (tx.isContractCreation()) {
				code = tx.getData(); // init code
				if (logger.isInfoEnabled())
					logger.info("running the init for contract: address={}",
							Hex.toHexString(receiverAddress));
			} else {
				code = trackRepository.getCode(receiverAddress);
				if (code != null) {
					if (logger.isInfoEnabled())
						logger.info("calling for existing contract: address={}",
								Hex.toHexString(receiverAddress));
				}
			}

			if (code != null) {
				Block lastBlock = blockchain.getLastBlock();

				ProgramInvoke programInvoke = ProgramInvokeFactory
						.createProgramInvoke(tx, lastBlock, trackRepository);
				
				VM vm = new VM();
				Program program = new Program(code, programInvoke);
				vm.play(program);
				ProgramResult result = program.getResult();
				applyProgramResult(result, gasDebit, trackRepository,
						senderAddress, receiverAddress, coinbase, true);
			}
		} catch (RuntimeException e) {
			trackRepository.rollback();
			return;
		}
		trackRepository.commit();
		pendingTransactions.put(Hex.toHexString(tx.getHash()), tx);
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
			Repository repository, byte[] senderAddress,
			byte[] contractAddress, byte[] coinbase, boolean initResults) {

		if (result.getException() != null
				&& result.getException() instanceof Program.OutOfGasException) {
			logger.info("contract run halted by OutOfGas: contract={}",
					Hex.toHexString(contractAddress));

			throw result.getException();
		}


		BigInteger gasPrice = BigInteger.valueOf(blockchain.getGasPrice());
		BigInteger refund = gasDebit.subtract(BigInteger.valueOf(
				result.getGasUsed()).multiply(gasPrice));

		if (refund.signum() > 0) {
			if (stateLogger.isInfoEnabled())
				stateLogger
						.info("After contract execution the sender address refunded with gas leftover, "
								+ "\n sender={} \n contract={}  \n gas_refund= {}",
								Hex.toHexString(senderAddress),
								Hex.toHexString(contractAddress), refund);
			// gas refund
			repository.addBalance(senderAddress, refund);
			repository.addBalance(coinbase, refund.negate());
		}


        if (initResults){

            // Save the code created by init
            byte[] bodyCode = null;
            if (result.getHReturn() != null) {
                bodyCode = result.getHReturn().array();
            }

            if (bodyCode != null) {
                repository.saveCode(contractAddress, bodyCode);
                if (stateLogger.isInfoEnabled())
                    stateLogger
                            .info("saving code of the contract to the db:\n contract={} code={}",
                                    Hex.toHexString(contractAddress),
                                    Hex.toHexString(bodyCode));
            }
        }
	}

	public void applyBlock(Block block) {

		// miner reward
		if (repository.getAccountState(block.getCoinbase()) == null)
			repository.createAccount(block.getCoinbase());
		repository.addBalance(block.getCoinbase(), Block.BLOCK_REWARD);

		int i = 0;
		List<Transaction> txList = block.getTransactionsList();
		for (Transaction tx : txList) {
			applyTransaction(tx, block.getCoinbase());
			repository.dumpState(block.getNumber(), i,
					Hex.toHexString(tx.getHash()));
			++i;
		}
	}

	public void applyBlockList(List<Block> blocks) {
		for (int i = blocks.size() - 1; i >= 0; --i) {
			applyBlock(blocks.get(i));
		}
	}

	public Repository getRepository() {
		return repository;
	}
	
	public Blockchain getBlockChain() {
		return blockchain;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void close() {
		blockchain.close();
		repository.close();
	}
}
