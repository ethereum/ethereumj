package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.datasource.DbSource;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryInsecureTrie;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.SyncStatus;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.client.Capability;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.server.Channel;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final static int MIN_PEERS_FOR_MANIFEST_SELECTION = 5;
    private final static int FORCE_SYNC_TIMEOUT = 60 * 1000;
    private final static int CHUNK_DL_TIMEOUT = 180 * 1000;

    // TODO: move to config
    private final static int MAX_SEARCH_TIME = 10 * 60 * 1000;
    private final static int MIN_SNAPSHOT_PEERS = 2;

    private final static int MAX_SNAPSHOT_DISTANCE = 30_000 + 1_000;

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
    private TransactionStore txStore;

    @Autowired
    private RepositoryInsecureTrie repository;

    @Autowired
    private SyncManager syncManager;

    @Autowired
    @Qualifier("stateDS")
    DbSource<byte[]> stateDS;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    FastSyncDownloader downloader;

    @Autowired
    CompositeEthereumListener listener;

    @Autowired
    ApplicationContext applicationContext;

    private HeadersDownloader headersDownloader;
    private BlockBodiesDownloader blockBodiesDownloader;
    private ReceiptsDownloader receiptsDownloader;

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
                        manifest.getStateHashes().size() - pendingChunks.size() - chunkQueue.size(),
                        manifest.getStateHashes().size());
            case SECURE:
                return new SyncStatus(SyncStatus.SyncStage.Headers, headersDownloader.getHeadersLoaded(),
                        manifest.getBlockNumber());
            case COMPLETE:
                if (receiptsDownloader != null) {
                    return new SyncStatus(SyncStatus.SyncStage.Receipts,
                            receiptsDownloader.getDownloadedBlocksCount(), manifest.getBlockNumber());
                } else if (blockBodiesDownloader!= null) {
                    return new SyncStatus(SyncStatus.SyncStage.BlockBodies,
                            blockBodiesDownloader.getDownloadedCount(), manifest.getBlockNumber());
                } else {
                    return new SyncStatus(SyncStatus.SyncStage.BlockBodies, 0, manifest.getBlockNumber());
                }
        }
        return new SyncStatus(SyncStatus.SyncStage.Complete, 0, 0);
    }

    private EthereumListener.SyncState getSyncStage() {
        byte[] bytes = stateDS.get(WARPSYNC_DB_KEY_SYNC_STAGE);
        if (bytes == null) return UNSECURE;
        return EthereumListener.SyncState.values()[bytes[0]];
    }


    private void setSyncStage(EthereumListener.SyncState stage) {
        if (stage == null) {
            stateDS.delete(WARPSYNC_DB_KEY_SYNC_STAGE);
        } else {
            stateDS.put(WARPSYNC_DB_KEY_SYNC_STAGE, new byte[]{(byte) stage.ordinal()});
        }
    }

    // TODO: not a best way considering large size and different internet connections
    synchronized void processTimeouts() {
        long cur = System.currentTimeMillis();
        List<ChunkRequest> requests = new ArrayList<>(pendingChunks.values());
        for (ChunkRequest request : requests) {
            if (request.requestSent != null && cur - request.requestSent > CHUNK_DL_TIMEOUT) {
                logger.trace("Removing state chunk {} from pending due to timeout", Hex.toHexString(request.chunkHash));
                pendingChunks.remove(request.chunkHash);
                chunkQueue.addFirst(request);
            }
        }
    }

    private void syncUnsecure() {
        setSyncStage(UNSECURE);

        logger.info("WarpSync: downloading state tries from {} state chunks", manifest.getStateHashes().size());

        for (byte[] stateChunkHash : manifest.getStateHashes()) {
            chunkQueue.add(new ChunkRequest(stateChunkHash));
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

        // TODO: we could skip import of these blocks from block chunks?
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

    private class ChunkRequest {
        byte[] chunkHash;
        Long requestSent;

        ChunkRequest(byte[] chunkHash) {
            this.chunkHash = chunkHash;
        }

        public void reqSent() {
            synchronized (WarpSyncManager.this) {
                Long timestamp = System.currentTimeMillis();
                requestSent = timestamp;
            }
        }

        @Override
        public String toString() {
            return String.format("ChunkRequest {chunkHash=%s}", Hex.toHexString(chunkHash));
        }
    }

    Deque<ChunkRequest> chunkQueue = new LinkedBlockingDeque<>();
    ByteArrayMap<ChunkRequest> pendingChunks = new ByteArrayMap<>();

    private void stateRetrieveLoop() {
        try {
            while (!chunkQueue.isEmpty() || !pendingChunks.isEmpty()) {
                try {
                    processTimeouts();

                    while (requestNextStateChunk()) ;

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

    boolean requestNextStateChunk() {
        final Channel idle = pool.getAnyIdle();

        if (idle != null) {
            ChunkRequest req = null;
            synchronized (this) {
                if (!chunkQueue.isEmpty()) {
                    req = chunkQueue.poll();
                    ChunkRequest request = pendingChunks.get(req.chunkHash);
                    if (request == null) {
                        pendingChunks.put(req.chunkHash, req);
                        req.reqSent();
                    } else {
                        req = null;
                    }
                }
            }
            if (req != null) {
                final ChunkRequest reqSave = req;
                logger.debug("chunkQueue: {}, pendingQueue: {}", chunkQueue.size(), pendingChunks.size());
                logger.debug("Requesting {} state chunk from peer: {}", Hex.toHexString(req.chunkHash), idle);
                ListenableFuture<RLPElement> dataFuture = idle.getParHandler().requestSnapshotData(req.chunkHash);
                Futures.addCallback(dataFuture, new FutureCallback<RLPElement>() {
                    @Override
                    public void onSuccess(RLPElement result) {
                        try {
                            // FIXME: if we could get really empty correct chunk?
                            synchronized (WarpSyncManager.this) {
                                final ChunkRequest request = pendingChunks.get(reqSave.chunkHash);
                                if (request == null) return;
                                request.requestSent = null;
                                if (result == null) {
                                    logger.debug("Received empty state chunk for hash {} from peer: {}",
                                            Hex.toHexString(reqSave.chunkHash), idle);
                                    chunkQueue.addFirst(request);
                                    pendingChunks.remove(reqSave.chunkHash);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            }
                            byte[] accountStatesCompressed = result.getRLPData();
                            logger.debug("Received {} bytes state chunk for hash: {}",
                                    accountStatesCompressed.length,
                                    Hex.toHexString(reqSave.chunkHash));

                            // Validation
                            byte[] hashActual = sha3(accountStatesCompressed);
                            logger.debug("Processing state chunk with hash: {}", Hex.toHexString(hashActual));
                            if (!FastByteComparisons.equal(reqSave.chunkHash, hashActual)) {
                                logger.debug("Received bad state chunk from peer: {}, expected hash: {}, actual hash: {}",
                                        idle, Hex.toHexString(hashActual), Hex.toHexString(reqSave.chunkHash));
                                synchronized (WarpSyncManager.this) {
                                    pendingChunks.remove(reqSave.chunkHash);
                                    chunkQueue.addFirst(reqSave);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            };
                            // TODO: Put it in some queue and work with it in separate thread, heavy ops underneath

                            byte[] accountStates = Snappy.uncompress(accountStatesCompressed);
                            logger.debug("State chunk with hash {} uncompressed size: {}",
                                    Hex.toHexString(reqSave.chunkHash),
                                    accountStates.length);
                            RLPList accountStateList = (RLPList) RLP.decode2(accountStates).get(0);
                            synchronized (WarpSyncManager.this) {
                                // TODO: in case of any error rollback etc
                                logger.debug("Received {} states from peer: {}", accountStateList.size(), idle);
                                for (RLPElement accountStateElement : accountStateList) {
                                    RLPList accountStateItem = (RLPList) accountStateElement;

                                    byte[] addressHash = accountStateItem.get(0).getRLPData();
                                    repository.createAccount(addressHash);

                                    if (accountStateItem.get(1).getRLPData() == null ||
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
                                        case 0x01:  // Code
                                            code = accountStateInfo.get(3).getRLPData();
                                            break;
                                        case 0x02:  // Code hash. some account with lower address should contain code
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
                                        if (!FastByteComparisons.equal(keyHash, sha3(valRlp))) {
                                            pendingChunks.remove(reqSave.chunkHash);
                                            chunkQueue.addFirst(reqSave);
                                            repository.rollback();
                                            WarpSyncManager.this.notifyAll();
                                            idle.dropConnection();
                                            return;
                                        };

                                        repository.addStorageRow(addressHash,
                                                new DataWord(keyHash), new DataWord(val));
                                    }
                                }

                                repository.commit();
                                dbFlushManager.commit();
                                pendingChunks.remove(reqSave.chunkHash);

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
                            final ChunkRequest request = pendingChunks.get(reqSave.chunkHash);
                            if (request == null) return;
                            chunkQueue.addFirst(request);
                            pendingChunks.remove(reqSave.chunkHash);
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
        logger.info("WarpSync: proceeding with complete sync.");
        setSyncStage(EthereumListener.SyncState.SECURE);

        manifest = new SnapshotManifest(stateDS.get(WARPSYNC_DB_KEY_MANIFEST));

        for (byte[] blockChunkHash : manifest.getBlockHashes()) {
            chunkQueue.addLast(new ChunkRequest(blockChunkHash));
        }

        blockChunkRetrieveLoop();

        dbFlushManager.commit();
        dbFlushManager.flush();

        pool.setNodesSelector(null);
        Block gapBlock = getGapBlock(manifest);
        logger.info("WarpSync: Block chunks downloaded finished. Blockchain synced to #{}", gapBlock.getNumber());

        logger.info("WarpSync: downloading headers from gap block down to genesis block for ensure manifest block ({}) is secure...",
                manifest.getShortDescr());
        headersDownloader = applicationContext.getBean(HeadersDownloader.class);
        headersDownloader.init(gapBlock.getHash());
        headersDownloader.waitForStop();
        if (!FastByteComparisons.equal(headersDownloader.getGenesisHash(), config.getGenesis().getHash())) {
            logger.error("WARPSYNC FATAL ERROR: after downloading header chain starting from the manifest block (" +
                    manifest.getShortDescr() + ") obtained genesis block doesn't match ours: " + Hex.toHexString(headersDownloader.getGenesisHash()));
            logger.error("Can't recover and exiting now. You need to restart from scratch (all DBs will be reset)");
            System.exit(-666);
        }
        dbFlushManager.commit();
        dbFlushManager.flush();
        logger.info("WarpSync: all headers downloaded. The state is SECURE now.");
    }

    private void blockChunkRetrieveLoop() {
        try {
            while (!chunkQueue.isEmpty() || !pendingChunks.isEmpty()) {
                try {
                    processTimeouts();

                    while (requestNextBlockChunk()) ;

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
            logger.warn("Block chunks warp sync loop was interrupted", e);
        }
    }

    private byte[] getTrieHash(RLPList rlpElements) {
        Trie<byte[]> txsState = new TrieImpl();
        for (int i = 0; i < rlpElements.size(); i++) {
            txsState.put(RLP.encodeInt(i), rlpElements.get(i).getRLPData());
        }

        return txsState.getRootHash();
    }

    // TODO: we are getting only 30k blocks preceding manifest by this.
    // In future we could mix several snapshots and usual download of absent data
    boolean requestNextBlockChunk() {
        final Channel idle = pool.getAnyIdle();

        if (idle != null) {
            ChunkRequest req = null;
            synchronized (this) {
                if (!chunkQueue.isEmpty()) {
                    req = chunkQueue.pollFirst();
                    ChunkRequest request = pendingChunks.get(req.chunkHash);
                    if (request == null) {
                        pendingChunks.put(req.chunkHash, req);
                        req.reqSent();
                    } else {
                        req = null;
                    }
                }
            }
            if (req != null) {
                final ChunkRequest reqSave = req;
                logger.debug("chunkQueue: {}, pendingQueue: {}", chunkQueue.size(), pendingChunks.size());
                logger.debug("Requesting {} block chunk from peer: {}", Hex.toHexString(req.chunkHash), idle);
                ListenableFuture<RLPElement> dataFuture = idle.getParHandler().requestSnapshotData(req.chunkHash);
                Futures.addCallback(dataFuture, new FutureCallback<RLPElement>() {
                    @Override
                    public void onSuccess(RLPElement result) {
                        try {
                            synchronized (WarpSyncManager.this) {
                                final ChunkRequest request = pendingChunks.get(reqSave.chunkHash);
                                if (request == null) return;
                                request.requestSent = null;
                                if (result == null) {
                                    logger.debug("Received empty block chunk for hash {} from peer: {}",
                                            Hex.toHexString(reqSave.chunkHash), idle);
                                    chunkQueue.addFirst(request);
                                    pendingChunks.remove(reqSave.chunkHash);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            }
                            byte[] blockStatesCompressed = result.getRLPData();
                            logger.debug("Received {} bytes block chunk for hash: {}",
                                    blockStatesCompressed.length,
                                    Hex.toHexString(reqSave.chunkHash));

                            // Validation
                            byte[] hashActual = sha3(blockStatesCompressed);
                            logger.debug("Processing block chunk with hash: {}", Hex.toHexString(hashActual));
                            if (!FastByteComparisons.equal(reqSave.chunkHash, hashActual)) {
                                logger.debug("Received bad block chunk from peer: {}, expected hash: {}, actual hash: {}",
                                        idle, Hex.toHexString(hashActual), Hex.toHexString(reqSave.chunkHash));
                                synchronized (WarpSyncManager.this) {
                                    pendingChunks.remove(reqSave.chunkHash);
                                    chunkQueue.addFirst(reqSave);
                                    WarpSyncManager.this.notifyAll();
                                    idle.dropConnection();
                                    return;
                                }
                            };
                            // TODO: Put it in some queue and work with it in separate thread, heavy ops underneath

                            byte[] blockHashes = Snappy.uncompress(blockStatesCompressed);
                            logger.debug("Block chunk with hash %s uncompressed size: %s",
                                    Hex.toHexString(reqSave.chunkHash),
                                    blockHashes.length);
                            RLPList blockList = (RLPList) RLP.decode2(blockHashes).get(0);

                            synchronized (WarpSyncManager.this) {
                                // TODO: in case of any error rollback etc

                                long firstBlockNumber = ByteUtil.byteArrayToLong(blockList.get(0).getRLPData());
                                if (logger.isDebugEnabled()) {
                                    long blockCount = blockList.size() - 3;
                                    logger.debug("Received {} blocks [{} - {}] from peer: {}",
                                                    blockCount, firstBlockNumber, firstBlockNumber + blockCount, idle);
                                }

                                byte[] parentHash = blockList.get(1).getRLPData();
                                BigInteger curTotalDiff = ByteUtil.bytesToBigInteger(blockList.get(2).getRLPData());
                                long currentBlockNumber = firstBlockNumber + 1;

                                for (int i = 3; i < blockList.size(); i++) {
                                    RLPList currentBlockData = (RLPList) blockList.get(i);
                                    RLPList abridgedBlock = (RLPList) currentBlockData.get(0);
                                    RLPList receiptsRlp = (RLPList) currentBlockData.get(1);

                                    byte[] author = abridgedBlock.get(0).getRLPData();
                                    byte[] stateRoot = abridgedBlock.get(1).getRLPData();
                                    byte[] logsBloom = abridgedBlock.get(2).getRLPData();
                                    byte[] difficulty = abridgedBlock.get(3).getRLPData();
                                    curTotalDiff = curTotalDiff.add(ByteUtil.bytesToBigInteger(difficulty));
                                    byte[] gasLimit = abridgedBlock.get(4).getRLPData();
                                    long gasUsed = ByteUtil.byteArrayToLong(abridgedBlock.get(5).getRLPData());
                                    long timestamp = ByteUtil.byteArrayToLong(abridgedBlock.get(6).getRLPData());
                                    byte[] extraData = abridgedBlock.get(7).getRLPData();

                                    RLPList transactionsRlp = (RLPList) abridgedBlock.get(8);
                                    List<Transaction> transactionsList = new ArrayList<>();
                                    for (RLPElement txRlp : transactionsRlp) {
                                        transactionsList.add(new Transaction(txRlp.getRLPData()));
                                    }

                                    RLPList unclesRlp = (RLPList) abridgedBlock.get(9);
                                    List<BlockHeader> uncleList = new ArrayList<>();
                                    for (RLPElement uncleRlp : unclesRlp) {
                                        uncleList.add(new BlockHeader(uncleRlp.getRLPData()));
                                    }

                                    byte[] mixHash = abridgedBlock.get(10).getRLPData();
                                    byte[] nonce = abridgedBlock.get(11).getRLPData();

                                    // TODO: validation
                                    BlockHeader header = new BlockHeader(
                                            parentHash, sha3(unclesRlp.getRLPData()), author, logsBloom, difficulty,
                                            currentBlockNumber, gasLimit, gasUsed, timestamp, extraData, mixHash, nonce
                                    );
                                    header.setStateRoot(stateRoot);
                                    byte[] transactionsRoot = getTrieHash(transactionsRlp);
                                    header.setTransactionsRoot(transactionsRoot);
                                    byte[] receiptsRoot = getTrieHash(receiptsRlp);
                                    header.setReceiptsRoot(receiptsRoot);

                                    // Creating and saving block
                                    Block block = new Block(header, transactionsList, uncleList);
                                    blockStore.saveBlock(block, curTotalDiff, true);


                                    // Creating and saving receipts
                                    // TODO: add validation (block, receipts)
                                    List<TransactionReceipt> receipts = new ArrayList<>();
                                    for (RLPElement txReceiptRlp : receiptsRlp) {
                                        receipts.add(new TransactionReceipt((RLPList) txReceiptRlp));
                                    }
                                    for (int j = 0; j < receipts.size(); j++) {
                                        TransactionReceipt receipt = receipts.get(j);
                                        TransactionInfo txInfo = new TransactionInfo(receipt, block.getHash(), j);
                                        txInfo.setTransaction(block.getTransactionsList().get(j));
                                        txStore.put(txInfo);
                                    }

                                    currentBlockNumber++;
                                    parentHash = block.getHash();
                                }
                                dbFlushManager.commit();
                                pendingChunks.remove(reqSave.chunkHash);
                                WarpSyncManager.this.notifyAll();

                                // TODO: Add stats
//                                idle.getNodeStatistics().eth63NodesRequested.add(hashes.size());
//                                idle.getNodeStatistics().eth63NodesReceived.add(result.size());
//                                idle.getNodeStatistics().eth63NodesRetrieveTime.add(System.currentTimeMillis() - reqTime);
                            }
                        } catch (Exception e) {
                            logger.error("Unexpected error processing block chunk", e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.warn("Error with snapshot data request: " + t);
                        synchronized (WarpSyncManager.this) {
                            final ChunkRequest request = pendingChunks.get(reqSave.chunkHash);
                            if (request == null) return;
                            chunkQueue.addFirst(request);
                            pendingChunks.remove(reqSave.chunkHash);
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

    public void main() {

        if (blockchain.getBestBlock().getNumber() == 0 || getSyncStage() == SECURE || getSyncStage() == COMPLETE) {
            // either no DB at all (clear sync or DB was deleted due to UNSECURE stage while initializing
            // or we have incomplete headers/blocks/receipts download

            warpSyncInProgress = true;
            pool.setForceSync(true);
            pool.setNodesSelector(new Functional.Predicate<NodeHandler>() {
                @Override
                public boolean test(NodeHandler handler) {
                    return handler.getNodeStatistics().capabilities.isEmpty() ||
                            handler.getNodeStatistics().capabilities.contains(PAR1_CAPABILITY);
                }
            });

            try {
                EthereumListener.SyncState origSyncStage = getSyncStage();

                switch (origSyncStage) {
                    case UNSECURE:
                        manifest = getManifest();
                        if (manifest == null || manifest.getBlockNumber() == 0) {
                            logger.info("WarpSync: too short blockchain, proceeding with regular sync...");
                            syncManager.initRegularSync(EthereumListener.SyncState.COMPLETE);
                            return;
                        }

                        syncUnsecure();
                    case SECURE:
                        if (origSyncStage == SECURE) {
                            logger.info("WarpSync: UNSECURE sync was completed prior to this run, proceeding with next stage...");
                            logger.info("Initializing regular sync");
                            syncManager.initRegularSync(EthereumListener.SyncState.UNSECURE);
                        }

                        syncSecure();

                        listener.onSyncDone(EthereumListener.SyncState.SECURE);
                    case COMPLETE:
                        if (origSyncStage == COMPLETE) {
                            logger.info("WarpSync: SECURE sync was completed prior to this run, proceeding with next stage...");
                            logger.info("Initializing regular sync");
                            syncManager.initRegularSync(EthereumListener.SyncState.SECURE);
                        }

                        syncBlocksReceipts();

                        listener.onSyncDone(EthereumListener.SyncState.COMPLETE);
                }
                logger.info("WarpSync: Full sync done.");
            } catch (InterruptedException ex) {
                logger.info("Shutting down due to interruption");
            } finally {
                warpSyncInProgress = false;
                pool.setNodesSelector(null);
                pool.setForceSync(false);
            }
        } else {
            logger.info("WarpSync: fast sync was completed, best block: (" + blockchain.getBestBlock().getShortDescr() + "). " +
                    "Continue with regular sync...");
            syncManager.initRegularSync(EthereumListener.SyncState.COMPLETE);
        }
    }

    private Block getGapBlock(SnapshotManifest manifest) {
        // [0..null blocks..gapBlock..full blocks..HEAD], gapBlock is full
        long gapBlockNumber = manifest.getBlockNumber() - 30000 + 1;

        return blockStore.getChainBlockByNumber(gapBlockNumber);
    }

    private void syncBlocksReceipts() {
        setSyncStage(EthereumListener.SyncState.COMPLETE);
        manifest = new SnapshotManifest(stateDS.get(WARPSYNC_DB_KEY_MANIFEST));

        Block gapBlock = getGapBlock(manifest);
        logger.info("WarpSync: Downloading Block bodies up to gap block (" + gapBlock.getShortDescr() + ")...");

        blockBodiesDownloader = applicationContext.getBean(BlockBodiesDownloader.class);
        blockBodiesDownloader.startImporting();
        blockBodiesDownloader.waitForStop();

        logger.info("WarpSync: Block bodies downloaded");

        logger.info("WarpSync: Downloading receipts...");

        receiptsDownloader = applicationContext.getBean
                (ReceiptsDownloader.class, 1, gapBlock.getNumber() + 1);
        receiptsDownloader.startImporting();
        receiptsDownloader.waitForStop();

        logger.info("WarpSync: receipts downloaded");

        logger.info("WarpSync: updating totDifficulties starting from the manifest block...");
        blockchain.updateBlockTotDifficulties(manifest.getBlockNumber().intValue());
        synchronized (blockchain) {
            Block bestBlock = blockchain.getBestBlock();
            BigInteger totalDifficulty = blockchain.getTotalDifficulty();
            logger.info("WarpSync: totDifficulties updated: bestBlock: " + bestBlock.getShortDescr() + ", totDiff: " + totalDifficulty);
        }
        setSyncStage(null);
        stateDS.delete(WARPSYNC_DB_KEY_MANIFEST);
        dbFlushManager.commit();
        dbFlushManager.flush();
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

            if (allIdle.size() >= MIN_PEERS_FOR_MANIFEST_SELECTION || forceSyncRemains < 0 && !allIdle.isEmpty()) {
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
                logger.info("WarpSync: waiting for at least " + MIN_PEERS_FOR_MANIFEST_SELECTION + " peers or " + forceSyncRemains / 1000 + " sec to select manifest block... ("
                        + allIdle.size() + " peers so far)");
                s = t;
            }

            Thread.sleep(500);
        }

        logger.info("WarpSync: fetching manifest from all peers to find best one available");

        try {
            Long manifestSearchStart = System.currentTimeMillis();
            while (true) {
                SnapshotManifest result = getBestManifest();

                if (result != null) return result;
                if (System.currentTimeMillis() - manifestSearchStart > MAX_SEARCH_TIME) {
                    logger.info("Maximum search time for good snapshot manifest peers exceeded. Aborting.");
                    logger.info("Required at least {} peer(s) with manifest for block in {} or less blocks to the HEAD.",
                            MIN_SNAPSHOT_PEERS, MAX_SNAPSHOT_DISTANCE);
                    return null;
                }

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
     * Requires at least 2 peers with the same manifest
     * in less than 31_000 of blocks to the best known
     */
    private SnapshotManifest getBestManifest() throws Exception {
        List<Channel> allIdle = pool.getAllIdle();
        if (!allIdle.isEmpty()) {
            try {
                long bestBlockNumber = 0L;
                SnapshotManifest bestShort = null;
                Set<Channel> bestChannels = new HashSet<>();

                for (Channel channel : allIdle) {
                    if (channel.getParHandler() == null) channel.getEthHandler().dropConnection();

                    long channelBestBlockNumber = channel.getEthHandler().getBestKnownBlock().getNumber();
                    if (channelBestBlockNumber > bestBlockNumber) {
                        bestBlockNumber = channelBestBlockNumber;
                    }

                    SnapshotManifest channelManifest = channel.getParHandler() == null ?
                            null : channel.getParHandler().getShortManifest();
                    if (channelManifest != null) {
                        if (bestShort == null || bestShort.getBlockNumber() < channelManifest.getBlockNumber()) {
                            bestShort = channelManifest;
                            bestChannels.clear();
                            bestChannels.add(channel);
                        } else if (bestShort.getBlockNumber().equals(channelManifest.getBlockNumber())) {
                            bestChannels.add(channel);
                        }
                    }
                }

                long minManifestBlockNumber = bestBlockNumber - MAX_SNAPSHOT_DISTANCE;
                if (minManifestBlockNumber < 0 || bestShort == null) return null;
                if (bestShort.getBlockNumber() < minManifestBlockNumber) return null;
                if (bestChannels.isEmpty() || bestChannels.size() < MIN_SNAPSHOT_PEERS) return null;

                List<ListenableFuture<SnapshotManifest>> result = new ArrayList<>();
                for (Channel channel : bestChannels) {
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

                for (Map.Entry<SnapshotManifest, Integer> snapshotEntry : snapshotMap.entrySet()) {
                    if (snapshotEntry.getValue() >= MIN_SNAPSHOT_PEERS) {
                        SnapshotManifest current = snapshotEntry.getKey();
                        logger.info("Snapshot manifest fetched: {}", current);

                        return  current;
                    }
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
