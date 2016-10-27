package org.ethereum.sync;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockchainImpl;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static org.ethereum.util.CompactEncoder.hasTerminator;

/**
 * Created by Anton Nashatyrev on 24.10.2016.
 */
@Component
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
//                    if (FastByteComparisons.equal(Hex.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"), state.getStateRoot())) {
//                        System.out.println("TrieNodeRequest.createChildRequests");
//                    }
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
//            if (reqCount > 5) {
//                logger.warn("Node couldn't be fetched for " + reqCount + " times: " + this);
//            }
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

        Block pivot = getPivotBlock();

        byte[] pivotStateRoot = pivot.getStateRoot();
//        byte[] pivotStateRoot = Hex.decode("209230089ff328b2d87b721c48dbede5fd163c3fae29920188a7118275ab2013");
        TrieNodeRequest request = new TrieNodeRequest(TrieNodeType.STATE, pivotStateRoot);
        nodesQueue.add(request);
        logger.info("Starting FastSync from state root: " + Hex.toHexString(pivotStateRoot));
        retrieveLoop();
        logger.info("FastSync complete!");
        last = 0; logStat();
//        ((Flushable) stateDS).flush();

        downloadPrevious256Blocks();
        blockStore.saveBlock(pivot, pivot.getCumulativeDifficulty(), true);
        blockchain.setBestBlock(pivot);
        config.setSyncEnabled(true);
        syncManager.init(channelManager, pool);

        downloadPreviousBlocks();
        downloadPreviousReceipts();

        // set indicator that we can send [STATUS] with the latest block
        // before we should report best block #0
    }

    private void downloadPreviousReceipts() {

    }

    private void downloadPrevious256Blocks() {
    }

    private void downloadPreviousBlocks() {
        
    }

    private Block getPivotBlock() {
        return new Block(Hex.decode("f9020cf90207a016c03af532a08350b051cb60a206087ec15b8f940f645de1633f2d0bf4db4e1ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794c0ea08a2d404d3172d2add29a45be56da40e2949a005c244b80f171e9d8f87531790d8dfebd236ef9db390e38d89abe30b507fdd64a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008660ba79d57cc383265424831e848080845810c6198a7777772e62772e636f6da06c01fad0847a92caf96b99435ea259b84ef8d2a4e20e365ad93407f9738c237f881231c74804ceb692c0c0"));
    }

}
