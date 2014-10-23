package org.ethereum.db;

import org.codehaus.plexus.util.FileUtils;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.json.EtherObjectMapper;
import org.ethereum.json.JSONHelper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.TrackTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

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
public class RepositoryImpl implements Repository {

    private static final Logger logger = LoggerFactory.getLogger("repository");

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
    public RepositoryImpl() {
    	this("blockchain", "details", "state");
    }
    
    public RepositoryImpl(String blockChainDbName, String detailsDbName, String stateDbName) {
    	chainDB 			= new DatabaseImpl(blockChainDbName);
        detailsDB     		= new DatabaseImpl(detailsDbName);
        contractDetailsDB 	= new TrackDatabase(detailsDB);
        stateDB 			= new DatabaseImpl(stateDbName);
        worldState 			= new TrieImpl(stateDB.getDb());
        accountStateDB 		= new TrackTrie(worldState);
    }

    private RepositoryImpl(TrackTrie accountStateDB, TrackDatabase contractDetailsDB) {
        this.accountStateDB = accountStateDB;
        this.contractDetailsDB = contractDetailsDB;
    }

    public Repository getTrack() {
        TrackTrie     trackState   = new TrackTrie(accountStateDB);
        TrackDatabase trackDetails = new TrackDatabase(contractDetailsDB);
        return new RepositoryImpl(trackState, trackDetails);
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
        byte[] raw = chainDB.get(ByteUtil.longToBytes(blockNr));
        if (raw == null) return null;
    	return new Block(raw);
    }
    
    public void saveBlock(Block block) {
    	this.chainDB.put(ByteUtil.longToBytes(block.getNumber()), block.getEncoded());

//        this.worldState.cleanCacheGarbage();
    	this.worldState.sync();
    }
	
