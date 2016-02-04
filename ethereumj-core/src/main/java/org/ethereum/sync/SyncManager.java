package org.ethereum.sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.sync.listener.CompositeSyncListener;
import org.ethereum.sync.listener.SyncListener;
import org.ethereum.sync.listener.SyncListenerAdapter;
import org.ethereum.sync.state.StateInitiator;
import org.ethereum.sync.state.SyncState;
import org.ethereum.sync.state.SyncStateName;
import org.ethereum.sync.strategy.LongSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

import static org.ethereum.sync.state.SyncStateName.*;
import static org.ethereum.util.TimeUtils.secondsToMillis;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    private static final long FORWARD_SWITCH_LIMIT = 100;
    private static final long BACKWARD_SWITCH_LIMIT = 200;

    private static final long WORKER_TIMEOUT = secondsToMillis(1);
    private static final long PEER_STUCK_TIMEOUT = secondsToMillis(20);
    private static final long GAP_RECOVERY_TIMEOUT = secondsToMillis(2);

    @Autowired
    SystemProperties config;

    @Resource
    @Qualifier("syncStates")
    private Map<SyncStateName, SyncState> syncStates;

    @Autowired
    private StateInitiator stateInitiator;

    private SyncState state;
    private final Object stateMutex = new Object();

    /**
     * block which gap recovery is running for
     */
    private BlockWrapper gapBlock;

    /**
     * true if sync done event was triggered
     */
    private boolean syncDone = false;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    Blockchain blockchain;

    @Autowired
    SyncQueue queue;

    @Autowired
    NodeManager nodeManager;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    SyncPool pool;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    LongSync longSync;

    @Autowired
    CompositeSyncListener compositeSyncListener;

    public void init() {

        // make it asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {

                // sync queue
                queue.init();

                if (!config.isSyncEnabled()) {
                    logger.info("Sync Manager: OFF");
                    return;
                }

                logger.info("Sync Manager: ON");

                // sync pool
                pool.init();

                // fore block retrieving if new headers are added
                compositeSyncListener.add(new SyncListenerAdapter() {
                    @Override
                    public void onHeadersAdded() {
                        pool.changeStateForIdles(BLOCK_RETRIEVING);
                    }
                });

                // switch between Long and Short
                compositeSyncListener.add(new SyncListenerAdapter() {
                    @Override
                    public void onNewBlockNumber(long number) {

                        long bestNumber = blockchain.getBestBlock().getNumber();
                        long diff = number - bestNumber;

                        if (diff < 0) return;

                        if (diff <= FORWARD_SWITCH_LIMIT) {

                            longSync.stop();
                            onSyncDone(true);

                        } else if (diff > BACKWARD_SWITCH_LIMIT) {
                            longSync.start();
                            onSyncDone(false);
                        }
                    }
                });

                // recover a gap
                compositeSyncListener.add(new SyncListenerAdapter() {
                    @Override
                    public void onNoParent(BlockWrapper block) {

                        // recover gap only during Short sync
                        if (!syncDone) return;

                        Channel master = pool.getByNodeId(block.getNodeId());

                        // drop the block if there is no peer sent it
                        if (master == null) {
                            queue.pollBlock();
                            return;
                        }

                        master.recoverGap(block);
                    }
                });

                // starting from long sync
                logger.info("Start Long sync");
                longSync.start();

                if (logger.isInfoEnabled()) {
                    startLogWorker();
                }

            }
        }).start();
    }


    public void onDisconnect(Channel peer) {

        // if master peer has been disconnected
        // we need to process data it sent
        if (peer.isMaster()) {
            changeState(BLOCK_RETRIEVING);
        }
    }

    public void tryGapRecovery(BlockWrapper wrapper) {
        if (!isGapRecoveryAllowed(wrapper)) {
            return;
        }

        if (logger.isDebugEnabled()) logger.debug(
                "Recovering gap: best.number [{}] vs block.number [{}]",
                blockchain.getBestBlock().getNumber(),
                wrapper.getNumber()
        );

        gapBlock = wrapper;

        changeState(HASH_RETRIEVING);
    }

    public BlockWrapper getGapBlock() {
        return gapBlock;
    }

    public void resetGapRecovery() {
        this.gapBlock = null;
    }

    public void notifyNewBlockImported(BlockWrapper wrapper) {
        if (syncDone) {
            return;
        }

        if (!wrapper.isSolidBlock()) {
            onSyncDone(true);

            logger.debug("NEW block.number [{}] imported", wrapper.getNumber());
        } else if (logger.isInfoEnabled()) {
            logger.debug(
                    "NEW block.number [{}] block.minsSinceReceiving [{}] exceeds import time limit, continue sync",
                    wrapper.getNumber(),
                    wrapper.timeSinceReceiving() / 1000 / 60
            );
        }
    }

    public boolean isSyncDone() {
        return syncDone;
    }

    private void onSyncDone(boolean done) {

        syncDone = done;

        channelManager.onSyncDone(done);

        if (done) {
            ethereumListener.onSyncDone();
            logger.info("Long synchronization is finished");
        }
    }

    private boolean isGapRecoveryAllowed(BlockWrapper block) {
        // hashes are not downloaded yet, recovery doesn't make sense at all
        if (state.is(HASH_RETRIEVING)) {
            return false;
        }

        // gap for this block is being recovered
        if (block.equals(gapBlock) && !state.is(IDLE)) {
            logger.trace("Gap recovery is already in progress for block.number [{}]", gapBlock.getNumber());
            return false;
        }

        // ALL blocks are downloaded, we definitely have a gap
        if (queue.isHeadersEmpty()) {
            logger.trace("No headers left, recover the gap");
            return true;
        }

        // if blocks downloading is in progress
        // and import fails during some period of time
        // then we assume that faced with a gap
        // but anyway NEW blocks must wait until SyncManager becomes idle
        if (!block.isNewBlock()) {
            return block.timeSinceFail() > GAP_RECOVERY_TIMEOUT;
        } else {
            return state.is(IDLE);
        }
    }

    public void changeState(SyncStateName newStateName) {
        SyncState newState = syncStates.get(newStateName);

        if (state == newState) {
            return;
        }

        logger.info("Changing state from {} to {}", state, newState);

        synchronized (stateMutex) {
            newState.doOnTransition();
            state = newState;
        }
    }

    public boolean isPeerStuck(Channel peer) {
        SyncStatistics stats = peer.getSyncStats();

        return stats.millisSinceLastUpdate() > PEER_STUCK_TIMEOUT
                || stats.getEmptyResponsesCount() > 0;
    }

    public void startMaster(Channel master) {

        pool.changeState(IDLE);
        master.changeSyncState(HASH_RETRIEVING);
    }

    // WORKER

    private void startLogWorker() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    pool.logActivePeers();
                    pool.logBannedPeers();
                    logger.info("\n");
                    logger.info("State {}\n", state);
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Exception in log worker", t);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void maintainState() {
        synchronized (stateMutex) {
            state.doMaintain();
        }
    }
}
