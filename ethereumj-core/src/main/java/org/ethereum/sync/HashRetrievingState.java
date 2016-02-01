package org.ethereum.sync;

import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.sync.SyncStateName.*;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class HashRetrievingState extends AbstractSyncState {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    public HashRetrievingState() {
        super(HASH_RETRIEVING);
    }

    @Override
    public void doMaintain() {

        super.doMaintain();

        // stop header downloading if we don't need more blocks
        if ((!syncManager.isSyncDone() || !queue.noParent) && !queue.isMoreBlocksNeeded()) {
            logger.info("Blockqueue limit exceeded, process downloaded blocks");
            syncManager.changeState(BLOCK_RETRIEVING);
            return;
        }

        Channel master = null;
        for (Channel peer : syncManager.pool) {
            // if hash retrieving is done all we need to do is just change state and quit
            if (peer.isHashRetrievingDone()) {
                syncManager.changeState(BLOCK_RETRIEVING);
                return;
            }

            // master is found
            if (peer.isHashRetrieving()) {
                master = peer;
                break;
            }
        }

        if (master != null) {
            // if master is stuck ban it and process data it sent
            if(syncManager.isPeerStuck(master)) {
                syncManager.pool.ban(master);
                logger.info("Master peer {}: banned due to stuck timeout exceeding", master.getPeerIdShort());

                // let's see what do we have
                // before proceed with HASH_RETRIEVING
                syncManager.changeState(BLOCK_RETRIEVING);
                return;
            }
        }

        if (master == null) {
            logger.trace("HASH_RETRIEVING is in progress, starting master peer");

            // recovering gap with gap block peer
            if (syncManager.getGapBlock() != null) {
                master = syncManager.pool.getByNodeId(syncManager.getGapBlock().getNodeId());
            }

            if (master == null) {
                master = syncManager.pool.getMaster();
            }

            if (master == null) {
                return;
            }
            syncManager.startMaster(master);
        }

        // Downloading blocks and headers simultaneously
        syncManager.pool.changeStateForIdles(BLOCK_RETRIEVING);
    }
}
