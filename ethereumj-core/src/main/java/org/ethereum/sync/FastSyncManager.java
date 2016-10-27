package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Flushable;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.test.Source;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.net.eth.handler.Eth63;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

import static org.ethereum.util.CompactEncoder.hasTerminator;

/**
 * Created by Anton Nashatyrev on 24.10.2016.
 */
@Component
@Lazy
public class FastSyncManager {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static long REQUEST_TIMEOUT = 3 * 1000;
    private final static int REQUEST_MAX_NODES = 100;

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
    private ChannelManager channelManager;

    //    Source<byte[], byte[]> stateDS;
    @Autowired @Qualifier("stateDS")
    KeyValueDataSource stateDS = new HashMapDB();

    @Autowired
    FastSyncDownloader downloader;

    int nodesInserted = 0;

    @PostConstruct
    void init() {
        pool.init(channelManager);
    }

    enum TrieNodeType {
        STATE,
        STORAGE,
        CODE
    }

    class TrieNodeRequest {
        TrieNodeType type;
        byte[] nodeHash;
        byte[] response;
        long timestamp;
        int reqCount;

        TrieNodeRequest(TrieNodeType type, byte[] nodeHash) {
            this.type = type;
            this.nodeHash = nodeHash;
        }

        List<TrieNodeRequest> createChildRequests() {
            if (type == TrieNodeType.CODE) {
                return Collections.emptyList();
            }

            List<Object> node = Value.fromRlpEncoded(response).asList();
            List<TrieNodeRequest> ret = new ArrayList<>();
            if (type == TrieNodeType.STATE) {
                if (node.size() == 2 && hasTerminator((byte[]) node.get(0))) {
                    byte[] nodeValue = (byte[]) node.get(1);
                    AccountState state = new AccountState(nodeValue);

                    if (!FastByteComparisons.equal(HashUtil.EMPTY_DATA_HASH, state.getCodeHash())) {
                        ret.add(new TrieNodeRequest(TrieNodeType.CODE, state.getCodeHash()));
                    }
                    if (!FastByteComparisons.equal(HashUtil.EMPTY_TRIE_HASH, state.getStateRoot())) {
                        ret.add(new TrieNodeRequest(TrieNodeType.STORAGE, state.getStateRoot()));
                    }
                    return ret;
                }
            }

            List<byte[]> childHashes = getChildHashes(node);
            for (byte[] childHash : childHashes) {
                ret.add(new TrieNodeRequest(type, childHash));
            }
            return ret;
        }

        public void reqSent() {
            timestamp = System.currentTimeMillis();
            reqCount++;
        }

        @Override
        public String toString() {
            return "TrieNodeRequest{" +
                    "type=" + type +
                    ", nodeHash=" + Hex.toHexString(nodeHash) +
                    '}';
        }
    }

    private static List<byte[]> getChildHashes(List<Object> siblings) {
        List<byte[]> ret = new ArrayList<>();
        if (siblings.size() == 2) {
            Value val = new Value(siblings.get(1));
            if (val.isHashCode() && !hasTerminator((byte[]) siblings.get(0)))
                ret.add(val.asBytes());
        } else {
            for (int j = 0; j < 16; ++j) {
                Value val = new Value(siblings.get(j));
                if (val.isHashCode())
                    ret.add(val.asBytes());
            }
        }
        return ret;
    }

    Deque<TrieNodeRequest> nodesQueue = new LinkedBlockingDeque<>();
    ByteArrayMap<TrieNodeRequest> pendingNodes = new ByteArrayMap<>();
    ByteArraySet knownHashes = new ByteArraySet();

    synchronized void processTimeouts() {
        long cur = System.currentTimeMillis();
        for (TrieNodeRequest request : new ArrayList<>(pendingNodes.values())) {
            if (cur - request.timestamp > REQUEST_TIMEOUT) {
                pendingNodes.remove(request.nodeHash);
                nodesQueue.addFirst(request);
            }
        }
    }

    synchronized void processResponse(TrieNodeRequest req) {
        stateDS.put(req.nodeHash, req.response);
        knownHashes.remove(req.nodeHash);
        nodesInserted++;
        for (TrieNodeRequest childRequest : req.createChildRequests()) {
            if (!knownHashes.contains(childRequest.nodeHash) && stateDS.get(childRequest.nodeHash) == null) {
                nodesQueue.addFirst(childRequest);
                knownHashes.add(childRequest.nodeHash);
            }
        }
    }

