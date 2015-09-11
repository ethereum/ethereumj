package org.ethereum.net.eth.sync;

import static org.ethereum.net.eth.sync.SyncStateName.*;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class IdleState extends AbstractSyncState {

    public IdleState() {
        super(SyncStateName.IDLE);
    }

    @Override
    public void doOnTransition() {
        syncManager.pool.changeState(IDLE);
    }

    @Override
    public void doMaintain() {

        if (!syncManager.queue.isHashesEmpty()) {

            // there are new hashes in the store
            // it's time to download blocks
            syncManager.changeState(BLOCK_RETRIEVING);

        } else if (syncManager.queue.isBlocksEmpty() && !syncManager.isSyncDone()) {

            // queue is empty and sync not done yet
            // try to download hashes again
            syncManager.resetGapRecovery();
            syncManager.changeState(HASH_RETRIEVING);

        }
    }
}
