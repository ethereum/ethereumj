package org.ethereum.sync;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum SyncState {

    // Common
    IDLE,
    HASH_RETRIEVING,
    BLOCK_RETRIEVING,

    // Peer
    DONE_HASH_RETRIEVING
}