    void requestNextNodes(int cnt) {
        final Channel idle = pool.getAnyIdle();

        if (idle != null) {
            final List<byte[]> hashes = new ArrayList<>();
            synchronized(this) {
                for (int i = 0; i < cnt && !nodesQueue.isEmpty(); i++) {
                    TrieNodeRequest req = nodesQueue.poll();
                    req.reqSent();
                    hashes.add(req.nodeHash);
                    pendingNodes.put(req.nodeHash, req);
                }
            }
            if (hashes.size() > 0) {
                logger.trace("Requesting " + hashes.size() + " nodes from peer: " + idle);
                ListenableFuture<List<Pair<byte[], byte[]>>> nodes = ((Eth63) idle.getEthHandler()).requestTrieNodes(hashes);
                Futures.addCallback(nodes, new FutureCallback<List<Pair<byte[], byte[]>>>() {
                    @Override
                    public void onSuccess(List<Pair<byte[], byte[]>> result) {
                        try {
                            synchronized (FastSyncManager.this) {
                                logger.trace("Received " + result.size() + " nodes (of " + hashes.size() + ") from peer: " + idle);
                                for (Pair<byte[], byte[]> pair : result) {
                                    TrieNodeRequest request = pendingNodes.remove(pair.getKey());
                                    if (request == null) {
                                        logger.error("Received node which was not requested: " + Hex.toHexString(pair.getKey()));
                                        return;
                                    }
                                    request.response = pair.getValue();
                                    processResponse(request);
                                }

                                FastSyncManager.this.notifyAll();
                            }
                        } catch (Exception e) {
                            logger.error("Unexpected error processing nodes", e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.warn("Error with Trie Node request: " + t);
                        synchronized (FastSyncManager.this) {
                            for (byte[] hash : hashes) {
                                nodesQueue.addFirst(pendingNodes.get(hash));
                            }
                            FastSyncManager.this.notifyAll();
                        }
                    }
                });
            }
        }
    }

    void retrieveLoop() {
        try {
            while(!nodesQueue.isEmpty() || !pendingNodes.isEmpty()) {
                try {
                    processTimeouts();
                    requestNextNodes(REQUEST_MAX_NODES);
                    synchronized (this) {
                        wait(200);
                    }
                    logStat();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable t) {
                    logger.error("Error, t");
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Main fast sync loop was interrupted", e);
        }
    }

    long last = 0;
    private void logStat() {
        long cur = System.currentTimeMillis();
        if (cur - last > 5000) {
            logger.info("FastSync: received: " + nodesInserted + ", known: " + nodesQueue.size() + ", pending: " + pendingNodes.size());
            last = cur;
        }
    }

    public void main() {

        BlockHeader pivot = getPivotBlock();

        byte[] pivotStateRoot = pivot.getStateRoot();
        TrieNodeRequest request = new TrieNodeRequest(TrieNodeType.STATE, pivotStateRoot);
        nodesQueue.add(request);
        logger.info("FastSync: downloading state trie at pivot block: " + pivot.getShortDescr());
        retrieveLoop();
        logger.info("FastSync: state trie download complete!");
        last = 0; logStat();

        if (stateDS instanceof Flushable) {
            ((Flushable) stateDS).flush();
        }

        logger.info("FastSync: starting downloading ancestors of the pivot block: " + pivot.getShortDescr());
        downloader.startImporting(pivot.getHash());
        while (downloader.getDownloadedBlocksCount() < 1000) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        logger.info("FastSync: downloaded more than 256 ancestor blocks, proceeding with live sync");
//        blockStore.saveBlock(pivot, pivot.getCumulativeDifficulty(), true);
        blockchain.setBestBlock(blockStore.getBlockByHash(pivot.getHash()));
        config.setSyncEnabled(true);
        syncManager.init(channelManager, pool);

//        downloadPreviousBlocks();
//        downloadPreviousReceipts();

        // set indicator that we can send [STATUS] with the latest block
        // before we should report best block #0
    }

    private BlockHeader getPivotBlock() {
        logger.info("FastSync: looking for best block number...");
        BlockIdentifier bestKnownBlock;

        long s = System.currentTimeMillis();
        while (true) {
            Channel bestIdle = pool.getBestIdle();
            if (bestIdle != null) {
                bestKnownBlock = bestIdle.getEthHandler().getBestKnownBlock();
                if (bestKnownBlock.getNumber() > 1000) {
                    logger.info("FastSync: best block " + bestKnownBlock + " found with peer " + bestIdle);
                    break;
                }
            }

            long t = System.currentTimeMillis();
            if (t - s > 5000) {
                logger.info("FastSync: waiting for a peer to sync with...");
                s = t;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        long pivotBlockNumber = bestKnownBlock.getNumber() - 1000;
        logger.info("FastSync: fetching pivot block #" + pivotBlockNumber);

        try {
            while (true) {
                Channel bestIdle = pool.getBestIdle();
                if (bestIdle != null) {
                    ListenableFuture<List<BlockHeader>> future = bestIdle.getEthHandler().sendGetBlockHeaders(pivotBlockNumber, 1, false);
                    List<BlockHeader> blockHeaders = future.get(3, TimeUnit.SECONDS);
                    if (!blockHeaders.isEmpty()) {
                        BlockHeader ret = blockHeaders.get(0);
                        logger.info("Pivot header fetched: " + ret.getShortDescr());
                        return ret;
                    }
                }

                long t = System.currentTimeMillis();
                if (t - s > 5000) {
                    logger.info("FastSync: waiting for a peer to fetch pivot block...");
                    s = t;
                }

                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.error("Unexpected", e);
            throw new RuntimeException(e);
        }
    }

}
