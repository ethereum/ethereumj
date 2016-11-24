package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Flushable;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.handler.Eth63;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.*;
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
public class FastSyncManager {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    private final static long REQUEST_TIMEOUT = 5 * 1000;
    private final static int REQUEST_MAX_NODES = 512;
    private final static int NODE_QUEUE_BEST_SIZE = 100_000;
    private final static int MIN_PEERS_FOR_PIVOT_SELECTION = 5;
    private final static int FORCE_SYNC_TIMEOUT = 60 * 1000;

    private static final Capability ETH63_CAPABILITY = new Capability(Capability.ETH, (byte) 63);

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
    KeyValueDataSource stateDS = new HashMapDB();

    @Autowired
    FastSyncDownloader downloader;

    int nodesInserted = 0;
    private ScheduledExecutorService logExecutor = Executors.newSingleThreadScheduledExecutor();

    private BlockingQueue<TrieNodeRequest> dbWriteQueue = new LinkedBlockingQueue<>();

    void init() {
        new Thread("FastSyncDBWriter") {
            @Override
            public void run() {
                try {
                    while (true) {
                        TrieNodeRequest request = dbWriteQueue.take();
                        stateDS.put(request.nodeHash, request.response);
                    }
                } catch (Exception e) {
                    logger.error("Fatal FastSync error while writing data", e);
                }
            }
        }.start();

        new Thread("FastSyncLoop") {
            @Override
            public void run() {
                try {
                    main();
                } catch (Exception e) {
                    logger.error("Fatal FastSync loop error", e);
                }
            }
        }.start();
    }

    enum TrieNodeType {
        STATE,
        STORAGE,
        CODE
    }

    private class TrieNodeRequest {
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
        dbWriteQueue.add(req);
        knownHashes.remove(req.nodeHash);
        nodesInserted++;
        for (TrieNodeRequest childRequest : req.createChildRequests()) {
            if (!knownHashes.contains(childRequest.nodeHash) && stateDS.get(childRequest.nodeHash) == null) {
                if (nodesQueue.size() > NODE_QUEUE_BEST_SIZE) {
                    // reducing queue by traversing tree depth-first
                    nodesQueue.addFirst(childRequest);
                } else {
                    // enlarging queue by traversing tree breadth-first
                    nodesQueue.add(childRequest);
                }
                knownHashes.add(childRequest.nodeHash);
            }
        }
    }

