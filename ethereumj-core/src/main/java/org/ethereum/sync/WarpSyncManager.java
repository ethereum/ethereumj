package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.datasource.DbSource;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryInsecureTrie;
import org.ethereum.db.StateSource;
import org.ethereum.facade.SyncStatus;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.client.Capability;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Functional;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.xerial.snappy.Snappy;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ethereum.crypto.HashUtil.sha3;
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
    private final static int CHUNK_DL_TIMEOUT = 180 * 1000;

    private static final Capability PAR1_CAPABILITY = new Capability(Capability.PAR, (byte) 1);

    public static final byte[] WARPSYNC_DB_KEY_SYNC_STAGE = sha3("Key in state DB indicating warpsync stage in progress".getBytes());
    public static final byte[] WARPSYNC_DB_KEY_MANIFEST = sha3("Key in state DB with encoded selected manifest".getBytes());

    @Autowired
    private SystemProperties config;

    @Autowired
    private SyncPool pool;

    @Autowired
    private BlockchainImpl blockchain;

    @Autowired
    private IndexedBlockStore blockStore;

    @Autowired
    private RepositoryInsecureTrie repository;

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


    public SyncStatus getSyncState() {
        if (!warpSyncInProgress) return new SyncStatus(SyncStatus.SyncStage.Complete, 0, 0);

        if (manifest == null) {
            return new SyncStatus(SyncStatus.SyncStage.PivotBlock,
                    (FORCE_SYNC_TIMEOUT - forceSyncRemains) / 1000, FORCE_SYNC_TIMEOUT / 1000);
        }

        EthereumListener.SyncState syncStage = getSyncStage();
        switch (syncStage) {
            case UNSECURE:
                return new SyncStatus(SyncStatus.SyncStage.StateNodes,
                        manifest.getStateHashes().size() - pendingStateChunks.size() - stateChunkQueue.size(),
                        manifest.getStateHashes().size());
        }
        return new SyncStatus(SyncStatus.SyncStage.Complete, 0, 0);
    }

    private EthereumListener.SyncState getSyncStage() {
        byte[] bytes = stateDS.get(WARPSYNC_DB_KEY_SYNC_STAGE);
        if (bytes == null) return UNSECURE;
        return EthereumListener.SyncState.values()[bytes[0]];
    }

    // TODO: not a best way considering large size and different internet connections
    synchronized void processTimeouts() {
        long cur = System.currentTimeMillis();
        List<StateChunkRequest> requests = new ArrayList<>(pendingStateChunks.values());
        for (StateChunkRequest request : requests) {
            if (request.requestSent != null && cur - request.requestSent > CHUNK_DL_TIMEOUT) {
                logger.trace("Removing state chunk {} from pending due to timeout", Hex.toHexString(request.stateChunkHash));
                pendingStateChunks.remove(request.stateChunkHash);
                stateChunkQueue.addFirst(request);
            }
        }
    }

    private void syncUnsecure() {

        logger.info("WarpSync: downloading state tries from {} state chunks", manifest.getStateHashes().size());

        for (byte[] stateChunkHash : manifest.getStateHashes()) {
            stateChunkQueue.add(new StateChunkRequest(stateChunkHash));
        }

        stateRetrieveLoop();

        dbFlushManager.commit();
        dbFlushManager.flush();

        blockchain.getRepository().syncToRoot(repository.getRoot());

        logger.info("Saving state finished, checking state root");

        if (FastByteComparisons.equal(repository.getRoot(), manifest.getStateRoot())) {
            logger.info("WarpSync: state trie download complete!");
        } else {
            logger.error("State root {} doesn't match manifest state root {}. WarpSync failed.",
                    Hex.toHexString(repository.getRoot()), Hex.toHexString(manifest.getStateRoot()));
            throw new RuntimeException("Fatal WarpSync error, incorrect state trie.");
        }


        logger.info("WarpSync: downloading 256 blocks prior to manifest block ( #{} {} )",
                manifest.getBlockNumber(), Hex.toHexString(manifest.getBlockHash()));
        downloader.startImporting(manifest.getBlockHash(), 260);
        downloader.waitForStop();

        logger.info("WarpSync: complete downloading 256 blocks prior to manifest block ( #{} {} )",
                manifest.getBlockNumber(), Hex.toHexString(manifest.getBlockHash()));

        blockchain.setBestBlock(blockStore.getBlockByHash(manifest.getBlockHash()));

        logger.info("WarpSync: proceeding to regular sync...");

        final CountDownLatch syncDoneLatch = new CountDownLatch(1);
        listener.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                syncDoneLatch.countDown();
            }
        });
        syncManager.initRegularSync(UNSECURE);
        logger.info("WarpSync: waiting for regular sync to reach the blockchain head...");

        try {
            syncDoneLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        stateDS.put(WARPSYNC_DB_KEY_MANIFEST, manifest.getEncoded());
        dbFlushManager.commit();
        dbFlushManager.flush();

        logger.info("WarpSync: regular sync reached the blockchain head.");
    }

    // TODO: Refactor to chunk request
    private class StateChunkRequest {
        byte[] stateChunkHash;
        byte[] response;
        Long requestSent;

        StateChunkRequest(byte[] stateChunkHash) {
            this.stateChunkHash = stateChunkHash;
        }

        public void reqSent() {
            synchronized (WarpSyncManager.this) {
                Long timestamp = System.currentTimeMillis();
                requestSent = timestamp;
            }
        }

        @Override
        public String toString() {
            return String.format("StateChunkRequest {chunkHash=%s}", Hex.toHexString(stateChunkHash));
        }
    }

    Deque<StateChunkRequest> stateChunkQueue = new LinkedBlockingDeque<>();
    ByteArrayMap<StateChunkRequest> pendingStateChunks = new ByteArrayMap<>();

    void stateRetrieveLoop() {
        try {
            while (!stateChunkQueue.isEmpty() || !pendingStateChunks.isEmpty()) {
                try {
                    processTimeouts();

                    while (requestNextStateChunks()) ;

                    synchronized (this) {
                        wait(10);
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable t) {
                    logger.error("Error", t);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("State chunks warp sync loop was interrupted", e);
        }
    }

    boolean requestNextStateChunks() {
        // TODO: Change to any idle after test
        final Channel idle = pool.getAnyIdle();

        if (idle != null) {
            StateChunkRequest req = null;
            synchronized (this) {
                if (!stateChunkQueue.isEmpty()) {
                    req = stateChunkQueue.poll();
                    StateChunkRequest request = pendingStateChunks.get(req.stateChunkHash);
                    if (request == null) {
                        pendingStateChunks.put(req.stateChunkHash, req);
                        req.reqSent();
                    } else {
                        req = null;
                    }
                }
            }
            if (req != null) {
                final StateChunkRequest reqSave = req;
                logger.debug("stateChunkQueue: {}, pendingQueue: {}", stateChunkQueue.size(), pendingStateChunks.size());
                logger.debug("Requesting {} state chunk from peer: {}", Hex.toHexString(req.stateChunkHash), idle);
                ListenableFuture<RLPElement> dataFuture = idle.getParHandler().requestSnapshotData(req.stateChunkHash);
                Futures.addCallback(dataFuture, new FutureCallback<RLPElement>() {
                    @Override
                    public void onSuccess(RLPElement result) {
                        try {
                            // FIXME: if we could get really empty correct chunk?
                            synchronized (WarpSyncManager.this) {
                                final StateChunkRequest request = pendingStateChunks.get(reqSave.stateChunkHash);
                                if (request == null) return;
                                request.requestSent = null;
                                if (result == null) {
                                    logger.debug("Received empty state chunk for hash {} from peer: {}",
                                            Hex.toHexString(reqSave.stateChunkHash), idle);
                                    stateChunkQueue.addFirst(request);
                                    pendingStateChunks.remove(reqSave.stateChunkHash);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            }
                            byte[] accountStatesCompressed = result.getRLPData();
                            logger.debug("Received {} bytes state chunk for hash: {}",
                                    accountStatesCompressed.length,
                                    Hex.toHexString(reqSave.stateChunkHash));

                            // Validation
                            byte[] hashActual = sha3(accountStatesCompressed);
                            logger.debug("Processing node with hash: {}", Hex.toHexString(hashActual));
                            if (!FastByteComparisons.equal(reqSave.stateChunkHash, hashActual)) {
                                logger.debug("Received bad state chunk from peer: {}, expected hash: {}, actual hash: {}",
                                        idle, Hex.toHexString(hashActual), Hex.toHexString(reqSave.stateChunkHash));
                                synchronized (WarpSyncManager.this) {
                                    pendingStateChunks.remove(reqSave.stateChunkHash);
                                    stateChunkQueue.addFirst(reqSave);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            };
                            // TODO: Put it in some queue and work with it in separate thread, heavy ops underneath

                            byte[] accountStates = Snappy.uncompress(accountStatesCompressed);
                            logger.debug("State chunk with hash %s uncompressed size: %s",
                                    Hex.toHexString(reqSave.stateChunkHash),
                                    accountStates.length);
                            RLPList accountStateList = (RLPList) RLP.decode2(accountStates).get(0);
                            synchronized (WarpSyncManager.this) {
                                // TODO: in case of any error rollback etc
                                logger.debug("Received {} states from peer: {}", accountStateList.size(), idle);
                                for (RLPElement accountStateElement : accountStateList) {
                                    RLPList accountStateItem = (RLPList) accountStateElement;

                                    byte[] addressHash = accountStateItem.get(0).getRLPData();
                                    repository.createAccount(addressHash);

                                    if (accountStateItem.get(1) == null ||
                                            accountStateItem.get(1).getRLPData().length == 0) continue;
                                    RLPList accountStateInfo = (RLPList) accountStateItem.get(1);

                                    byte[] nonceRaw = accountStateInfo.get(0).getRLPData();
                                    if (nonceRaw != null) repository.setNonce(addressHash,
                                            ByteUtil.bytesToBigInteger(nonceRaw));

                                    byte[] balanceRaw = accountStateInfo.get(1).getRLPData();
                                    if (balanceRaw != null) repository.addBalance(addressHash,
                                            ByteUtil.bytesToBigInteger(balanceRaw));

                                    // 1-byte code flag
                                    byte[] codeFlagRaw = accountStateInfo.get(2).getRLPData();
                                    byte codeFlag = codeFlagRaw == null? 0x00 : codeFlagRaw[0];
                                    byte[] code = null;
                                    byte[] codeHash = null;
                                    switch (codeFlag) {
                                        case 0x00:  // No code
                                            break;
                                        case 0x01:  // code
                                            code = accountStateInfo.get(3).getRLPData();
                                            break;
                                        case 0x02:  // code hash. some account with lower address should contain code
                                            codeHash = accountStateInfo.get(3).getRLPData();
                                        default:
                                            // TODO: do something bad
                                    }
                                    if (codeHash != null) repository.saveCodeHash(addressHash, codeHash);
                                    if (code != null) repository.saveCode(addressHash, code);

                                    RLPList storageDataList = (RLPList) accountStateInfo.get(4);
                                    for (RLPElement storageRowElement : storageDataList) {
                                        RLPList storageRowList = (RLPList) storageRowElement;
                                        byte[] keyHash = storageRowList.get(0).getRLPData();
                                        byte[] valRlp = storageRowList.get(1).getRLPData();
                                        byte[] val = RLP.decode2(valRlp).get(0).getRLPData();
                                        // TODO: better
                                        assert FastByteComparisons.equal(keyHash, sha3(valRlp));

                                        repository.addStorageRow(addressHash,
                                                new DataWord(keyHash), new DataWord(val));
                                    }
                                }

                                repository.commit();
                                dbFlushManager.commit();
                                pendingStateChunks.remove(reqSave.stateChunkHash);

                                WarpSyncManager.this.notifyAll();

                                // TODO: Add stats
//                                idle.getNodeStatistics().eth63NodesRequested.add(hashes.size());
//                                idle.getNodeStatistics().eth63NodesReceived.add(result.size());
//                                idle.getNodeStatistics().eth63NodesRetrieveTime.add(System.currentTimeMillis() - reqTime);
                            }
                        } catch (Exception e) {
                            logger.error("Unexpected error processing state chunk", e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.warn("Error with snapshot data request: " + t);
                        synchronized (WarpSyncManager.this) {
                            final StateChunkRequest request = pendingStateChunks.get(reqSave.stateChunkHash);
                            if (request == null) return;
                            stateChunkQueue.addFirst(request);
                            pendingStateChunks.remove(reqSave.stateChunkHash);
                            WarpSyncManager.this.notifyAll();
                        }
                    }
                });
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private void syncSecure() {
        logger.info("WarpSync: proceeding with secure sync.");
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

                        syncUnsecure();
                    case SECURE:

                        syncSecure();
                        listener.onSyncDone(EthereumListener.SyncState.SECURE);
                    case COMPLETE:

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

        logger.info("WarpSync: fetching manifest from all peers to find best one available");

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

    /**
     * Requires at least 2 peers with the same manifest if there are more than 1 peer
     * Chooses the best manifest available
     */
    private SnapshotManifest getBestManifest() throws Exception {
        List<Channel> allIdle = pool.getAllIdle();
        if (!allIdle.isEmpty()) {
            try {
                List<ListenableFuture<SnapshotManifest>> result = new ArrayList<>();
                // TODO: If we keep status answer data in ParHandler, we could know best peer
                for (Channel channel : allIdle) {
                    ListenableFuture<SnapshotManifest> future =
                            channel.getParHandler().requestManifest();
                    result.add(future);
                }
                ListenableFuture<List<SnapshotManifest>> successfulRequests = Futures.successfulAsList(result);
                List<SnapshotManifest> successfulResults = successfulRequests.get(3, TimeUnit.SECONDS);

                Map<SnapshotManifest, Integer> snapshotMap = new HashMap<>();
                for (SnapshotManifest manifest : successfulResults) {
                    if (manifest == null || manifest.getBlockNumber() == null || manifest.getBlockNumber()  == 0) continue;
                    if (snapshotMap.get(manifest) == null) {
                        snapshotMap.put(manifest, 1);
                    } else {
                         snapshotMap.put(manifest, snapshotMap.get(manifest) + 1);
                    }
                }

                // Require at least 2 peers with the same manifest, if we have more than 1 peer
                int peerCount = allIdle.size();
                SnapshotManifest candidate = null;
                for (Map.Entry<SnapshotManifest, Integer> snapshotEntry : snapshotMap.entrySet()) {
                    if (peerCount == 1 || snapshotEntry.getValue() > 1) {
                        SnapshotManifest current = snapshotEntry.getKey();
                        if (candidate == null || candidate.getBlockNumber() < current.getBlockNumber()) {
                            candidate = current;
                        }
                    }
                }

                if (candidate != null) {
                    logger.info("Snapshot manifest fetched: {}", candidate);
                    return candidate;
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
