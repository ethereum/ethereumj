package org.ethereum.net.eth;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public enum SyncState {
    INIT,
    HASHES_RETRIEVING,
    BLOCKS_RETRIEVING,
    DONE
}
