package org.ethereum.db;

import org.codehaus.plexus.util.FileUtils;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.json.JSONHelper;
import org.ethereum.trie.TrackTrie;
import org.ethereum.trie.Trie;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 *
 *
 ***********************************************************************************
         MainRepository
         |
             --> AccountState      ---> Trie ---> leveldb (state) /key=address
                 --> nonce
                 --> balance
                 --> stateRoot
                 --> codeHash
         |
             -->  ContractDetails  ---> leveldb(details) /key=address
                 --> code      ---> sha3(code) // saved into AccountInfo.codeHash
                 --> storage   ---> Trie // to calculate the AccountInfo.stateRoot
 ***********************************************************************************
 *
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 23/06/2014 23:01
 */
public class Repository {

    private Logger logger = LoggerFactory.getLogger("repository");

    Trie worldState;

    TrackTrie     accountStateDB;
    TrackDatabase contractDetailsDB;

    // todo: Listeners listeners
    // todo: cash impl

    DatabaseImpl detailsDB = null;
    DatabaseImpl stateDB = null;


    public Repository() {

        detailsDB     = new DatabaseImpl("details");
        contractDetailsDB = new TrackDatabase(detailsDB);


        stateDB = new DatabaseImpl("state");
        worldState = new Trie(stateDB.getDb());

        accountStateDB = new TrackTrie(worldState);
    }

    private Repository(TrackTrie accountStateDB, TrackDatabase contractDetailsDB){
        this.accountStateDB = accountStateDB;
        this.contractDetailsDB = contractDetailsDB;
    }

    public Repository getTrack(){

        TrackTrie     trackState   = new TrackTrie(accountStateDB);
        TrackDatabase trackDetails = new TrackDatabase(contractDetailsDB);

        return new Repository (trackState, trackDetails);
    }

    public void startTracking(){

        logger.info("start tracking");
        accountStateDB.startTrack();
        contractDetailsDB.startTrack();
    }

    public void commit(){
        logger.info("commit changes");
        accountStateDB.commitTrack();
        contractDetailsDB.commitTrack();
    }

    public void rollback(){
        logger.info("rollback changes");
        accountStateDB.rollbackTrack();
        contractDetailsDB.rollbackTrack();
    }

    public AccountState createAccount(byte[] addr){

        // 1. Save AccountState
        AccountState state =  new AccountState();
        accountStateDB.update(addr, state.getEncoded());

        // 2. Save ContractDetails
        ContractDetails details = new ContractDetails();
        contractDetailsDB.put(addr, details.getEncoded());

        if (logger.isInfoEnabled())
            logger.info("New account created: [ {} ]", Hex.toHexString(addr));


        return state;
    }


    public AccountState getAccountState(byte[] addr){

        byte[] accountStateRLP = accountStateDB.get(addr);
        if (accountStateRLP.length == 0){

            if (logger.isInfoEnabled())
                logger.info("No account: [ {} ]", Hex.toHexString(addr));
            return null;
        }

        AccountState state =  new AccountState(accountStateRLP);
        return state;
    }

    public ContractDetails getContractDetails(byte[] addr){

        byte[] accountDetailsRLP = contractDetailsDB.get(addr);

        if (accountDetailsRLP == null){

            if (logger.isInfoEnabled())
                logger.info("No account: [ {} ]", Hex.toHexString(addr));
            return null;
        }

        ContractDetails details = new ContractDetails(accountDetailsRLP);
        return details;
    }

    public BigInteger addBalance(byte[] address, BigInteger value){

        AccountState state = getAccountState(address);
        if (state == null) return BigInteger.ZERO;

        BigInteger newBalance = state.addToBalance(value);

        if (logger.isInfoEnabled())
            logger.info("Changing balance: account: [ {} ] new balance: [ {} ]",
                    Hex.toHexString(address), newBalance.toString());

        accountStateDB.update(address, state.getEncoded());

        return newBalance;
    }

    public BigInteger getBalance(byte[] address){

        AccountState state = getAccountState(address);
        if (state == null) return BigInteger.ZERO;

        return state.getBalance();
    }

    public BigInteger getNonce(byte[] address){

        AccountState state = getAccountState(address);
        if (state == null) return BigInteger.ZERO;

        return state.getNonce();
    }


