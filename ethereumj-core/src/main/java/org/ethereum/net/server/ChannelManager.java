package org.ethereum.net.server;

import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;

import org.ethereum.sync.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import static org.ethereum.net.message.ReasonCode.DUPLICATE_PEER;

/**
 * @author Roman Mandeleil
 * @since 11.11.2014
 */
@Component
public class ChannelManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private List<Channel> newPeers = new CopyOnWriteArrayList<>();
    private final Map<ByteArrayWrapper, Channel> activePeers = Collections.synchronizedMap(new HashMap<ByteArrayWrapper, Channel>());

    private ScheduledExecutorService mainWorker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    SyncManager syncManager;

    @PostConstruct
    public void init() {
        mainWorker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    processNewPeers();
                } catch (Throwable t) {
                    logger.error("Error", t);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    private void processNewPeers() {
        List<Channel> processed = new ArrayList<>();
        for(Channel peer : newPeers) {

            if(peer.isProtocolsInitialized()) {

                if (!activePeers.containsKey(peer.getNodeIdWrapper())) {
                    process(peer);
                } else {
                    peer.disconnect(DUPLICATE_PEER);
                }

                processed.add(peer);
            }

        }

        newPeers.removeAll(processed);
    }

    private void process(Channel peer) {
        if(peer.hasEthStatusSucceeded()) {
            if (syncManager.isSyncDone()) {
                peer.onSyncDone();
            }
            syncManager.addPeer(peer);
            activePeers.put(peer.getNodeIdWrapper(), peer);
        }
    }

    public void sendTransaction(List<Transaction> tx, Channel receivedFrom) {
        synchronized (activePeers) {
            for (Channel channel : activePeers.values()) {
                if (channel != receivedFrom) {
                    channel.sendTransaction(tx);
                }
            }
        }
    }

    public void add(Channel peer) {
        newPeers.add(peer);
    }

    public void notifyDisconnect(Channel channel) {
        logger.debug("Peer {}: notifies about disconnect", channel.getPeerIdShort());
        channel.onDisconnect();
        syncManager.onDisconnect(channel);
        activePeers.values().remove(channel);
        newPeers.remove(channel);
    }

    public void onSyncDone() {

        synchronized (activePeers) {
            for (Channel channel : activePeers.values())
                channel.onSyncDone();
        }
    }
}
