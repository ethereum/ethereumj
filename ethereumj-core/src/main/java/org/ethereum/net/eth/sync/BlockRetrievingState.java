package org.ethereum.net.eth.sync;

import org.ethereum.net.server.Channel;
import org.ethereum.util.Functional;

import static org.ethereum.net.eth.sync.SyncStateName.*;

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
        syncManager.pool.changeState(BLOCK_RETRIEVING);
    }

    @Override
    public void doMaintain() {
        if (syncManager.queue.isHashesEmpty()) {
            syncManager.changeState(IDLE);
            return;
        }

        syncManager.pool.changeState(BLOCK_RETRIEVING, new Functional.Predicate<Channel>() {
            @Override
            public boolean test(Channel peer) {
                return peer.isIdle();
            }
        });
    }
}
