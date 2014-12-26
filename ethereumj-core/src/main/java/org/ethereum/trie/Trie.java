package org.ethereum.trie;

/**
 * Trie interface for the main data structure in Ethereum
 * which is used to store both the account state and storage of each account.
 */
public interface Trie {

    /**
     * Gets a value from the trie for a given key
     *
     * @param key - any length byte array
     * @return an rlp encoded byte array of the stored object
     */
    public byte[] get(byte[] key);

    /**
     * Insert or update a value in the trie for a specified key
     *
     * @param key - any length byte array
     * @param value rlp encoded byte array of the object to store
     */
    public void update(byte[] key, byte[] value);

    /**
     * Deletes a key/value from the trie for a given key
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

    /**
     * Discard all the changes until now
     */
    public void undo();

    public String getTrieDump();

    public boolean validate();

}