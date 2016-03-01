package org.ethereum.sync.strategy;

import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.net.server.Channel;
import org.ethereum.sync.SyncState;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Thread.sleep;
import static org.ethereum.sync.SyncState.BLOCK_RETRIEVING;
import static org.ethereum.sync.SyncState.HASH_RETRIEVING;
import static org.ethereum.sync.SyncState.IDLE;
import static org.ethereum.util.BIUtil.isIn20PercentRange;

/**
 * Implements long sync algorithm <br/>
 *
 * It starts to act each time VM is started
 * and tries to reach the end of block chain <br/>
 *
 * After it reaches the end or block near to it
 * it's switched to Short Sync algorithm
 *
 * @author Mikhail Kalinin
 * @since 02.02.2016
 */
@Component
public class LongSync extends AbstractSyncStrategy {

    private static final int ROTATION_LIMIT = 100;

    private SyncState state = HASH_RETRIEVING;

    @Override
    public SyncState getState() {
        return state;
    }

    @Override
    public void start() {

        super.start();

        Thread headerProducer = new Thread(new Runnable() {
            @Override
            public void run() {
                produceHeaders();
            }
        });
        headerProducer.start();
    }

    private void produceHeaders() {

        while (inProgress()) {

            List<BlockHeaderWrapper> headers = null;

            try {
                headers = queue.takeHeaders();

                if (headers.isEmpty()) continue;

                Channel peer;
                while (null == (peer = pool.getBestIdle())) {
                    sleep(100);
                }

                peer.fetchBlockBodies(headers);

            } catch (Throwable t) {
                if (headers == null || headers.isEmpty()) {
                    logger.error("Error processing headers, {}", t);
                } else {
                    logger.error("Error processing headers: {}...{}, {}",
                            headers.get(0).getHexStrShort(), headers.get(headers.size() - 1).getHexStrShort(), t);
                }
            }
        }
    }


    @Override
    protected void doWork() {

        logger.trace("Maintain {} state", state);

        switch (state) {
            case HASH_RETRIEVING:   doHeaders(); break;
            case BLOCK_RETRIEVING:  doBodies(); break;
            case IDLE:              doIdle(); break;
        }
    }

    private void doHeaders() {

        if (queue.isLimitExceeded()) {
            logger.info("Queue limit exceeded, process blocks");
            changeState(BLOCK_RETRIEVING);
            return;
        }

        Channel master = null;
        for (Channel peer : pool) {
            // if hash retrieving is done all we need to do is just change state and quit
            if (peer.isHashRetrievingDone()) {
                changeState(BLOCK_RETRIEVING);
                return;
            }

            // master is found
            if (peer.isHashRetrieving()) {
                master = peer;
                break;
            }
        }

        if (master == null) {

            logger.trace("HASH_RETRIEVING is in progress, start master peer");

            master = pool.getMasterCandidate();

            if (master == null) return;

            master.changeSyncState(HASH_RETRIEVING);
        } else {

            // master peer rotation
            if (rotationNeeded(master)) {
                master.changeSyncState(IDLE);
                changeState(BLOCK_RETRIEVING);
            }
        }
    }

    private boolean rotationNeeded(Channel master) {

        if (master.getSyncStats().getHeaderBunchesCount() > ROTATION_LIMIT) {
            logger.debug("Peer {}: rotating due to rotation limit", master.getPeerIdShort());
            return true;
        }

        Channel candidate = pool.getMasterCandidate();

        if (candidate != null) {

            // master's TD is too low
            if (!isIn20PercentRange(candidate.getTotalDifficulty(), master.getTotalDifficulty())) {
                logger.debug("Peer {}: rotating due to low difficulty", master.getPeerIdShort());
                return true;
            }

            // master's speed is too low
            if (candidate.getPeerStats().getAvgLatency() * 2 < master.getPeerStats().getAvgLatency()) {
                logger.debug("Peer {}: rotating due to high ping", master.getPeerIdShort());
                return true;
            }
        }

        return false;
    }

    private void doBodies() {
        if (queue.isHeadersEmpty()) changeState(IDLE);
    }

    private void doIdle() {
        if (queue.isMoreBlocksNeeded()) changeState(HASH_RETRIEVING);
    }

    private void changeState(SyncState newState) {

        if (state == newState) {
            return;
        }

        logger.info("Change state from {} to {}", state, newState);

        state = newState;
    }
}
