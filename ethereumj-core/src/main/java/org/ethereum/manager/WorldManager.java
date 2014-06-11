package org.ethereum.manager;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.ContractDetails;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Database;
import org.ethereum.trie.Trie;
import org.ethereum.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * WorldManager is the main class to handle the processing of transactions and managing the world state.
 *
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 10:44
 *
 */
public class WorldManager {

    private Logger logger = LoggerFactory.getLogger("main");
    private Logger stateLogger = LoggerFactory.getLogger("state");

    public static WorldManager instance = new WorldManager();

    private Map<String, Transaction> pendingTransactions =
            Collections.synchronizedMap(new HashMap<String, Transaction>());

    public Database chainDB   = new Database("blockchain");
    public Database stateDB   = new Database("state");
    public Database detaildDB = new Database("details");

    public Trie worldState = new Trie(stateDB.getDb());

    public void applyTransaction(Transaction tx) {

        // TODO: refactor the wallet transactions to the world manager
        MainData.instance.getBlockchain().addWalletTransaction(tx);

        // 1. VALIDATE THE NONCE
        byte[] senderAddress = tx.getSender();
        byte[] stateData = worldState.get(senderAddress);

        if (stateData == null || stateData.length == 0) {
            if (stateLogger.isWarnEnabled())
                stateLogger.warn("No such address: {}", Hex.toHexString(senderAddress));
            return;
        }

        AccountState senderState = new AccountState(stateData);
        if (senderState.getNonce().compareTo(new BigInteger(tx.getNonce())) !=  0) {
			if (stateLogger.isWarnEnabled())
				stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
						senderState.getNonce(), new BigInteger(tx.getNonce()));
            return;
        }

        // 2. THE SIMPLE BALANCE CHANGE SHOULD HAPPEN ANYWAY
        AccountState receiverState = null;

