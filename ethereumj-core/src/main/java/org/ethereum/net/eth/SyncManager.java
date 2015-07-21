package org.ethereum.net.eth;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.ethereum.core.Block;
import org.ethereum.facade.Blockchain;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.eth.BlockHashesMessage;
import org.ethereum.net.eth.BlocksMessage;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.StatusMessage;
import org.ethereum.net.eth.sync.SyncState;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    private Blockchain blockchain;

    private static final int MAX_PEERS_COUNT = 5;
    private static final int FAILURES_THRESHOLD = 5;

    volatile EthHandler masterPeer;
    List<EthHandler> peers = Collections.synchronizedList(new ArrayList<EthHandler>());

    volatile SyncState state = SyncState.INIT;
    volatile EthHandler masterSyncHandler;

    List<EthHandler> blockHandlers = new CopyOnWriteArrayList<>();
    Map<EthHandler, Integer> failures = new ConcurrentHashMap<>();
    Map<EthHandler, Integer> counters = new ConcurrentHashMap<>();


    ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void removePeer(EthHandler peer) {
        //TODO stop
        peers.remove(peer);
    }

    public void addPeer(EthHandler peer) {
        peers.add(peer);
        //TODO add and start
    }

    public void changeState(SyncState newState) {
        if(newState == SyncState.HASHES_RETRIEVING) {
            blockchain.getQueue().clearHashes();
            masterPeer.
        }
        if(newState == SyncState.BLOCKS_RETRIEVING) {

        }
        if(newState == SyncState.DONE) {

        }

        switch(state) {
            case INIT:
                switch(newState) {
                    case HASHES:
                        masterPeer.sendGetBlockHashes();
                        break;
                    default:
                        return;
                }
            case HASHES:
                switch(newState) {
                    case HASHES:
                        //TODO stop downloading
                        //clear
                    case BLOCKS:
                    default: return;
                }
            case BLOCKS:
                switch(newState) {
                    case HASHES:
                    case DONE:
                    default: return;
                }
            default:
                return;
        }
        this.state = newState;
    }

    private void

    public void handleStatus(EthHandler handler, StatusMessage msg) {

        if(status == SyncState.SYNC_DONE) {
            return;
        }

        BlockQueue chainQueue = blockchain.getQueue();
        BigInteger peerTotalDifficulty = new BigInteger(1, msg.getTotalDifficulty());
        BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();

        if ((highestKnownTotalDifficulty == null ||
                peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0)) {

            logger.info(
                    "Their chain is better than previously known: total difficulty : {} vs {}",
                    peerTotalDifficulty.toString(),
                    highestKnownTotalDifficulty == null ? "0" : highestKnownTotalDifficulty.toString()
            );

            masterSyncHandler = handler;

            chainQueue.setHighestTotalDifficulty(peerTotalDifficulty);
            chainQueue.setBestHash(msg.getBestHash());

            initiateGetBlockHashes();
        }

        if(peerTotalDifficulty.compareTo(blockchain.getTotalDifficulty()) > 0) {
            logger.info(
                    "Their chain is better than ours: total difficulty : {} vs {}",
                    peerTotalDifficulty.toString(),
                    blockchain.getTotalDifficulty()
            );
            addBlockHandler(handler);
            if(status == SyncState.BLOCK_RETRIEVING) {
                handler.sendGetBlocks();
            }
        }
    }

    public void handleBlockHashes(EthHandler handler, BlockHashesMessage msg) {
        if(masterSyncHandler != handler) {
            return;
        }

        List<byte[]> receivedHashes = msg.getBlockHashes();
        BlockQueue chainQueue = blockchain.getQueue();

        // result is empty, peer has no more hashes
        // or peer doesn't have the best hash anymore
        if (receivedHashes.isEmpty()) {
            status = SyncState.HASHES_RETRIEVED;
        } else {
            chainQueue.addHashes(receivedHashes);
            // store unknown hashes in queue until known hash is found
            final byte[] latestHash = blockchain.getBestBlockHash();
            byte[] foundHash = CollectionUtils.find(receivedHashes, new Predicate<byte[]>() {
                @Override
                public boolean evaluate(byte[] hash) {
                    return FastByteComparisons.compareTo(hash, 0, 32, latestHash, 0, 32) == 0;
                }
            });
            if (foundHash != null) {
                status = SyncState.HASHES_RETRIEVED; // store unknown hashes in queue until known hash is found
                logger.trace("Catch up with the hashes until: {[]}", foundHash);
            }
        }

        if(status == SyncState.HASHES_RETRIEVED) {
            logger.info(" Block hashes sync completed: {} hashes in queue", chainQueue.getHashes().size());
            chainQueue.addHash(blockchain.getBestBlockHash());
            initiateGetBlocks();
        } else {
            // no known hash has been reached
            chainQueue.logHashQueueSize();
            masterSyncHandler.sendGetBlockHashes(); // another getBlockHashes with last received hash.
        }
    }

    public void handleBlocks(EthHandler handler, BlocksMessage msg) {
        List<Block> blockList = msg.getBlocks();
        blockchain.getQueue().addBlocks(blockList);
        blockchain.getQueue().logHashQueueSize();

        if(blockchain.getQueue().isHashesEmpty()) {
            logger.info(" Block retrieving process fully complete");
            status = SyncState.BLOCKS_RETRIEVED;
            clearBlockHandlers();
            return;
        }

        int cnt = counters.get(handler);
        cnt += blockList.size();
        counters.put(handler, cnt);

        if(logger.isInfoEnabled()) {
            Long headNumber = null;
            if (!blockList.isEmpty()) {
                Block head = Collections.max(blockList, new Comparator<Block>() {
                    @Override
                    public int compare(Block o1, Block o2) {
                        return Long.valueOf(o1.getNumber()).compareTo(o2.getNumber());
                    }
                });
                headNumber = head.getNumber();
            }
            logger.info("handler {}: {} blocks loaded, max block number {}",
                    blockHandlers.indexOf(handler), cnt, String.valueOf(headNumber));
        }

        if(blockList.isEmpty()) {
            int failureCnt = failures.get(handler) + 1;
            failures.put(handler, failureCnt);
            if(failureCnt > FAILURES_THRESHOLD) {
                removeBlockHandler(handler);
                return;
            }
        } else {
            failures.put(handler, 0);
        }

        handler.sendGetBlocks();
    }

    private void addBlockHandler(EthHandler handler) {
        blockHandlers.add(handler);
        failures.put(handler, 0);
        counters.put(handler, 0);
    }

    private void removeBlockHandler(EthHandler handler) {
        logger.info("handler {}: remove", blockHandlers.indexOf(handler));
        blockHandlers.remove(handler);
        failures.remove(handler);
        counters.remove(handler);
    }

    private void clearBlockHandlers() {
        blockHandlers.clear();
        failures.clear();
        counters.clear();
    }

    private void initiateGetBlocks() {
        if(status != SyncState.BLOCK_RETRIEVING) {
            status = SyncState.BLOCK_RETRIEVING;
            for (EthHandler handler : blockHandlers) {
                handler.sendGetBlocks();
            }
        }
    }

    private void initiateGetBlockHashes() {
        status = SyncState.HASH_RETRIEVING;
        masterSyncHandler.sendGetBlockHashes();
    }

    private class NodeListener extends EthereumListenerAdapter {

        public void onNodeDiscovered(NodeHandler node) {

        }
    }
}
