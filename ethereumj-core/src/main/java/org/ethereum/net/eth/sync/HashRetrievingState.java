package org.ethereum.net.eth.sync;

import org.ethereum.net.eth.EthHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class HashRetrievingState extends AbstractSyncState {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    public HashRetrievingState() {
        super(SyncStateName.HASH_RETRIEVING);
    }

    @Override
    public void doMaintain() {
        EthHandler master = null;
        for (EthHandler peer : syncManager.pool) {
            // if hash retrieving is done all we need to do is just change state and quit
            if (peer.isHashRetrievingDone()) {
                syncManager.changeState(SyncStateName.BLOCK_RETRIEVING);
                return;
            }

            // master is found
            if (peer.isHashRetrieving()) {
                master = peer;
                break;
            }
        }

        if (master != null) {
            // if master is stuck ban it and try to start a new one
            if(syncManager.isPeerStuck(master)) {
                syncManager.pool.ban(master);
                syncManager.pool.remove(master);
                logger.info("Master peer {}: banned due to stuck timeout exceeding", master.getPeerIdShort());
                master = null;
            }
        }

        if (master == null) {
            logger.trace("HASH_RETRIEVING is in progress, starting master peer");
            master = syncManager.pool.getBest();
            if (master == null) {
                return;
            }
            syncManager.startMaster(master);
        }
    }
}
