package org.ethereum.net.eth.sync;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum SyncStatus {
    INIT,
    HASH_RETRIEVING,
    HASHES_RETRIEVED,
    BLOCK_RETRIEVING,
    BLOCKS_RETRIEVED,
    SYNC_DONE
}
