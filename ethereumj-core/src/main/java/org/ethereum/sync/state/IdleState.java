package org.ethereum.sync.state;

import static org.ethereum.sync.state.SyncStateName.*;

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

        pool.changeState(IDLE);
    }

    @Override
    public void doMaintain() {

        super.doMaintain();

        if (!queue.isHeadersEmpty()) {

            // there are new hashes in the store
            // it's time to download blocks
            syncManager.changeState(BLOCK_RETRIEVING);

        } else if (queue.isBlocksEmpty() && !syncManager.isSyncDone()) {

            // queue is empty and sync not done yet
            // try to download hashes again
            syncManager.resetGapRecovery();
            syncManager.changeState(HASH_RETRIEVING);
        }
    }
}