	public Blockchain loadBlockchain() {
		Blockchain blockchain = WorldManager.getInstance().getBlockchain();
		DBIterator iterator = chainDB.iterator();
		try {
			if (!iterator.hasNext()) {
                logger.info("DB is empty - adding Genesis");
                for (String address : Genesis.getPremine()) {
            		this.createAccount(Hex.decode(address));
            		this.addBalance   (Hex.decode(address), Genesis.PREMINE_AMOUNT);
				}
                blockchain.storeBlock(Genesis.getInstance());

                EthereumListener listener =  WorldManager.getInstance().getListener();
                if (listener != null){
                    listener.onPreloadedBlock(Genesis.getInstance());
                }

               	logger.debug("Block #{} -> {}", Genesis.NUMBER, blockchain.getLastBlock().toFlatString());
               	dumpState(Genesis.getInstance(), 0, 0, null);
            } else {
            	logger.debug("Displaying blocks stored in DB sorted on blocknumber");

            	for (iterator.seekToFirst(); iterator.hasNext();) {
            		Block block = new Block(iterator.next().getValue());
            		blockchain.getBlockCache().put(block.getNumber(), block.getHash());
            		blockchain.setLastBlock(block);
            		blockchain.updateTotalDifficulty(block);
                    EthereumListener listener =  WorldManager.getInstance().getListener();
                    if (listener != null){
                        listener.onPreloadedBlock(block);
                    }
    	            logger.debug("Block #{} -> {}", block.getNumber(), block.toFlatString());
            	}
				logger.info("*** Loaded up to block [{}] with stateRoot [{}]", 
						blockchain.getLastBlock().getNumber(), 
						Hex.toHexString(blockchain.getLastBlock().getStateRoot()));
            }
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

        if (CONFIG.rootHashStart() != null){

            // update world state by dummy hash
            byte[] rootHash = Hex.decode(CONFIG.rootHashStart());
            logger.info("Loading root hash from property file: [{}]", CONFIG.rootHashStart());
            this.worldState.setRoot(rootHash);

        } else{

            // Update world state to latest loaded block from db
            this.worldState.setRoot(blockchain.getLastBlock().getStateRoot());
        }

		return blockchain;
	}
	

    public AccountState createAccount(byte[] addr) {

        logger.trace("createAccount: [{}]", Hex.toHexString(addr)) ;
    	this.validateAddress(addr);
    	    	
        // 1. Save AccountState
        AccountState state =  new AccountState();
        accountStateDB.update(addr, state.getEncoded());
        
        ContractDetails details = new ContractDetails();
        contractDetailsDB.put(addr, details.getEncoded());
        
        if (logger.isDebugEnabled())
            logger.debug("New account created: [{}]", Hex.toHexString(addr));

        return state;
    }
    
    public Trie getWorldState() {
    	return worldState;
    }

    public AccountState getAccountState(byte[] addr) {

        if (logger.isDebugEnabled())
            logger.debug("Get account state for: [{}]", Hex.toHexString(addr));

    	this.validateAddress(addr);

        byte[] accountStateRLP = accountStateDB.get(addr);

        if (logger.isDebugEnabled())
            logger.debug("Found account state RLP: [{}]", Hex.toHexString(accountStateRLP));

        if (accountStateRLP == null || accountStateRLP.length == 0)
            return null;

        AccountState state =  new AccountState(accountStateRLP);
        return state;
    }

	public ContractDetails getContractDetails(byte[] addr) {

		this.validateAddress(addr);

        if (logger.isDebugEnabled())
            logger.debug("Get contract details for: [{}]", Hex.toHexString(addr));

        byte[] accountDetailsRLP = contractDetailsDB.get(addr);
        
        if (accountDetailsRLP == null)
        	return null;
        	        			
        if (logger.isDebugEnabled())
            logger.debug("Found contract details RLP: [{}]", Hex.toHexString(accountDetailsRLP));

		ContractDetails details = new ContractDetails(accountDetailsRLP);
		return details;
	}

	public BigInteger addBalance(byte[] addr, BigInteger value) {
    	
		AccountState state = getAccountState(addr);

        if (state == null)
            state = createAccount(addr);
        
		BigInteger newBalance = state.addToBalance(value);

		if (logger.isDebugEnabled())
			logger.debug("Changing balance: \n account:\t [{}]\n new balance:\t [{}]\n delta:\t\t [{}]",
					Hex.toHexString(addr), newBalance.toString(), value);

		accountStateDB.update(addr, state.getEncoded());
		return newBalance;
	}

    public BigInteger getBalance(byte[] addr) {
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        return state.getBalance();
    }

    public BigInteger getNonce(byte[] addr) {
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        return state.getNonce();
    }

    public BigInteger increaseNonce(byte[] addr) {
        AccountState state = getAccountState(addr);
        if (state == null) return BigInteger.ZERO;
        state.incrementNonce();

        if (logger.isDebugEnabled())
            logger.debug("Increment nonce:\n account:\t [{}]\n new nonce:\t [{}]",
                    Hex.toHexString(addr), state.getNonce().longValue());

        accountStateDB.update(addr, state.getEncoded());
        return state.getNonce();
    }

	public void addStorageRow(byte[] addr, DataWord key, DataWord value) {

        if (key == null) return;
        AccountState      state = getAccountState(addr);
        ContractDetails   details = getContractDetails(addr);

        if (state == null || details == null) return;
        details.put(key, value);

        byte[] storageHash = details.getStorageHash();
        state.setStateRoot(storageHash);

        if (logger.isDebugEnabled())
            logger.debug("Storage key/value saved:\n account:\t [{}]\n key:\t\t [{}]\n value:\t\t [{}]\n new hash:\t [{}]",
                    Hex.toHexString(addr),
                    Hex.toHexString(key.getNoLeadZeroesData()),
                    Hex.toHexString(value.getNoLeadZeroesData()),
                    Hex.toHexString(storageHash));

        accountStateDB.update(addr, state.getEncoded());
        contractDetailsDB.put(addr, details.getEncoded());
    }

    public DataWord getStorageValue(byte[] addr, DataWord key) {

        if (key == null) return null;

        AccountState state = getAccountState(addr);
        if (state == null) return null;

        ContractDetails details = getContractDetails(addr);
        DataWord value = details.get(key);

        return value;
    }

    public byte[] getCode(byte[] addr) {
        ContractDetails details = getContractDetails(addr);
        if (details == null) return null;
        return details.getCode();
    }

    public void saveCode(byte[] addr, byte[] code) {
    	
    	if (code == null) return;
        AccountState state = getAccountState(addr);
        if (state == null) return;
        
        if (logger.isDebugEnabled())
            logger.debug("Saving code: \n address:\t [{}], \n code:\t\t [{}]",
                    Hex.toHexString(addr),
                    Hex.toHexString(code));

        ContractDetails details = getContractDetails(addr);
        details.setCode(code);

        byte[] codeHash = HashUtil.sha3(code);
        state.setCodeHash(codeHash);

        accountStateDB.update(addr, state.getEncoded());
        contractDetailsDB.put(addr, details.getEncoded());
        
        if (logger.isDebugEnabled())
            logger.debug("Code saved: \n accountstate:\t [{}]\n codeHash:\t [{}]\n details RLP:\t [{}]",
                    Hex.toHexString(state.getEncoded()),
                    Hex.toHexString(codeHash),
                    Hex.toHexString(details.getEncoded()));
    }

    public void delete(byte[] addr) {
    	this.validateAddress(addr);
        accountStateDB.delete(addr);
        contractDetailsDB.delete(addr);
    }

    public List<ByteArrayWrapper> dumpKeys() {
        return stateDB.dumpKeys();
    }

    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {

		if (!(CONFIG.dumpFull() || CONFIG.dumpBlock() == block.getNumber()))
			return;

        // todo: dump block header and the relevant tx

        if (block.getNumber() == 0 && txNumber == 0)
            if (CONFIG.dumpCleanOnRestart()) {
                try {FileUtils.deleteDirectory(CONFIG.dumpDir());} catch (IOException e) {}
            }

        String dir = CONFIG.dumpDir() + "/";

        String fileName = "";
        if (txHash != null)
			fileName = String.format("%07d_%d_%s.dmp", block.getNumber(), txNumber,
					Hex.toHexString(txHash).substring(0, 8));
		else {
			fileName = String.format("%07d_c.dmp", block.getNumber());
		}

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            List<ByteArrayWrapper> keys = this.detailsDB.dumpKeys();
            
            JsonNodeFactory jsonFactory = new JsonNodeFactory(false);
            ObjectNode blockNode = jsonFactory.objectNode();
            JSONHelper.dumpBlock(blockNode, block, gasUsed,
                    this.getWorldState().getRootHash(),
                    keys, this);
            
            EtherObjectMapper mapper = new EtherObjectMapper();
            bw.write(mapper.writeValueAsString(blockNode));

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


    public void dumpTrie(Block block){

        if (!(CONFIG.dumpFull() || CONFIG.dumpBlock() == block.getNumber()))
            return;

        String fileName = String.format("%07d_trie.dmp", block.getNumber());
        String dir = CONFIG.dumpDir() + "/";
        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        FileWriter fw = null;
        BufferedWriter bw = null;

        String dump = this.getWorldState().getTrieDump();

        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            fw = new FileWriter(dumpFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            bw.write(dump);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null)bw.close();
                if (fw != null)fw.close();
            } catch (IOException e) {e.printStackTrace();}
        }
    }

    public DBIterator getAccountsIterator() {
    	return detailsDB.iterator();
    }

    public boolean isClosed(){
        return chainDB == null;
    }

    public void close() {
        if (this.detailsDB != null){
            detailsDB.close();
            detailsDB = null;
        }
        if (this.stateDB != null){
            stateDB.close();
            stateDB = null;
        }
        if (this.chainDB != null){
            chainDB.close();
            chainDB = null;
        }
    }

    private void validateAddress(byte[] addr) {
		 if (addr == null || addr.length < 20) {
			logger.error("Can't create address {} because is null or length != 20", ByteUtil.toHexString(addr));
			throw new IllegalArgumentException("Address must be a byte-array of length 20");
		}
    }
}

