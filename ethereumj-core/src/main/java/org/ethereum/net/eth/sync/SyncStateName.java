package org.ethereum.net.eth.sync;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum SyncStateName {

    // Common
    IDLE,
    HASH_RETRIEVING,
    BLOCK_RETRIEVING,

    // Peer
    DONE_HASH_RETRIEVING,
    BLOCKS_LACK
}
