package org.ethereum.net.eth;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum SyncState {

    // Sync manager
    INIT,
    DONE_SYNC,
    GAP_RECOVERY,
    DONE_GAP_RECOVERY,

    // Common
    HASH_RETRIEVING,
    BLOCK_RETRIEVING,

    // Peer
    IDLE,
    DONE_HASH_RETRIEVING,
    NO_MORE_BLOCKS
}
