package org.ethereum.sync.listener;

import org.ethereum.core.BlockWrapper;

/**
 * Interfaces for listener used by different sync part
 * to communicate with each other
 *
 * @author Mikhail Kalinin
 * @since 04.02.2016
 */
public interface SyncListener {

    /**
     * Triggered when new headers are added to the queue
     */
    void onHeadersAdded();

    /**
     * Triggered when new block or its number is received
     *
     * @param number number of the block
     */
    void onNewBlockNumber(long number);

    /**
     * Triggered when Blockchain is not able to import block
     * due to parent absence
     *
     * @param block block which has no parent
     */
    void onNoParent(BlockWrapper block);
}
