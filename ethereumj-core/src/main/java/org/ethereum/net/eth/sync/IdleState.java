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
            syncManager.changeState(BLOCK_RETRIEVING);
        }
    }
}
