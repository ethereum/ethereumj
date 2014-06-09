package org.ethereum.manager;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
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

    public Database chainDB = new Database("blockchain");
    public Database stateDB = new Database("state");

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
                ProgramInvokeFactory.createProgramInvoke(tx, lastBlock);

            if (logger.isInfoEnabled())
                logger.info("running the init for contract: addres={}" ,
                        Hex.toHexString(tx.getContractAddress()));

            VM vm = new VM();
            Program program = new Program(initCode, programInvoke);
            vm.play(program);

            ProgramResult result = program.getResult();
            byte[] bodyCode = null;
            if (result.gethReturn() != null){

                bodyCode = result.gethReturn().array();
            }
            // TODO: what if the body code is null , still submit ?

            // TODO: (!!!!!) ALL THE CHECKS FOR THE PROGRAM RESULT

            if (bodyCode != null){

                byte[] codeKey = HashUtil.sha3(bodyCode);
                chainDB.put(codeKey, bodyCode);
                receiverState.setCodeHash(codeKey);
                worldState.update(tx.getContractAddress(), receiverState.getEncoded());

                if (stateLogger.isInfoEnabled())
                    stateLogger.info("saving code of the contract to the db:\n contract={} sha3(code)={} code={}",
                            Hex.toHexString(tx.getContractAddress()),
                            Hex.toHexString(codeKey),
                            Hex.toHexString(bodyCode));
            }

        } else {

            if (receiverState.getCodeHash() != HashUtil.EMPTY_DATA_HASH){

                byte[] programCode = chainDB.get(receiverState.getCodeHash());
                if (programCode.length != 0){

                    Block lastBlock =
                            MainData.instance.getBlockchain().getLastBlock();

                    ProgramInvoke programInvoke =
                            ProgramInvokeFactory.createProgramInvoke(tx, lastBlock);

                    if (logger.isInfoEnabled())
                        logger.info("calling for existing contract: addres={}" , Hex.toHexString(tx.getReceiveAddress()));

                    VM vm = new VM();
                    Program program = new Program(programCode, programInvoke);
                    vm.play(program);

                    ProgramResult result = program.getResult();

                    // TODO: (!!!!!) ALL THE CHECKS FOR THE PROGRAM RESULT


                }
            }

        }
        pendingTransactions.put(Hex.toHexString(tx.getHash()), tx);
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