    public BigInteger increaseNonce(byte[] address){

        AccountState state = getAccountState(address);
        if (state == null) return BigInteger.ZERO;

        state.incrementNonce();

        if (logger.isInfoEnabled())
            logger.info("Incerement nonce: account: [ {} ] new nonce: [ {} ]",
                    Hex.toHexString(address), state.getNonce().longValue());

        accountStateDB.update(address, state.getEncoded());
        return state.getNonce();
    }

    public void addStorageRow(byte[] address, DataWord key, DataWord value){

        if (address == null || key == null) return;
        AccountState       state = getAccountState(address);
        ContractDetails details = getContractDetails(address);

        if (state == null || details == null) return;
        details.put(key, value);

        byte[] storageHash = details.getStorageHash();
        state.setStateRoot(storageHash);

        if (logger.isInfoEnabled())
            logger.info("Storage key/value saved: account: [ {} ]\n key: [ {} ]  value: [ {} ]\n new storageHash: [ {} ]",
                    Hex.toHexString(address),
                    Hex.toHexString(key.getNoLeadZeroesData()),
                    Hex.toHexString(value.getNoLeadZeroesData()),
                    Hex.toHexString(storageHash));

        accountStateDB.update(address, state.getEncoded());
        contractDetailsDB.put(address, details.getEncoded());
    }

    public DataWord getStorageValue(byte[] address, DataWord key){

        if (key == null) return null;
        AccountState state = getAccountState(address);
        if (state == null) return null;

        ContractDetails details = getContractDetails(address);
        DataWord value = details.get(key);

        return value;
    }

    public byte[] getCode(byte[] address){

        ContractDetails details = getContractDetails(address);
        if (details == null) return null;

        return details.getCode();
    }

    public void saveCode(byte[] address, byte[] code){

        if (code == null) return;

        AccountState state = getAccountState(address);
        if (state == null) return;

        ContractDetails details = getContractDetails(address);
        details.setCode(code);

        byte[] codeHash = HashUtil.sha3(code);
        state.setCodeHash(codeHash);

        if (logger.isInfoEnabled())
            logger.info("Program code saved:\n account: [ {} ]\n codeHash: [ {} ] \n code: [ {} ]",
                    Hex.toHexString(address),
                    Hex.toHexString(codeHash),
                    Hex.toHexString(code));

        accountStateDB.update(address, state.getEncoded());
        contractDetailsDB.put(address, details.getEncoded());
    }

    public byte[] getRootHash(){
        return this.worldState.getRootHash();
    }


    public void close(){

        if (this.stateDB != null)
            stateDB.close();

        if (this.detailsDB != null)
            detailsDB.close();
    }

    public void dumpState(long blockNumber, int txNumber, String txHash){

        if (!CONFIG.dumpFull()) return;

        if (txHash == null)
        if (CONFIG.dumpCleanOnRestart()){
            try {FileUtils.deleteDirectory(CONFIG.dumpDir());} catch (IOException e) {}
        }

        String dir = CONFIG.dumpDir() + "/";

        String fileName = "0.dmp";
        if (txHash != null)
             fileName =
                String.format("%d_%d_%s.dmp",
                        blockNumber, txNumber, txHash.substring(0, 8));

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            FileWriter fw = new FileWriter(dumpFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            ArrayList<ByteArrayWrapper> keys =  this.detailsDB.dumpKeys();

            // dump json file
            for (ByteArrayWrapper key : keys){

                byte[] keyBytes = key.getData();
                AccountState    state    = getAccountState(keyBytes);
                ContractDetails details  = getContractDetails(keyBytes);

                BigInteger nonce   = state.getNonce();
                BigInteger balance = state.getBalance();

                byte[] stateRoot = state.getStateRoot();
                byte[] codeHash = state.getCodeHash();

                byte[] code = details.getCode();
                Map<DataWord, DataWord> storage = details.getStorage();

                String accountLine = JSONHelper.dumpLine(key.getData(), nonce.toByteArray(),
                        balance.toByteArray(), stateRoot, codeHash, code, storage);

                bw.write(accountLine);
                bw.write("\n");

    //            {address: x, nonce: n1, balance: b1, stateRoot: s1, codeHash: c1, code: c2, sotrage: [key: k1, value: v1, key:k2, value: v2 ] }
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
