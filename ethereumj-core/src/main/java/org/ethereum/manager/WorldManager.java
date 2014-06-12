package org.ethereum.manager;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.ContractDetails;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.DatabaseImpl;
import org.ethereum.db.TrackDatabase;
import org.ethereum.trie.TrackTrie;
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

    public DatabaseImpl chainDB   = new DatabaseImpl("blockchain");
    public DatabaseImpl stateDB   = new DatabaseImpl("state");
    public DatabaseImpl detaildDB = new DatabaseImpl("details");

    public Trie worldState = new Trie(stateDB.getDb());

    public void applyTransaction(Transaction tx) {

        // TODO: refactor the wallet transactions to the world manager
        MainData.instance.getBlockchain().addWalletTransaction(tx);

        // TODO: what is going on with simple wallet transfer

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

        // 2.1 PERFORM THE GAS VALUE TX
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)

        // first of all debit the gas from the issuer
        AccountState receiverState = null;
        BigInteger gasDebit = tx.getTotalGasValueDebit();
        byte[] contractAddress;

        // Contract creation or existing Contract call
        if (tx.isContractCreation()) {

            // credit the receiver
            contractAddress = tx.getContractAddress();
            receiverState = new AccountState();
            worldState.update(contractAddress, receiverState.getEncoded());
            stateLogger.info("New contract created address={}",
                    Hex.toHexString(contractAddress));
        } else {

            contractAddress = tx.getReceiveAddress();
            byte[] accountData = this.worldState.get(tx.getReceiveAddress());
            if (accountData.length == 0){
                receiverState = new AccountState();
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("New account created address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            } else {
                receiverState = new AccountState(accountData);
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("Account found address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            }
        }

        // 2.2 UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        if (senderState.getBalance().compareTo(BigInteger.ZERO) == 1) {
            senderState.incrementNonce();
            worldState.update(tx.getSender(), senderState.getEncoded());

            if(stateLogger.isInfoEnabled())
                stateLogger.info("Before contract execution the sender address debit with gas total cost, " +
                                "\n sender={} \n gas_debit= {}",
                        Hex.toHexString( tx.getSender() ),    gasDebit);

        }

        // actual gas value debit from the sender
        // the purchase gas will be available for the
        // contract in the execution state, and
        // can be validate using GAS op
        if (gasDebit.signum() == 1){

            if (senderState.getBalance().subtract(gasDebit).signum() == -1){
                logger.info("No gas to start the execution: sender={}" , Hex.toHexString(tx.getSender()));
                return;
            }
            senderState.addToBalance(gasDebit.negate());
            worldState.update(senderAddress, senderState.getEncoded());
        }

        // 3. START TRACKING FOR REVERT CHANGES OPTION !!!
        TrackDatabase trackDetailDB = new TrackDatabase( WorldManager.instance.detaildDB );
        TrackDatabase trackChainDb  = new TrackDatabase( WorldManager.instance.chainDB);
        TrackTrie     trackStateDB  = new TrackTrie(WorldManager.instance.worldState );

        trackDetailDB.startTrack();
        trackChainDb.startTrack();
        trackStateDB.startTrack();

        try {

            // 4. THE SIMPLE VALUE/BALANCE CHANGE
            if(tx.getValue() != null) {

                if (senderState.getBalance().subtract(new BigInteger(1, tx.getValue())).signum() >= 0){
                    receiverState.addToBalance(new BigInteger(1, tx.getValue()));
                    senderState.addToBalance(new BigInteger(1, tx.getValue()).negate());

                    trackStateDB.update(senderAddress, senderState.getEncoded());
                    trackStateDB.update(contractAddress, receiverState.getEncoded());

                    if (stateLogger.isInfoEnabled())
                        stateLogger.info("Update value balance \n " +
                                        "sender={}, receiver={}, value={}",
                                Hex.toHexString(senderAddress),
                                Hex.toHexString(contractAddress),
                                new BigInteger( tx.getValue()));
                }
            }

            // 3. FIND OUT WHAT IS THE TRANSACTION TYPE
            if (tx.isContractCreation()) {

                byte[] initCode = tx.getData();

                Block lastBlock =
                        MainData.instance.getBlockchain().getLastBlock();

                ProgramInvoke programInvoke =
                    ProgramInvokeFactory.createProgramInvoke(tx, lastBlock, null, trackDetailDB, trackChainDb, trackStateDB);

                if (logger.isInfoEnabled())
                    logger.info("running the init for contract: addres={}" ,
                            Hex.toHexString(tx.getContractAddress()));


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


                        // FETCH THE SAVED STORAGE
                        ContractDetails details = null;
                        byte[] detailsRLPData = detaildDB.get(tx.getReceiveAddress());
                        if (detailsRLPData.length > 0)
                            details = new ContractDetails(detailsRLPData);

                        ProgramInvoke programInvoke =
                                ProgramInvokeFactory.createProgramInvoke(tx, lastBlock, details, trackDetailDB, trackChainDb, trackStateDB);

                        VM vm = new VM();
                        Program program = new Program(programCode, programInvoke);
                        vm.play(program);

                        ProgramResult result = program.getResult();
                        applyProgramResult(result, gasDebit, senderState, receiverState, senderAddress, tx.getReceiveAddress());
                    }
                }
            }
        } catch (RuntimeException e) {

            trackDetailDB.rollbackTrack();
            trackChainDb.rollbackTrack();
            trackStateDB.rollbackTrack();
            return;
        }

        trackDetailDB.commitTrack();
        trackChainDb.commitTrack();
        trackStateDB.commitTrack();

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
            logger.info("contract run halted by OutOfGas: contract={}", Hex.toHexString(contractAddress));

            throw result.getException();
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
