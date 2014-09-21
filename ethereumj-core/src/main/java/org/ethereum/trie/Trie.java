package org.ethereum.trie;

/**
 * Trie interface for the main data structure in Ethereum 
 * which is used to store both the account state and storage of each account.
 */
public interface Trie {

	/**
	 * Adds or updates a value to the trie for the specified key
	 * 
	 * @param key - any length byte array
	 * @param value - an rlp encoded byte representation of the object to store
	 */
    public void update(byte[] key, byte[] value);
    
    /**
     * Gets a value from the trie for a given key
     *   
     * @param key - any length byte array
     * @return value - an rlp encoded byte array of the stored object
     */
    public byte[] get(byte[] key);
    
    /**
     * Deletes a value from the trie for a given key
     * 
     * @param key - any length byte array
     */
    public void delete(byte[] key);
    
    /**
     * Returns a SHA-3 hash from the top node of the trie
     * 
     * @return 32-byte SHA-3 hash representing the entire contents of the trie. 
     */
    public byte[] getRootHash();
    
    /**
     * Set the top node of the trie
     *  
     * @param root - 32-byte SHA-3 hash of the root node
     */
    public void setRoot(byte[] root);
    
    /**
     * Commit all the changes until now
     */
    public void sync();
    
    public String getTrieDump();
}