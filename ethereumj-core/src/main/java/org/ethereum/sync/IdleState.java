package org.ethereum.sync;

import static org.ethereum.sync.SyncStateName.*;

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

        super.doOnTransition();

        syncManager.pool.changeState(IDLE);
    }

    @Override
    public void doMaintain() {

        super.doMaintain();

        if (!syncManager.queue.isHeadersEmpty()) {

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
