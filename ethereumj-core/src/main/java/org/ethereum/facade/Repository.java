package org.ethereum.facade;

import java.math.BigInteger;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.db.ContractDetails;
import org.ethereum.trie.Trie;
import org.ethereum.vm.DataWord;
import org.iq80.leveldb.DBIterator;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 08/09/2014 10:25
 */

public interface Repository {

	/**
	 * Create a new account in the database
	 *  
	 * @param addr of the contract
	 * @return newly created account state
	 */
	public AccountState createAccount(byte[] addr);
	
	/**
	 * Retrieve an account
	 * 
	 * @param addr of the account
	 * @return account state as stored in the database
	 */
    public AccountState getAccountState(byte[] addr);
    
    /**
     * Deletes the account
     * 
     * @param addr of the account
     */
    public void delete(byte[] addr);
    
    /**
     * Increase the account nonce of the given account by one
     * 
     * @param addr of the account
     * @return new value of the nonce
     */
    public BigInteger increaseNonce(byte[] addr);
    
    /**
     * Get current nonce of a given account
     * 
     * @param addr of the account
     * @return value of the nonce
     */
    public BigInteger getNonce(byte[] addr);
      
    /**
     * Retrieve contract details for a given account from the database
     * 
     * @param addr of the account
     * @return new contract details 
     */
    public ContractDetails getContractDetails(byte[] addr);
    
    /**
     * Store code associated with an account
     * 
     * @param addr for the account
     * @param code that will be associated with this account
     */
    public void saveCode(byte[] addr, byte[] code);
    
    /**
     * Retrieve the code associated with an account
     * 
     * @param addr of the account
     * @return code in byte-array format
     */
    public byte[] getCode(byte[] addr);
    
    /**
     * Put a value in storage of an account at a given key
     * 
     * @param addr of the account
     * @param key of the data to store
     * @param value is the data to store
     */
    public void addStorageRow(byte[] addr, DataWord key, DataWord value);
    
    /**
     * Retrieve storage value from an account for a given key
     * 
     * @param addr of the account
     * @param key associated with this value
     * @return data in the form of a <code>DataWord</code>
     */
    public DataWord getStorageValue(byte[] addr, DataWord key);
    
    /**
     * Save block and post state in the database
     *     
     * @param block the <code>Block</code> to store
     */
    public void saveBlock(Block block);

    /**
     * Retrieve block from the blockchain
     * 
     * @param blockNr number of block in the blockchain
     * @return Block containing header, uncles and transactions
     */
    public Block getBlock(long blockNr);
    
    /**
     * Retrieve balance of an account
     * 
     * @param addr of the account
     * @return balance of the account as a <code>BigInteger</code> value
     */
    public BigInteger getBalance(byte[] addr);
    
    /**
     * Add value to the balance of an account
     * 
     * @param addr of the account
     * @param value to be added
     * @return new balance of the account
     */
    public BigInteger addBalance(byte[] addr, BigInteger value);
    
    /**
     * Returns an iterator over the accounts in this database in proper sequence
     * 
     * @return an iterator over the accounts in this database in proper sequence
     */
    public DBIterator getAccountsIterator();
    
    /**
     * Return the current state as the Trie data structure
     * 
     * @return the <code>Trie</code> representing the entire current state
     */
    public Trie getWorldState();
    
    /**
     * Load the blockchain into cache memory
     * 
     * @return the <code>Blockchain</code> object
     */
    public Blockchain loadBlockchain();
    
    /**
     * Dump the full state of the current repository into a file with JSON format
     * It contains all the contracts/account, their attributes and 
     *   
     * @param block of the current state
     * @param gasUsed the amount of gas used in the block until that point
     * @param txNumber is the number of the transaction for which the dump has to be made
     * @param txHash is the hash of the given transaction. 
     * 		If null, the block state post coinbase reward is dumped.
     */
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash);  
   
    /**
     *  Start tracking the database changes 
     */
    public void startTracking();
    
    /**
     * Return a repository snapshot of the current state
     * 
     * @return the repository in its current state
     */
    public Repository getTrack();

    /**
     * Store all the temporary changes made 
     * to the repository in the actual database
     */
    public void commit();
    
    /**
     * Undo all the changes made so far 
     * to a snapshot of the repository
     */
    public void rollback();
    
    /**
     * Check to see if the current repository has an open connection to the database
     * @return <tt>true</tt> if connection to database is open
     */
    public boolean isClosed();
    
    /**
     * Close the database
     */
    public void close();
}
