package org.ethereum.net.eth.sync;

import org.ethereum.core.Block;
import org.ethereum.facade.Blockchain;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.eth.BlockHashesMessage;
import org.ethereum.net.eth.BlocksMessage;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.StatusMessage;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
@Component
public class SyncManager {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    private Blockchain blockchain;

    private static final int FAILURES_THRESHOLD = 5;

    private volatile SyncStatus status = SyncStatus.INIT;

    private volatile EthHandler masterSyncHandler;
    private List<EthHandler> blockHandlers = new CopyOnWriteArrayList<>();
    private Map<EthHandler, Integer> failures = new ConcurrentHashMap<>();
    private Map<EthHandler, Integer> counters = new ConcurrentHashMap<>();

    public void handleStatus(EthHandler handler, StatusMessage msg) {

        if(status == SyncStatus.SYNC_DONE) {
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
            if(status == SyncStatus.BLOCK_RETRIEVING) {
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
            status = SyncStatus.HASHES_RETRIEVED;
        } else {
            Iterator<byte[]> hashIterator = receivedHashes.iterator();
            byte[] foundHash, latestHash = blockchain.getBestBlockHash();
            while (hashIterator.hasNext()) {
                foundHash = hashIterator.next();
                if (FastByteComparisons.compareTo(foundHash, 0, 32, latestHash, 0, 32) != 0) {
                    chainQueue.addHash(foundHash);    // store unknown hashes in queue until known hash is found
                } else {
                    status = SyncStatus.HASHES_RETRIEVED;
                    logger.trace("Catch up with the hashes until: {[]}", foundHash);
                    break;
                }
            }
        }

        if(status == SyncStatus.HASHES_RETRIEVED) {
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
            status = SyncStatus.BLOCKS_RETRIEVED;
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
        if(status != SyncStatus.BLOCK_RETRIEVING) {
            status = SyncStatus.BLOCK_RETRIEVING;
            for (EthHandler handler : blockHandlers) {
                handler.sendGetBlocks();
            }
        }
    }

    private void initiateGetBlockHashes() {
        status = SyncStatus.HASH_RETRIEVING;
        masterSyncHandler.sendGetBlockHashes();
    }
}
