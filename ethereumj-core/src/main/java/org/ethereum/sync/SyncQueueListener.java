package org.ethereum.sync;


import org.ethereum.core.BlockWrapper;

/**
 * SyncQueue events
 *
 * @author Tiberius Iliescu
 */
public interface SyncQueueListener {

    void onInvalidBlock(byte[] nodeId);

    void onNewBlockImported(BlockWrapper blockWrapper);

    void onNoParentBlock(BlockWrapper blockWrapper);
}
