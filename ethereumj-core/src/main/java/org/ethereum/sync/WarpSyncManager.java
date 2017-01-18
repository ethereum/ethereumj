package org.ethereum.sync;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DbSource;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.StateSource;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.Capability;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.server.Channel;
import org.ethereum.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ethereum.listener.EthereumListener.SyncState.COMPLETE;
import static org.ethereum.listener.EthereumListener.SyncState.SECURE;
import static org.ethereum.listener.EthereumListener.SyncState.UNSECURE;

/**
 * Sync using Parity v1 protocol (PAR1)
 */
@Component
public class WarpSyncManager {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static int MIN_PEERS_FOR_PIVOT_SELECTION = 5;
    private final static int FORCE_SYNC_TIMEOUT = 60 * 1000;

    private static final Capability PAR1_CAPABILITY = new Capability(Capability.PAR, (byte) 1);

    public static final byte[] WARPSYNC_DB_KEY_SYNC_STAGE = HashUtil.sha3("Key in state DB indicating warpsync stage in progress".getBytes());

    @Autowired
    private SystemProperties config;

    @Autowired
    private SyncPool pool;

    @Autowired
    private BlockchainImpl blockchain;

    @Autowired
    private IndexedBlockStore blockStore;

    @Autowired
    private SyncManager syncManager;

    @Autowired
    @Qualifier("stateDS")
    DbSource<byte[]> stateDS;

    @Autowired
    private StateSource stateSource;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    FastSyncDownloader downloader;

    @Autowired
    CompositeEthereumListener listener;

    @Autowired
    ApplicationContext applicationContext;

    private boolean warpSyncInProgress = false;
    private Thread warpSyncThread;

    private SnapshotManifest manifest;
    private long forceSyncRemains;

    void init() {
        warpSyncThread = new Thread("WarpSyncLoop") {
            @Override
            public void run() {
                try {
                    main();
                } catch (Exception e) {
                    logger.error("Fatal WarpSync loop error", e);
                }
            }
        };
        warpSyncThread.start();
    }

    private EthereumListener.SyncState getSyncStage() {
        byte[] bytes = stateDS.get(WARPSYNC_DB_KEY_SYNC_STAGE);
        if (bytes == null) return UNSECURE;
        return EthereumListener.SyncState.values()[bytes[0]];
    }


    private void syncUnsecure(SnapshotManifest manifest) {
        // TODO: Implement
    }

    public void main() {

        if (blockchain.getBestBlock().getNumber() == 0 || getSyncStage() == SECURE || getSyncStage() == COMPLETE) {
            // either no DB at all (clear sync or DB was deleted due to UNSECURE stage while initializing
            // or we have incomplete headers/blocks/receipts download

            warpSyncInProgress = true;
            pool.setNodesSelector(new Functional.Predicate<NodeHandler>() {
                @Override
                public boolean test(NodeHandler handler) {
                    return handler.getNodeStatistics().capabilities.contains(PAR1_CAPABILITY);
                }
            });

            try {
                EthereumListener.SyncState origSyncStage = getSyncStage();

                switch (origSyncStage) {
                    case UNSECURE:
                        manifest = getManifest();
                        if (manifest.getBlockNumber() == 0) {
                            logger.info("WarpSync: too short blockchain, proceeding with regular sync...");
                            syncManager.initRegularSync(EthereumListener.SyncState.COMPLETE);
                            return;
                        }

                        syncUnsecure(manifest);  // regularSync should be inited here
                    case COMPLETE:
//                        if (origSyncStage == COMPLETE) {
//                            logger.info("FastSync: SECURE sync was completed prior to this run, proceeding with next stage...");
//                            logger.info("Initializing regular sync");
//                            syncManager.initRegularSync(EthereumListener.SyncState.SECURE);
//                        }
//
//                        syncBlocksReceipts();

                        listener.onSyncDone(EthereumListener.SyncState.COMPLETE);
                }
                logger.info("WarpSync: Full sync done.");
            } catch (InterruptedException ex) {
                logger.info("Shutting down due to interruption");
            } finally {
                warpSyncInProgress = false;
                pool.setNodesSelector(null);
            }
        } else {
            logger.info("WarpSync: fast sync was completed, best block: (" + blockchain.getBestBlock().getShortDescr() + "). " +
                    "Continue with regular sync...");
            syncManager.initRegularSync(EthereumListener.SyncState.COMPLETE);
        }
    }

