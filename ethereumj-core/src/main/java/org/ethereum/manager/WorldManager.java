package org.ethereum.manager;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.Database;
import org.ethereum.trie.Trie;
import org.ethereum.vm.Program;
import org.ethereum.vm.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 07/06/2014 10:08
 */

public class WorldManager {

    Logger logger = LoggerFactory.getLogger("main");
    Logger stateLogger = LoggerFactory.getLogger("state");

    public static WorldManager instance = new WorldManager();

    private Map<String, Transaction> pendingTransactions =
            Collections.synchronizedMap(new HashMap<String, Transaction>());

    public Database chainDB = new Database("blockchain");
    public Database stateDB = new Database("state");

    public Trie allAccountsState = new Trie(stateDB.getDb());


    public void applyTransaction(Transaction tx){

        // todo: refactor the wallet transactions to the world manager
        MainData.instance.getBlockchain().addWalletTransaction(tx);

        // 1. VALIDATE THE NONCE
        byte[] senderAddress = tx.getSender();
        byte[] stateData = allAccountsState.get(senderAddress);

        if (stateData == null || stateData.length == 0) {
            if (stateLogger.isWarnEnabled())
                stateLogger.warn("No such address: {}", Hex.toHexString(senderAddress));
            return;
        }

        AccountState senderState = new AccountState(stateData);
        if (senderState.getNonce().compareTo(new BigInteger(tx.getNonce())) !=  0){

            if (stateLogger.isWarnEnabled())
                stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
                        senderState.getNonce(),
                        new BigInteger(tx.getNonce()));
            return;
        }


        // 2. FIND OUT WHAT IS THE TRANSACTION TYPE
        if (tx.isContractCreation()){

            // todo 0. run the init method

            VM vm = new VM();
            Program program = new Program(null, null);


        } else{

            AccountState recieverState;
            byte[] accountData = this.allAccountsState.get(tx.getReceiveAddress());
            if (accountData.length == 0){

                recieverState = new AccountState(tx.getKey());
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("New account created address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            } else {
                recieverState = new AccountState(accountData);
                if (stateLogger.isInfoEnabled())
                    stateLogger.info("Account updated address={}",
                            Hex.toHexString(tx.getReceiveAddress()));
            }

            // APPLY THE BALANCE VALUE
            recieverState.addToBalance(new BigInteger(1, tx.getValue()));
            senderState.addToBalance(new BigInteger(1, tx.getValue()).negate());


            // todo 2. check if the address is a contract,  if it is perform contract call


            if (senderState.getBalance().compareTo(BigInteger.ZERO) == 1){

                senderState.incrementNonce();
                allAccountsState.update(tx.getSender(), senderState.getEncoded());
                allAccountsState.update(tx.getReceiveAddress(), recieverState.getEncoded());
            }

        }

        pendingTransactions.put(Hex.toHexString(tx.getHash()), tx);
    }

    public void applyTransactionList(List<Transaction> txList){
        for (Transaction tx :  txList){
            applyTransaction(tx);
        }
    }

    public void applyBlock(Block block){

        List<Transaction> txList = block.getTransactionsList();
        applyTransactionList(txList);
    }

    public void applyBlockList(List<Block> blocks){
        for (int i = blocks.size() - 1; i >= 0 ; --i){
            applyBlock(blocks.get(i));
        }
    }



    public void close(){
        chainDB.close();
        stateDB.close();
    }

}
