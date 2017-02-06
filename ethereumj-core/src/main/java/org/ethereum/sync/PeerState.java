package org.ethereum.sync;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum PeerState {

    // Common
    IDLE,
    HEADER_RETRIEVING,
    BLOCK_RETRIEVING,
    NODE_RETRIEVING,
    RECEIPT_RETRIEVING,
    MANIFEST_RETRIEVING,
    SNAPSHOT_DATA_RETRIEVING,

    // Peer
    DONE_HASH_RETRIEVING
}
