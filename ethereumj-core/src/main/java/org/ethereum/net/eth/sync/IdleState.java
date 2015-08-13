package org.ethereum.net.eth.sync;

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
        syncManager.pool.changeState(SyncStateName.IDLE);
    }

    @Override
    public void doMaintain() {
        if (!syncManager.queue.isHashesEmpty()) {
            syncManager.changeState(SyncStateName.BLOCK_RETRIEVING);
        }
    }
}
