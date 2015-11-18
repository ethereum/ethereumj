package org.ethereum.net.server;

import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    ChannelManagerListener channelManagerListener = null;

    @PostConstruct
    public void init() {
        mainWorker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                processNewPeers();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void setChannelManagerListener(ChannelManagerListener channelManagerListener) {

        this.channelManagerListener = channelManagerListener;
        synchronized (activePeers) {
            for (Channel peer : activePeers.values()) {
                this.channelManagerListener.onNewPeerChannel(peer);
            }
        }
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
            if (channelManagerListener != null) {
                channelManagerListener.onNewPeerChannel(peer);
            }
            activePeers.put(peer.getNodeIdWrapper(), peer);
        }
    }

    public void sendTransaction(Transaction tx) {

        synchronized (activePeers) {
            for (Channel channel : activePeers.values())
                channel.sendTransaction(tx);
        }
    }

    public void add(Channel peer) {
        newPeers.add(peer);
    }

    public void notifyDisconnect(Channel channel) {
        logger.debug("Peer {}: notifies about disconnect", channel.getPeerIdShort());
        channel.onDisconnect();
        if (channelManagerListener != null) {
            channelManagerListener.onPeerChannelDisconnected(channel);
        }
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
