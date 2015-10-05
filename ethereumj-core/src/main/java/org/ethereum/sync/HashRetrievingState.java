package org.ethereum.sync;

import org.ethereum.net.server.Channel;
import org.ethereum.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.net.eth.EthVersion.*;
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

        // Since Eth V61 it makes sense to download blocks and hashes simultaneously

        final boolean master62 = master.getEthVersion().getCode() >= V62.getCode();

        if (master.getEthVersion().getCode() > V60.getCode()) {
            syncManager.pool.changeState(BLOCK_RETRIEVING, new Functional.Predicate<Channel>() {
                @Override
                public boolean test(Channel peer) {

                    if (master62) {
                        return peer.isIdle() && peer.getEthVersion().getCode() >= V62.getCode();
                    } else {
                        return peer.isIdle() && peer.getEthVersion().getCode() < V62.getCode();
                    }
                }
            });
        }
    }
}
