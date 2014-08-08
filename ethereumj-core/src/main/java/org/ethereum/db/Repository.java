package org.ethereum.db;

import org.codehaus.plexus.util.FileUtils;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.crypto.HashUtil;
import org.ethereum.json.JSONHelper;
import org.ethereum.trie.TrackTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 *
 ***********************************************************************************
         Repository
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

    private Trie 			worldState;
    private TrackTrie     	accountStateDB;
    private TrackDatabase 	contractDetailsDB;

    // TODO: Listeners listeners
    // TODO: cash impl

    private DatabaseImpl chainDB 	= null;
    private DatabaseImpl detailsDB 	= null;
    private DatabaseImpl stateDB 	= null;
    
    /**
     * Create a new Repository DAO 
     * 		assuming empty db and thus no stateRoot
     * 
     * @See loadBlockchain() to update the stateRoot
     */
    public Repository() {
    	chainDB 			= new DatabaseImpl("blockchain");
        detailsDB     		= new DatabaseImpl("details");
        contractDetailsDB 	= new TrackDatabase(detailsDB);
        stateDB 			= new DatabaseImpl("state");
        worldState 			= new Trie(stateDB.getDb());
        accountStateDB 		= new TrackTrie(worldState);
    }

    private Repository(TrackTrie accountStateDB, TrackDatabase contractDetailsDB) {
        this.accountStateDB = accountStateDB;
        this.contractDetailsDB = contractDetailsDB;
    }

    public Repository getTrack() {
        TrackTrie     trackState   = new TrackTrie(accountStateDB);
        TrackDatabase trackDetails = new TrackDatabase(contractDetailsDB);
        return new Repository (trackState, trackDetails);
    }

    public void startTracking() {
        logger.debug("start tracking");
        accountStateDB.startTrack();
        contractDetailsDB.startTrack();
    }

    public void commit() {
        logger.debug("commit changes");
        accountStateDB.commitTrack();
        contractDetailsDB.commitTrack();
    }

    public void rollback() {
        logger.debug("rollback changes");
        accountStateDB.rollbackTrack();
        contractDetailsDB.rollbackTrack();
    }
    
    public Block getBlock(long blockNr) {
    	return new Block(chainDB.get(ByteUtil.longToBytes(blockNr)));
    }
    
    public void saveBlock(Block block) {
    	this.chainDB.put(ByteUtil.longToBytes(block.getNumber()), block.getEncoded());
    	this.worldState.sync();
    }
	
	public Blockchain loadBlockchain() {
		Blockchain blockchain = new Blockchain(this);
		DBIterator iterator = chainDB.iterator();
		try {
			if (!iterator.hasNext()) {
                logger.info("DB is empty - adding Genesis");
                for (String address : Genesis.getPremine()) {
            		this.createAccount(Hex.decode(address));
            		this.addBalance   (Hex.decode(address), Genesis.getPremineAmount());
				}
                blockchain.storeBlock(Genesis.getInstance());
               	logger.debug("Block #{} -> {}", Genesis.NUMBER, blockchain.getLastBlock().toFlatString());
               	dumpState(0, 0, null);
            } else {
            	logger.debug("Displaying blocks stored in DB sorted on blocknumber");

            	for (iterator.seekToFirst(); iterator.hasNext();) {
            		Block block = new Block(iterator.next().getValue());
            		blockchain.getBlockCache().put(block.getNumber(), block.getHash());
            		blockchain.setLastBlock(block);
    	            logger.debug("Block #{} -> {}", block.getNumber(), block.toFlatString());
            	}
				logger.info(
						"*** Loaded up to block [ {} ] with stateRoot [ {} ]",
						blockchain.getLastBlock().getNumber(), Hex
								.toHexString(blockchain.getLastBlock()
										.getStateRoot()));
            }
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		// Update world state to latest loaded block from db
		this.worldState.setRoot(blockchain.getLastBlock().getStateRoot());
		
		return blockchain;
	}
	

    public AccountState createAccount(byte[] addr) {

    	this.validateAddress(addr);
    	    	
        // 1. Save AccountState
        AccountState state =  new AccountState();
        accountStateDB.update(addr, state.getEncoded());

        // 2. Save ContractDetails
        ContractDetails details = new ContractDetails();
        contractDetailsDB.put(addr, details.getEncoded());

        if (logger.isDebugEnabled())
            logger.debug("New account created: [ {} ]", Hex.toHexString(addr));

        return state;
    }
    
    public Trie getWorldState() {
    	return worldState;
    }

    public AccountState getAccountState(byte[] addr) {

    	this.validateAddress(addr);

        byte[] accountStateRLP = accountStateDB.get(addr);

        if (accountStateRLP == null || accountStateRLP.length == 0)
            return null;

        AccountState state =  new AccountState(accountStateRLP);
        return state;
    }

	public ContractDetails getContractDetails(byte[] addr) {

		this.validateAddress(addr);

        if (logger.isDebugEnabled())
            logger.debug("Get contract details for: [ {} ]", Hex.toHexString(addr));

        byte[] accountDetailsRLP = contractDetailsDB.get(addr);
        
        if (accountDetailsRLP == null)
        	return null;
        	        			
        if (logger.isDebugEnabled())
            logger.debug("Contract details RLP: [ {} ]", Hex.toHexString(accountDetailsRLP));

		ContractDetails details = new ContractDetails(accountDetailsRLP);
		return details;
	}

	public BigInteger addBalance(byte[] addr, BigInteger value) {

		this.validateAddress(addr);
    	
		AccountState state = getAccountState(addr);

        if (state == null)
            state = createAccount(addr);
        
		BigInteger newBalance = state.addToBalance(value);

		if (logger.isDebugEnabled())
			logger.debug("Changing balance: account: [ {} ] new balance: [ {} ] delta: [ {} ]",
					Hex.toHexString(addr), newBalance.toString(), value);

		accountStateDB.update(addr, state.getEncoded());
		return newBalance;
	}

    public BigInteger getBalance(byte[] addr) {
    	this.validateAddress(addr);
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        return state.getBalance();
    }

    public BigInteger getNonce(byte[] addr) {
    	this.validateAddress(addr);
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        return state.getNonce();
    }

    public BigInteger increaseNonce(byte[] addr) {
		
    	this.validateAddress(addr);
    	
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        state.incrementNonce();

        if (logger.isDebugEnabled())
            logger.debug("Incerement nonce: account: [ {} ] new nonce: [ {} ]",
                    Hex.toHexString(addr), state.getNonce().longValue());

        accountStateDB.update(addr, state.getEncoded());
        return state.getNonce();
    }

	public void addStorageRow(byte[] addr, DataWord key, DataWord value) {

        if (key == null) return;
        this.validateAddress(addr);

        AccountState      state = getAccountState(addr);
        ContractDetails   details = getContractDetails(addr);

        if (state == null || details == null) return;
        details.put(key, value);

        byte[] storageHash = details.getStorageHash();
        state.setStateRoot(storageHash);

        if (logger.isDebugEnabled())
            logger.debug("Storage key/value saved: account: [ {} ]\n key: [ {} ]  value: [ {} ]\n new storageHash: [ {} ]",
                    Hex.toHexString(addr),
                    Hex.toHexString(key.getNoLeadZeroesData()),
                    Hex.toHexString(value.getNoLeadZeroesData()),
                    Hex.toHexString(storageHash));

        accountStateDB.update(addr, state.getEncoded());
        contractDetailsDB.put(addr, details.getEncoded());
    }

    public DataWord getStorageValue(byte[] addr, DataWord key) {

        if (key == null) return null;
        this.validateAddress(addr);

        AccountState state = getAccountState(addr);
        if (state == null) return null;

        ContractDetails details = getContractDetails(addr);
        DataWord value = details.get(key);

        return value;
    }

    public byte[] getCode(byte[] addr) {

    	this.validateAddress(addr);
        ContractDetails details = getContractDetails(addr);
        if (details == null) return null;
        return details.getCode();
    }

    public void saveCode(byte[] addr, byte[] code) {
    	
    	if (code == null) return;
        this.validateAddress(addr);
        
        if (logger.isDebugEnabled())
            logger.debug("saveCode: \n address: [ {} ], \n code: [ {} ]",
                    Hex.toHexString(addr),
                    Hex.toHexString(code));


        AccountState state = getAccountState(addr);
        if (state == null) return;

        ContractDetails details = getContractDetails(addr);
        details.setCode(code);

        byte[] codeHash = HashUtil.sha3(code);
        state.setCodeHash(codeHash);

        if (logger.isDebugEnabled())
            logger.debug("Program code saved:\n account: [ {} ]\n codeHash: [ {} ] \n code: [ {} ]",
                    Hex.toHexString(addr),
                    Hex.toHexString(codeHash),
                    Hex.toHexString(code));

        accountStateDB.update(addr, state.getEncoded());
        contractDetailsDB.put(addr, details.getEncoded());

        if (logger.isDebugEnabled())
            logger.debug("saveCode: \n accountState: [ {} ], \n contractDetails: [ {} ]",
                    Hex.toHexString(state.getEncoded()),
                    Hex.toHexString(details.getEncoded()));
    }

    public void delete(byte[] addr) {

    	this.validateAddress(addr);
        accountStateDB.delete(addr);
        contractDetailsDB.delete(addr);
    }

    public List<ByteArrayWrapper> dumpKeys(){
        return stateDB.dumpKeys();
    }

    public void dumpState(long blockNumber, int txNumber, byte[] txHash) {

        if (!CONFIG.dumpFull()) return;

        // todo: dump block header and the relevant tx

        if (blockNumber == 0 && txNumber == 0)
            if (CONFIG.dumpCleanOnRestart()) {
                try {FileUtils.deleteDirectory(CONFIG.dumpDir());} catch (IOException e) {}
            }

        String dir = CONFIG.dumpDir() + "/";

        String fileName = "";
        if (txHash != null)
             fileName = String.format("%d_%d_%s.dmp",
                        	blockNumber, txNumber, Hex.toHexString(txHash).substring(0, 8));
        else
            fileName = String.format("%d_c.dmp", blockNumber);

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            List<ByteArrayWrapper> keys = this.detailsDB.dumpKeys();

            // dump json file
            for (ByteArrayWrapper key : keys) {

                byte[] keyBytes = key.getData();
                AccountState    state    = getAccountState(keyBytes);
                ContractDetails details  = getContractDetails(keyBytes);

                BigInteger nonce   = (state != null)? state.getNonce():null;
                BigInteger balance = (state != null)? state.getBalance():null;

                byte[] stateRoot = (state != null)? state.getStateRoot():null;
                byte[] codeHash = (state != null)? state.getCodeHash():null;

                byte[] code = details.getCode();
                Map<DataWord, DataWord> storage = details.getStorage();

                String accountLine = JSONHelper.dumpLine(key.getData(),
                        (nonce != null)? BigIntegers.asUnsignedByteArray(nonce) : null,
                        (nonce != null)? BigIntegers.asUnsignedByteArray(balance): null,
                        stateRoot, codeHash, code, storage);

                bw.write(accountLine);
                bw.write("\n");
            }

			String rootHash = Hex.toHexString(this.getWorldState().getRootHash());
            bw.write(
                    String.format(" => Global State Root: [ %s ]", rootHash)
            );

        } catch (IOException e) {
        	logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null)bw.close();
                if (fw != null)fw.close();
            } catch (IOException e) {e.printStackTrace();}
        }
    }

    public void close() {
        if (this.chainDB != null)
        	chainDB.close();
        if (this.stateDB != null)
            stateDB.close();
        if (this.detailsDB != null)
            detailsDB.close();
    }

    private void validateAddress(byte[] addr) {
		 if (addr == null || addr.length < 20) {
			logger.error("Can't create address {} because is null or length != 20", ByteUtil.toHexString(addr));
			throw new IllegalArgumentException("Address must be a byte-array of length 20");
		}
    }
}