    boolean requestNextNodes(int cnt) {
        final Channel idle = pool.getAnyIdle();

        if (idle != null) {
            final List<byte[]> hashes = new ArrayList<>();
            final List<TrieNodeRequest> requestsSent = new ArrayList<>();
            synchronized (this) {
                for (int i = 0; i < cnt && !nodesQueue.isEmpty(); i++) {
                    TrieNodeRequest req = nodesQueue.poll();
                    req.reqSent();
                    hashes.add(req.nodeHash);
                    requestsSent.add(req);
                    pendingNodes.put(req.nodeHash, req);
                }
            }
            if (hashes.size() > 0) {
                logger.trace("Requesting " + hashes.size() + " nodes from peer: " + idle);
                ListenableFuture<List<Pair<byte[], byte[]>>> nodes = ((Eth63) idle.getEthHandler()).requestTrieNodes(hashes);
                final long reqTime = System.currentTimeMillis();
                Futures.addCallback(nodes, new FutureCallback<List<Pair<byte[], byte[]>>>() {
                    @Override
                    public void onSuccess(List<Pair<byte[], byte[]>> result) {
                        try {
                            synchronized (FastSyncManager.this) {
                                logger.trace("Received " + result.size() + " nodes (of " + hashes.size() + ") from peer: " + idle);
                                for (Pair<byte[], byte[]> pair : result) {
                                    TrieNodeRequest request = pendingNodes.remove(pair.getKey());
                                    if (request == null) {
                                        long t = System.currentTimeMillis();
                                        logger.debug("Received node which was not requested: " + Hex.toHexString(pair.getKey()) + " from " + idle);
                                        return;
                                    }
                                    request.response = pair.getValue();
                                    processResponse(request);
                                }

                                FastSyncManager.this.notifyAll();

                                idle.getNodeStatistics().eth63NodesRequested.add(hashes.size());
                                idle.getNodeStatistics().eth63NodesReceived.add(result.size());
                                idle.getNodeStatistics().eth63NodesRetrieveTime.add(System.currentTimeMillis() - reqTime);
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
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    void retrieveLoop() {
        try {
            while (!nodesQueue.isEmpty() || !pendingNodes.isEmpty()) {
                try {
                    processTimeouts();

                    while (requestNextNodes(REQUEST_MAX_NODES)) ;

                    synchronized (this) {
                        wait(10);
                    }
                    logStat();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable t) {
                    logger.error("Error", t);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Main fast sync loop was interrupted", e);
        }
    }

    long last = 0;
    long lastNodeCount = 0;

    private void logStat() {
        long cur = System.currentTimeMillis();
        if (cur - last > 5000) {
            logger.info("FastSync: received: " + nodesInserted + ", known: " + nodesQueue.size() + ", pending: " + pendingNodes.size()
                    + String.format(", nodes/sec: %1$.2f", 1000d * (nodesInserted - lastNodeCount) / (cur - last)));
            last = cur;
            lastNodeCount = nodesInserted;
        }
    }

    public void main() {

        if (blockchain.getBestBlock().getNumber() == 0) {
            startLogWorker();

            BlockHeader pivot = getPivotBlock();

            // Temporary avoid Parity due to bug https://github.com/ethcore/parity/issues/2887
            pool.setNodesSelector(new Functional.Predicate<NodeHandler>() {
                @Override
                public boolean test(NodeHandler handler) {
                    if (!handler.getNodeStatistics().capabilities.contains(ETH63_CAPABILITY))
                        return false;
                    return true;
                }
            });

            byte[] pivotStateRoot = pivot.getStateRoot();
            TrieNodeRequest request = new TrieNodeRequest(TrieNodeType.STATE, pivotStateRoot);
            nodesQueue.add(request);
            logger.info("FastSync: downloading state trie at pivot block: " + pivot.getShortDescr());

            stateDS.put(CommonConfig.FASTSYNC_DB_KEY, new byte[]{1});

            retrieveLoop();

            stateDS.delete(CommonConfig.FASTSYNC_DB_KEY);

            logger.info("FastSync: state trie download complete!");
            last = 0;
            logStat();

            if (stateDS instanceof Flushable) {
                ((Flushable) stateDS).flush();
            }

            pool.setNodesSelector(null);

            logger.info("FastSync: starting downloading ancestors of the pivot block: " + pivot.getShortDescr());
            downloader.startImporting(pivot.getHash());
            while (downloader.getDownloadedBlocksCount() < 256) {
                // we need 256 previous blocks to correctly execute BLOCKHASH EVM instruction
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            logger.info("FastSync: downloaded more than 256 ancestor blocks, proceeding with live sync");

            blockchain.setBestBlock(blockStore.getBlockByHash(pivot.getHash()));
        } else {
            logger.info("FastSync: current best block is > 0 (" + blockchain.getBestBlock().getShortDescr() + "). " +
                    "Continue with regular sync...");
        }

        syncManager.initRegularSync();

        // set indicator that we can send [STATUS] with the latest block
        // before we should report best block #0
    }

    private BlockHeader getPivotBlock() {
        byte[] pivotBlockHash = config.getFastSyncPivotBlockHash();
        long pivotBlockNumber = 0;

        long start = System.currentTimeMillis();
        long s = start;

        if (pivotBlockHash != null) {
            logger.info("FastSync: fetching trusted pivot block with hash " + Hex.toHexString(pivotBlockHash));
        } else {
            logger.info("FastSync: looking for best block number...");
            BlockIdentifier bestKnownBlock;

            while (true) {
                List<Channel> allIdle = pool.getAllIdle();

                if (allIdle.size() >= MIN_PEERS_FOR_PIVOT_SELECTION
                        || (System.currentTimeMillis() - start > FORCE_SYNC_TIMEOUT && !allIdle.isEmpty())) {
                    Channel bestPeer = allIdle.get(0);
                    for (Channel channel : allIdle) {
                        if (bestPeer.getEthHandler().getBestKnownBlock().getNumber() < channel.getEthHandler().getBestKnownBlock().getNumber()) {
                            bestPeer = channel;
                        }
                    }
                    bestKnownBlock = bestPeer.getEthHandler().getBestKnownBlock();
                    if (bestKnownBlock.getNumber() > 1000) {
                        logger.info("FastSync: best block " + bestKnownBlock + " found with peer " + bestPeer);
                        break;
                    }
                }

                long t = System.currentTimeMillis();
                if (t - s > 5000) {
                    logger.info("FastSync: waiting for at least " + MIN_PEERS_FOR_PIVOT_SELECTION + " peers to select pivot block... ("
                            + allIdle.size() + " peers so far)");
                    s = t;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            pivotBlockNumber = bestKnownBlock.getNumber() - 500;
            logger.info("FastSync: fetching pivot block #" + pivotBlockNumber);
        }

        try {
            while (true) {
                Channel bestIdle = pool.getAnyIdle();
                if (bestIdle != null) {
                    try {
                        ListenableFuture<List<BlockHeader>> future = pivotBlockHash == null ?
                                bestIdle.getEthHandler().sendGetBlockHeaders(pivotBlockNumber, 1, false) :
                                bestIdle.getEthHandler().sendGetBlockHeaders(pivotBlockHash, 1, 0, false);
                        List<BlockHeader> blockHeaders = future.get(3, TimeUnit.SECONDS);
                        if (!blockHeaders.isEmpty()) {
                            BlockHeader ret = blockHeaders.get(0);

                            if (pivotBlockHash != null && !FastByteComparisons.equal(pivotBlockHash, ret.getHash())) {
                                logger.warn("Peer " + bestIdle + " returned pivot block with another hash: " +
                                        Hex.toHexString(ret.getHash()) + " Dropping the peer.");
                                bestIdle.disconnect(ReasonCode.USELESS_PEER);
                                continue;
                            }

                            logger.info("Pivot header fetched: " + ret.getShortDescr());
                            return ret;
                        }
                    } catch (TimeoutException e) {
                        logger.debug("Timeout waiting for answer", e);
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

    private void startLogWorker() {
        logExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    pool.logActivePeers();
                    logger.info("\n");
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Exception in log worker", t);
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

}
