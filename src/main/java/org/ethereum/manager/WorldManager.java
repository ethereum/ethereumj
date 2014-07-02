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
import org.ethereum.db.DatabaseImpl;
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

	private Blockchain blockChain;
	private Wallet wallet = new Wallet();

	private Map<String, Transaction> pendingTransactions = Collections
			.synchronizedMap(new HashMap<String, Transaction>());

	public DatabaseImpl chainDB = new DatabaseImpl("blockchain");
	public Repository repository = new Repository();
	
	public static WorldManager instance = new WorldManager();

	public WorldManager() {
	}

	public void loadChain() {

		// Initialize Wallet
		byte[] cowAddr = HashUtil.sha3("cow".getBytes());
		ECKey key = ECKey.fromPrivate(cowAddr);
		wallet.importKey(cowAddr);

		AccountState state = wallet.getAccountState(key.getAddress());
		state.addToBalance(BigInteger.valueOf(2).pow(200));

		String secret = CONFIG.coinbaseSecret();
		byte[] cbAddr = HashUtil.sha3(secret.getBytes());
		wallet.importKey(cbAddr);

		// Initialize Blockchain
		blockChain = new Blockchain(wallet);
		blockChain.loadChain();
	}

	public void applyTransaction(Transaction tx, byte[] coinbase) {

		// TODO: refactor the wallet pending transactions to the world manager
		if (blockChain != null)
			blockChain.addWalletTransaction(tx);

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

		BigInteger nonce = repository.getNonce(senderAddress);
		if (nonce.compareTo(new BigInteger(tx.getNonce())) != 0) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
						nonce.longValue(), new BigInteger(tx.getNonce()));
			return;
		}

		// 2.1 PERFORM THE GAS VALUE TX
		// (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)

		// first of all debit the gas from the issuer
		BigInteger gasDebit = tx.getTotalGasValueDebit();

		// The coinbase get the gas cost
		repository.addBalance(coinbase, gasDebit);

		byte[] contractAddress;

		// Contract creation or existing Contract call
		if (tx.isContractCreation()) {

			// credit the receiver
			contractAddress = tx.getContractAddress();
			repository.createAccount(contractAddress);
			stateLogger.info("New contract created address={}",
					Hex.toHexString(contractAddress));
		} else {

			contractAddress = tx.getReceiveAddress();
			AccountState receiverState = repository.getAccountState(tx
					.getReceiveAddress());

			if (receiverState == null) {
				repository.createAccount(tx.getReceiveAddress());
				if (stateLogger.isInfoEnabled())
					stateLogger.info("New account created address={}",
							Hex.toHexString(tx.getReceiveAddress()));
			}
		}

		// 2.2 UPDATE THE NONCE
		// (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
		BigInteger balance = repository.getBalance(senderAddress);
		if (balance.compareTo(BigInteger.ZERO) == 1) {
			repository.increaseNonce(senderAddress);

			if (stateLogger.isInfoEnabled())
				stateLogger.info(
						"Before contract execution the sender address debit with gas total cost, "
								+ "\n sender={} \n gas_debit= {}",
						Hex.toHexString(tx.getSender()), gasDebit);
		}

		// actual gas value debit from the sender
		// the purchase gas will be available for the
		// contract in the execution state, and
		// can be validate using GAS op
		if (gasDebit.signum() == 1) {
			if (balance.compareTo(gasDebit) == -1) {
				logger.info("No gas to start the execution: sender={}",
						Hex.toHexString(tx.getSender()));
				return;
			}
			repository.addBalance(senderAddress, gasDebit.negate());
		}

		// 3. START TRACKING FOR REVERT CHANGES OPTION !!!
		Repository trackRepository = repository.getTrack();
		trackRepository.startTracking();

		try {

			// 4. THE SIMPLE VALUE/BALANCE CHANGE
			if (tx.getValue() != null) {

				BigInteger senderBalance = repository.getBalance(senderAddress);
				BigInteger contractBalance = repository.getBalance(contractAddress);

				if (senderBalance.compareTo(new BigInteger(1, tx.getValue())) >= 0) {

					repository.addBalance(contractAddress,
							new BigInteger(1, tx.getValue()));
					repository.addBalance(senderAddress,
							new BigInteger(1, tx.getValue()).negate());

					if (stateLogger.isInfoEnabled())
						stateLogger.info("Update value balance \n "
								+ "sender={}, receiver={}, value={}",
								Hex.toHexString(senderAddress),
								Hex.toHexString(contractAddress),
								new BigInteger(tx.getValue()));
				}
			}

			// 3. FIND OUT WHAT IS THE TRANSACTION TYPE
			if (tx.isContractCreation()) {

				byte[] initCode = tx.getData();

				Block lastBlock = blockChain.getLastBlock();

				ProgramInvoke programInvoke = ProgramInvokeFactory
						.createProgramInvoke(tx, lastBlock, trackRepository);

				if (logger.isInfoEnabled())
					logger.info("running the init for contract: addres={}",
							Hex.toHexString(tx.getContractAddress()));

				VM vm = new VM();
				Program program = new Program(initCode, programInvoke);
				vm.play(program);
				ProgramResult result = program.getResult();
				applyProgramResult(result, gasDebit, trackRepository,
						senderAddress, tx.getContractAddress(), coinbase);

			} else {

				byte[] programCode = trackRepository.getCode(tx
						.getReceiveAddress());
				if (programCode != null) {

					Block lastBlock = blockChain.getLastBlock();

					if (logger.isInfoEnabled())
						logger.info("calling for existing contract: addres={}",
								Hex.toHexString(tx.getReceiveAddress()));

					ProgramInvoke programInvoke = ProgramInvokeFactory
							.createProgramInvoke(tx, lastBlock, trackRepository);

					VM vm = new VM();
					Program program = new Program(programCode, programInvoke);
					vm.play(program);

					ProgramResult result = program.getResult();
					applyProgramResult(result, gasDebit, trackRepository,
							senderAddress, tx.getReceiveAddress(), coinbase);
				}
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
			byte[] contractAddress, byte[] coinbase) {

		if (result.getException() != null
				&& result.getException() instanceof Program.OutOfGasException) {
			logger.info("contract run halted by OutOfGas: contract={}",
					Hex.toHexString(contractAddress));

			throw result.getException();
		}

		// Save the code created by init
		byte[] bodyCode = null;
		if (result.getHReturn() != null) {
			bodyCode = result.getHReturn().array();
		}

		BigInteger gasPrice = BigInteger.valueOf(blockChain.getGasPrice());
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

		if (bodyCode != null) {
			repository.saveCode(contractAddress, bodyCode);
			if (stateLogger.isInfoEnabled())
				stateLogger
						.info("saving code of the contract to the db:\n contract={} code={}",
								Hex.toHexString(contractAddress),
								Hex.toHexString(bodyCode));
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

	public Blockchain getBlockChain() {
		return blockChain;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void close() {
		chainDB.close();
		repository.close();
	}
}
