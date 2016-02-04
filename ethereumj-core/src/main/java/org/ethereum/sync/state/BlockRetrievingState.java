package org.ethereum.sync.state;

import static org.ethereum.sync.state.SyncStateName.*;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class BlockRetrievingState extends AbstractSyncState {

    public BlockRetrievingState() {
        super(BLOCK_RETRIEVING);
    }

    @Override
    public void doOnTransition() {

        super.doOnTransition();

        pool.changeState(BLOCK_RETRIEVING);
    }

    @Override
    public void doMaintain() {

        super.doMaintain();

        if (queue.isHeadersEmpty()) {

            syncManager.changeState(IDLE);
            return;
        }

        pool.changeStateForIdles(BLOCK_RETRIEVING);
    }
}