        // Check if the receive is a new contract
        if (tx.isContractCreation()) {
            byte[] contractAddress = tx.getContractAddress();
            receiverState = new AccountState();
            worldState.update(contractAddress, receiverState.getEncoded());
            stateLogger.info("New contract created address={}",
                    Hex.toHexString(contractAddress));
        } else {
        	// receiver was not set by creation of contract
            byte[] accountData = this.worldState.get(tx.getReceiveAddress());
            if (accountData.length == 0){
                receiverState = new AccountState();
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("New account created address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            } else {
                receiverState = new AccountState(accountData);
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("Account updated address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            }
        }
        if(tx.getValue() != null) {
        	receiverState.addToBalance(new BigInteger(1, tx.getValue()));
        	senderState.addToBalance(new BigInteger(1, tx.getValue()).negate());
        }

        if (senderState.getBalance().compareTo(BigInteger.ZERO) == 1) {
            senderState.incrementNonce();
            worldState.update(tx.getSender(), senderState.getEncoded());
            worldState.update(tx.getReceiveAddress(), receiverState.getEncoded());
        }

        // 3. FIND OUT WHAT IS THE TRANSACTION TYPE
        if (tx.isContractCreation()) {

            byte[] initCode = tx.getData();

            Block lastBlock =
                    MainData.instance.getBlockchain().getLastBlock();

            ProgramInvoke programInvoke =
                ProgramInvokeFactory.createProgramInvoke(tx, lastBlock, null);

            if (logger.isInfoEnabled())
                logger.info("running the init for contract: addres={}" ,
                        Hex.toHexString(tx.getContractAddress()));

            // first of all debit the gas from the issuer
            BigInteger gasDebit = tx.getTotalGasDebit();
            senderState.addToBalance(gasDebit.negate());
            if (senderState.getBalance().signum() == -1){
                // todo: the sender can't afford this contract do Out-Of-Gas

            }

            if(stateLogger.isInfoEnabled())
                stateLogger.info("Before contract execution the sender address debit with gas total cost, \n sender={} \n contract={}  \n gas_debit= {}",
                         Hex.toHexString( tx.getSender() ),    Hex.toHexString(tx.getContractAddress()), gasDebit);
            worldState.update(senderAddress, senderState.getEncoded());

            VM vm = new VM();
            Program program = new Program(initCode, programInvoke);
            vm.play(program);
            ProgramResult result = program.getResult();
            applyProgramResult(result, gasDebit, senderState, receiverState, senderAddress, tx.getContractAddress());

        } else {

            if (receiverState.getCodeHash() != HashUtil.EMPTY_DATA_HASH){

                byte[] programCode = chainDB.get(receiverState.getCodeHash());
                if (programCode != null && programCode.length != 0){

                    Block lastBlock =
                            MainData.instance.getBlockchain().getLastBlock();

                    if (logger.isInfoEnabled())
                        logger.info("calling for existing contract: addres={}" , Hex.toHexString(tx.getReceiveAddress()));

                    // first of all debit the gas from the issuer
                    BigInteger gasDebit = tx.getTotalGasDebit();
                    senderState.addToBalance(gasDebit.negate());
                    if (senderState.getBalance().signum() == -1){
                        // todo: the sender can't afford this contract do Out-Of-Gas
                    }

                    if(stateLogger.isInfoEnabled())
                        stateLogger.info("Before contract execution the sender address debit with gas total cost, \n sender={} \n contract={}  \n gas_debit= {}",
                                Hex.toHexString( tx.getSender() ),    Hex.toHexString(tx.getReceiveAddress()), gasDebit);
                    worldState.update(senderAddress, senderState.getEncoded());

                    // FETCH THE SAVED STORAGE
                    ContractDetails details = null;
                    byte[] detailsRLPData = detaildDB.get(tx.getReceiveAddress());
                    if (detailsRLPData.length > 0)
                        details = new ContractDetails(detailsRLPData);

                    ProgramInvoke programInvoke =
                            ProgramInvokeFactory.createProgramInvoke(tx, lastBlock, details);

                    VM vm = new VM();
                    Program program = new Program(programCode, programInvoke);
                    vm.play(program);

                    ProgramResult result = program.getResult();
                    applyProgramResult(result, gasDebit, senderState, receiverState, senderAddress, tx.getReceiveAddress());
                }
            }
        }
        pendingTransactions.put(Hex.toHexString(tx.getHash()), tx);
    }


    /**
     * After any contract code finish the run
     * the certain result should take place,
     * according the given circumstances
     *
     * @param result
     * @param gasDebit
     * @param senderState
     * @param receiverState
     * @param senderAddress
     * @param contractAddress
     */
    private void applyProgramResult(ProgramResult result, BigInteger gasDebit,
                                    AccountState senderState, AccountState receiverState,
                                    byte[] senderAddress, byte[] contractAddress) {

        if (result.getException() != null &&
                result.getException() instanceof Program.OutOfGasException){

            // todo: find out what exactly should be reverted in that case
            return;
        }

        // Save the code created by init
        byte[] bodyCode = null;
        if (result.gethReturn() != null){

            bodyCode = result.gethReturn().array();
        }

        BigInteger gasPrice =
                BigInteger.valueOf( MainData.instance.getBlockchain().getGasPrice());
        BigInteger refund =
                gasDebit.subtract(BigInteger.valueOf( result.getGasUsed()).multiply(gasPrice));

        if (refund.signum() > 0){
            if(stateLogger.isInfoEnabled())
                stateLogger.info("After contract execution the sender address refunded with gas leftover , \n sender={} \n contract={}  \n gas_refund= {}",
                        Hex.toHexString(senderAddress) ,Hex.toHexString(contractAddress), refund);
            senderState.addToBalance(refund);
            worldState.update(senderAddress, senderState.getEncoded());
        }

        if (bodyCode != null){
            byte[] codeKey = HashUtil.sha3(bodyCode);
            chainDB.put(codeKey, bodyCode);
            receiverState.setCodeHash(codeKey);
            worldState.update(contractAddress, receiverState.getEncoded());

            if (stateLogger.isInfoEnabled())
                stateLogger.info("saving code of the contract to the db:\n contract={} sha3(code)={} code={}",
                        Hex.toHexString(contractAddress),
                        Hex.toHexString(codeKey),
                        Hex.toHexString(bodyCode));
        }

        // Save the storage changes.
        Map<DataWord, DataWord> storage =  result.getStorage();
        if (storage != null){
            ContractDetails contractDetails = new ContractDetails(storage);
            detaildDB.put(contractAddress , contractDetails.getEncoded());
        }
    }

    public void applyTransactionList(List<Transaction> txList) {
        for (Transaction tx :  txList){
            applyTransaction(tx);
        }
    }

    public void applyBlock(Block block) {
        List<Transaction> txList = block.getTransactionsList();
        applyTransactionList(txList);
    }

    public void applyBlockList(List<Block> blocks) {
        for (int i = blocks.size() - 1; i >= 0 ; --i) {
            applyBlock(blocks.get(i));
        }
    }

    public void close() {
        chainDB.close();
        stateDB.close();
    }
}
