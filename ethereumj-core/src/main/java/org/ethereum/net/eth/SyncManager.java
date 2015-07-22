package org.ethereum.net.eth;

import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
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

    private static final int PEERS_COUNT = 5;

    private SyncState state = SyncState.INIT;
    private EthHandler masterPeer;
    private List<EthHandler> peers = Collections.synchronizedList(new ArrayList<EthHandler>());

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private Ethereum ethereum;

    private byte[] bestHash;

    @PostConstruct
    public void init() {
        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                checkMaster();
                checkPeers();
                askNewPeers();
            }
        }, 0, 1, TimeUnit.SECONDS);
        if(logger.isInfoEnabled()) {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logStats();
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
    }

    private void checkMaster() {
        if(isHashRetrieving() && masterPeer.isHashRetrievingDone()) {
            changeState(SyncState.BLOCK_RETRIEVING);
        }
    }

    private void checkPeers() {
        List<EthHandler> removed = new ArrayList<>();
        for(EthHandler peer : peers) {
            if(peer.hasNoMoreBlocks()) {
                removed.add(peer);
                peer.changeState(SyncState.IDLE);
            }
        }
        peers.removeAll(removed);
    }

    private void askNewPeers() {
        if(peers.size() < PEERS_COUNT) {
            //TODO ask PeerDiscovery
        }
    }

    private void logStats() {
        for(EthHandler peer : peers) {
            peer.logSyncStats();
        }
    }

    public void removePeer(EthHandler peer) {
        peer.changeState(SyncState.IDLE);
        peers.remove(peer);
    }

    public void addPeer(EthHandler peer) {
        StatusMessage msg = peer.getHandshakeStatusMessage();

        BigInteger peerTotalDifficulty = msg.getTotalDifficultyAsBigInt();
        if(blockchain.getTotalDifficulty().compareTo(peerTotalDifficulty) > 0) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its difficulty lower than ours: {} vs {}, skipping",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    blockchain.getTotalDifficulty().toString()
            );
            // TODO report about lower total difficulty
            return;
        }

        BlockQueue chainQueue = blockchain.getQueue();
        BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();

        if ((highestKnownTotalDifficulty == null || peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0)) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: its chain is better than previously known: {} vs {}",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    peerTotalDifficulty.toString(),
                    highestKnownTotalDifficulty == null ? "0" : highestKnownTotalDifficulty.toString()
            );
            logger.debug(
                    "Peer {}: best hash [{}]",
                    Utils.getNodeIdShort(peer.getPeerId()),
                    Hex.toHexString(msg.getBestHash())
            );

            bestHash = msg.getBestHash();
            masterPeer = peer;
            chainQueue.setHighestTotalDifficulty(peerTotalDifficulty);

            changeState(SyncState.HASH_RETRIEVING);
        }

        if(state == SyncState.BLOCK_RETRIEVING) {
            peer.changeState(SyncState.BLOCK_RETRIEVING);
        }

        logger.info("Peer {}: adding to pool", Utils.getNodeIdShort(peer.getPeerId()));

        peers.add(peer);
    }

    public void changeState(SyncState newState) {
        if(newState == SyncState.HASH_RETRIEVING) {
            for(EthHandler peer : peers) {
                peer.changeState(SyncState.IDLE);
            }
            blockchain.getQueue().setBestHash(bestHash);
            masterPeer.changeState(SyncState.HASH_RETRIEVING);
        }
        if(newState == SyncState.BLOCK_RETRIEVING) {
            for(EthHandler peer : peers) {
                peer.changeState(SyncState.BLOCK_RETRIEVING);
            }
        }
        if(newState == SyncState.DONE_SYNC) {
            //TODO handle sync DONE
        }
        this.state = newState;
    }

    public void notifyNewNode(NodeHandler nodeHandler) {
        if(state == SyncState.DONE_SYNC) {
            return;
        }
        //TODO implement decision maker
        ethereum.connect(nodeHandler.getNode());
    }

    private boolean isHashRetrieving() {
        return state == SyncState.HASH_RETRIEVING;
    }
}
