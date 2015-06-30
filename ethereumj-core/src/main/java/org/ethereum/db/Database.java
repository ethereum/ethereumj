package org.ethereum.db;

/**
 * Ethereum generic database interface
 */
public interface Database {

    /**
     * Get value from database
     *
     * @param key for which to retrieve the value
     * @return the value for the given key
     */
    public byte[] get(byte[] key);

    /**
     * Insert value into database
     *
     * @param key for the given value
     * @param value to insert
     */
    public void put(byte[] key, byte[] value);

    /**
     * Delete key/value pair from database
     *
     * @param key for which to delete the value
     */
    public void delete(byte[] key);

    void init();

    /**
     * Close the database connection
     */
    public void close();
}
