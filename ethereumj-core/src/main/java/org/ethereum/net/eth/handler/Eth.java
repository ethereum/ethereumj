package org.ethereum.net.eth.handler;

import org.ethereum.core.Transaction;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.sync.SyncStateName;
import org.ethereum.net.eth.sync.SyncStatistics;

/**
 * Describes interface required by Eth peer clients
 *
 * @see org.ethereum.net.server.Channel
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public interface Eth {

    /**
     * @return true if StatusMessage was processed, false otherwise
     */
    boolean hasStatusPassed();

    /**
     * @return true if Status has succeeded
     */
    boolean hasStatusSucceeded();

    /**
     * Executes cleanups required to be done
     * during shutdown, e.g. disconnect
     */
    void onShutdown();

    /**
     * Puts sync statistics to log output
     */
    void logSyncStats();

    /**
     * Changes Sync state to the new one
     *
     * @param newState new state
     */
    void changeState(SyncStateName newState);

    /**
     * @return true if syncState is BLOCKS_LACK, false otherwise
     */
    boolean hasBlocksLack();

    /**
     * @return true if syncState is DONE_HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrievingDone();

    /**
     * @return true if syncState is HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrieving();

    /**
     * @return true if syncState is IDLE, false otherwise
     */
    boolean isIdle();

    /**
     * Sets maxHashesAsk param for GET_BLOCK_HASHES message
     *
     * @param maxHashesAsk maxHashesAsk value
     */
    void setMaxHashesAsk(int maxHashesAsk);

    /**
     * @return current value of maxHashesAsk param
     */
    int getMaxHashesAsk();

    /**
     * Sets last hash to be asked from the peer
     *
     * @param lastHashToAsk terminal hash
     */
    void setLastHashToAsk(byte[] lastHashToAsk);

    /**
     * @return lastHashToAsk value
     */
    byte[] getLastHashToAsk();

    /**
     * @return best hash (that we're aware of) known by the peer
     */
    byte[] getBestKnownHash();

    /**
     * @return sync statistics
     */
    SyncStatistics getStats();

    /**
     * Disables pending transaction processing
     */
    void disableTransactions();

    /**
     * Enables pending transaction processing
     */
    void enableTransactions();

    /**
     * Sends transaction to the wire
     *
     * @param tx sending transaction
     */
    void sendTransaction(Transaction tx);

    /**
     * @return protocol version
     */
    EthVersion getVersion();
}
