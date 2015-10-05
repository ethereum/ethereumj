package org.ethereum.sync;

import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.server.Channel;
import org.ethereum.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.sync.SyncStateName.*;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public class BlockRetrievingState extends AbstractSyncState {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    public BlockRetrievingState() {
        super(BLOCK_RETRIEVING);
    }

    @Override
    public void doOnTransition() {

        super.doOnTransition();

        syncManager.pool.changeState(BLOCK_RETRIEVING);
    }

    @Override
    public void doMaintain() {

        super.doMaintain();

        boolean found61 = false;
        boolean found62 = false;

        Iterator<Channel> iter = syncManager.pool.iterator();

        if (!iter.hasNext()) {
            return;
        }

        while (iter.hasNext()) {

            Channel peer = iter.next();

            if (peer.getEthVersion().getCode() >= V62.getCode()) {
                found62 = true;
            } else {
                found61 = true;
            }

            if (found61 && found62) {
                break;
            }
        }

        if (!found61) {
            logger.trace("Clear hashes, no Eth V61 peers found");
            syncManager.queue.clearHashes();
        }

        if (!found62) {
            logger.trace("Clear headers, no Eth V62 peers found");
            syncManager.queue.clearHeaders();
        }

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
