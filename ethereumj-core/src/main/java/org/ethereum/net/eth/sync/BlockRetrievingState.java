package org.ethereum.net.eth.sync;

import org.ethereum.net.eth.EthHandler;
import org.ethereum.util.Functional;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class BlockRetrievingState extends AbstractSyncState {

    public BlockRetrievingState() {
        super(SyncStateName.BLOCK_RETRIEVING);
    }

    @Override
    public void doOnTransition() {
        syncManager.pool.changeState(SyncStateName.BLOCK_RETRIEVING);
    }

    @Override
    public void doMaintain() {
        if (syncManager.queue.isHashesEmpty()) {
            syncManager.changeState(SyncStateName.IDLE);
            return;
        }

        syncManager.pool.changeState(SyncStateName.BLOCK_RETRIEVING, new Functional.Predicate<EthHandler>() {
            @Override
            public boolean test(EthHandler peer) {
                return peer.isIdle();
            }
        });
    }
}