    public boolean isWarpSyncInProgress() {
        return warpSyncInProgress;
    }

    private SnapshotManifest getManifest() throws InterruptedException {

        long start = System.currentTimeMillis();
        long s = start;

        logger.info("WarpSync: looking for best manifest...");
        BlockIdentifier bestKnownBlock;

        while (true) {
            List<Channel> allIdle = pool.getAllIdle();

            forceSyncRemains = FORCE_SYNC_TIMEOUT - (System.currentTimeMillis() - start);

            if (allIdle.size() >= MIN_PEERS_FOR_PIVOT_SELECTION || forceSyncRemains < 0 && !allIdle.isEmpty()) {
                Channel bestPeer = allIdle.get(0);
                for (Channel channel : allIdle) {
                    if (bestPeer.getEthHandler().getBestKnownBlock().getNumber() < channel.getEthHandler().getBestKnownBlock().getNumber()) {
                        bestPeer = channel;
                    }
                }
                bestKnownBlock = bestPeer.getEthHandler().getBestKnownBlock();
                if (bestKnownBlock.getNumber() > 1000) {
                    logger.info("WarpSync: best block " + bestKnownBlock + " found with peer " + bestPeer);
                    break;
                }
            }

            long t = System.currentTimeMillis();
            if (t - s > 5000) {
                logger.info("WarpSync: waiting for at least " + MIN_PEERS_FOR_PIVOT_SELECTION + " peers or " + forceSyncRemains / 1000 + " sec to select manifest block... ("
                        + allIdle.size() + " peers so far)");
                s = t;
            }

            Thread.sleep(500);
        }

        logger.info("WarpSync: fetching manifest from best peer with block #" + bestKnownBlock);

        try {
            while (true) {
                SnapshotManifest result = getBestManifest();

                if (result != null) return result;

                long t = System.currentTimeMillis();
                if (t - s > 5000) {
                    logger.info("WarpSync: waiting for a peer to fetch manifest block...");
                    s = t;
                }

                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected", e);
            throw new RuntimeException(e);
        }
    }

    private SnapshotManifest getBestManifest() throws Exception {
        List<Channel> allIdle = pool.getAllIdle();
        if (!allIdle.isEmpty()) {
            try {
                List<ListenableFuture<SnapshotManifest>> result = new ArrayList<>();

                for (Channel channel : allIdle) {
                    ListenableFuture<SnapshotManifest> future =
                            channel.getParHandler().requestManifest();
                    result.add(future);
                }
                ListenableFuture<List<SnapshotManifest>> successfulRequests = Futures.successfulAsList(result);
                List<SnapshotManifest> successfulResults = successfulRequests.get(3, TimeUnit.SECONDS);

                SnapshotManifest best = null;
                for (SnapshotManifest manifest : successfulResults) {
                    if (best == null && manifest.getBlockNumber() > 0) {
                        best = manifest;
                    } else if (best != null && manifest.getBlockNumber() > best.getBlockNumber()) {
                         best = manifest;
                    }
                }

                if (best != null) {
                    logger.info("Snapshot manifest fetched: {}", best);
                    return best;
                }
            } catch (TimeoutException e) {
                logger.debug("Timeout waiting for answer", e);
            }
        }

        return null;
    }

    public void close() {
        logger.info("Closing WarpSyncManager");
        try {
            warpSyncThread.interrupt();
            warpSyncInProgress = false;
            dbFlushManager.commit();
            dbFlushManager.flush();
            warpSyncThread.join(10 * 1000);
        } catch (Exception e) {
            logger.warn("Problems closing WarpSyncManager", e);
        }
    }
}
