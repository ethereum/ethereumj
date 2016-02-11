package org.ethereum.net.server;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.NodeFilter;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;

import org.ethereum.net.message.ReasonCode;
import org.ethereum.sync.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import static org.ethereum.net.message.ReasonCode.DUPLICATE_PEER;
import static org.ethereum.net.message.ReasonCode.TOO_MANY_PEERS;

/**
 * @author Roman Mandeleil
 * @since 11.11.2014
 */
@Component
public class ChannelManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    // If the inbound peer connection was dropped by us with a reason message
    // then we ban that peer IP on any connections for some time to protect from
    // too active peers
    private static final int inboundConnectionBanTimeout = 10 * 1000;

    private List<Channel> newPeers = new CopyOnWriteArrayList<>();
    private final Map<ByteArrayWrapper, Channel> activePeers = Collections.synchronizedMap(new HashMap<ByteArrayWrapper, Channel>());

    private ScheduledExecutorService mainWorker = Executors.newSingleThreadScheduledExecutor();
    private int maxActivePeers;
    private Map<InetAddress, Date> recentlyDisconnected = Collections.synchronizedMap(new LRUMap<InetAddress, Date>(500));
    private NodeFilter trustedPeers;

    @Autowired
    SystemProperties config;

    @Autowired
    SyncManager syncManager;

    @PostConstruct
    public void init() {
        maxActivePeers = config.maxActivePeers();
        trustedPeers = config.peerTrusted();
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
        if (newPeers.isEmpty()) return;

        List<Channel> processed = new ArrayList<>();

        int addCnt = 0;
        for(Channel peer : newPeers) {

            if(peer.isProtocolsInitialized()) {

                if (!activePeers.containsKey(peer.getNodeIdWrapper())) {
                    if (!peer.isActive() &&
                        activePeers.size() >= maxActivePeers &&
                        !trustedPeers.accept(peer.getNode())) {

                        // restricting inbound connections unless this is a trusted peer

                        disconnect(peer, TOO_MANY_PEERS);
                    } else {
                        process(peer);
                        addCnt++;
                    }
                } else {
                    disconnect(peer, DUPLICATE_PEER);
                }

                processed.add(peer);
            }
        }

        logger.info("New peers processed: " + processed + ", active peers added: " + addCnt + ", total active peers: " + activePeers.size());

        newPeers.removeAll(processed);
    }

    private void disconnect(Channel peer, ReasonCode reason) {
        logger.debug("Disconnecting peer with reason " + reason + ": " + peer);
        peer.disconnect(reason);
        recentlyDisconnected.put(peer.getInetSocketAddress().getAddress(), new Date());
    }

    public boolean isRecentlyDisconnected(InetAddress peerAddr) {
        Date disconnectTime = recentlyDisconnected.get(peerAddr);
        if (disconnectTime != null &&
                System.currentTimeMillis() - disconnectTime.getTime() < inboundConnectionBanTimeout) {
            return true;
        } else {
            recentlyDisconnected.remove(peerAddr);
            return false;
        }
    }

    private void process(Channel peer) {
        if(peer.hasEthStatusSucceeded()) {
            // prohibit transactions processing until main sync is done
            if (syncManager.isSyncDone()) {
                peer.onSyncDone();
            }
            syncManager.addPeer(peer);
            activePeers.put(peer.getNodeIdWrapper(), peer);
        }
    }

    /**
     * Propagates the transactions message across active peers with exclusion of
     * 'receivedFrom' peer.
     * @param tx  transactions to be sent
     * @param receivedFrom the peer which sent original message or null if
     *                     the transactions were originated by this peer
     */
    public void sendTransaction(List<Transaction> tx, Channel receivedFrom) {
        synchronized (activePeers) {
            for (Channel channel : activePeers.values()) {
                if (channel != receivedFrom) {
                    channel.sendTransaction(tx);
                }
            }
        }
    }

    /**
     * Propagates the new block message across active peers with exclusion of
     * 'receivedFrom' peer.
     * @param block  new Block to be sent
     * @param receivedFrom the peer which sent original message or null if
     *                     the block has been mined by us
     */
    public void sendNewBlock(Block block, Channel receivedFrom) {
        synchronized (activePeers) {
            for (Channel channel : activePeers.values()) {
                if (channel != receivedFrom) {
                    channel.sendNewBlock(block);
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
