package org.ethereum.net.server;

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.NodeFilter;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;

import org.ethereum.facade.Ethereum;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.Node;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.SyncPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

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

    Random rnd = new Random();  // Used for distributing new blocks / hashes logic

    @Autowired
    SyncPool syncPool;

    @Autowired
    private Ethereum ethereum;

    private SystemProperties config;

    private SyncManager syncManager;

    private PeerServer peerServer;

    @Autowired
    private ChannelManager(final SystemProperties config, final SyncManager syncManager,
                           final PeerServer peerServer) {
        this.config = config;
        this.syncManager = syncManager;
        this.peerServer = peerServer;
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

        if (config.listenPort() > 0) {
            new Thread(new Runnable() {
                        public void run() {
                            peerServer.start(config.listenPort());
                        }
                    },
            "PeerServerThread").start();
        }
    }

    public void connect(Node node) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: initiate connection",
                node.getHexIdShort()
        );
        if (nodesInUse().contains(node.getHexId())) {
            if (logger.isTraceEnabled()) logger.trace(
                    "Peer {}: connection already initiated",
                    node.getHexIdShort()
            );
            return;
        }

        ethereum.connect(node);
    }

    public Set<String> nodesInUse() {
        Set<String> ids = new HashSet<>();
        for (Channel peer : getActivePeers()) {
            ids.add(peer.getPeerId());
        }
        for (Channel peer : newPeers) {
            ids.add(peer.getPeerId());
        }
        return ids;
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

        if (addCnt > 0) {
            logger.info("New peers processed: " + processed + ", active peers added: " + addCnt + ", total active peers: " + activePeers.size());
        }

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
                peer.onSyncDone(true);
                // So SyncManager could perform some tasks on recently connected peer
                syncManager.onNewPeer(peer);
            }
            synchronized (activePeers) {
                activePeers.put(peer.getNodeIdWrapper(), peer);
            }
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
     * Propagates the new block message across active peers
     * Suitable only for self-mined blocks
     * Use {@link #sendNewBlock(Block, Channel)} for sending blocks received from net
     * @param block  new Block to be sent
     */
    public void sendNewBlock(Block block) {
        synchronized (activePeers) {
            for (Channel channel : activePeers.values()) {
                channel.sendNewBlock(block);
            }
        }
    }

    /**
     * Propagates the new block message across active peers with exclusion of
     * 'receivedFrom' peer.
     * Distributes full block to 30% of peers and only its hash to remains
     * @param block  new Block to be sent
     * @param receivedFrom the peer which sent original message
     */
    public void sendNewBlock(Block block, Channel receivedFrom) {
        synchronized (activePeers) {
            for (Channel channel : activePeers.values()) {
                if (channel == receivedFrom) continue;
                if (rnd.nextInt(10) < 3) {  // 30%
                    channel.sendNewBlock(block);
                } else {                    // 70%
                    channel.sendNewBlockHashes(block);
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
        syncPool.onDisconnect(channel);
        activePeers.values().remove(channel);
        newPeers.remove(channel);
    }

    public void onSyncDone(boolean done) {

        synchronized (activePeers) {
            for (Channel channel : activePeers.values())
                channel.onSyncDone(done);
        }
    }

    public Collection<Channel> getActivePeers() {
        synchronized (activePeers) {
            return new ArrayList<>(activePeers.values());
        }
    }

    public Channel getActivePeer(byte[] nodeId) {
        return activePeers.get(new ByteArrayWrapper(nodeId));
    }

    public void close() {
        try {
            logger.info("Shutting down ChannelManager worker thread...");
            mainWorker.shutdownNow();
            mainWorker.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Problems shutting down", e);
        }
        peerServer.close();

        synchronized (activePeers) {
            ArrayList<Channel> allPeers = new ArrayList<>(activePeers.values());
            allPeers.addAll(newPeers);

            for (Channel channel : allPeers) {
                try {
                    channel.dropConnection();
                } catch (Exception e) {
                    logger.warn("Problems disconnecting channel " + channel, e);
                }
            }
        }
    }
}
