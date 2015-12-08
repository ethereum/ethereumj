package org.ethereum.net.client;


import org.ethereum.net.rlpx.Node;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

/**
 * @author Tiberius Iliescu
 */
@Component
public class PeerClientManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    ApplicationContext ctx;

    @Autowired
    private PeerClient activePeer;


    public PeerClientManager() {
    }

    public void connect(Node node) {
        connect(node.getHost(), node.getPort(), node.getHexId());
    }

    public void connect(String ip, int port, String remoteId) {
        logger.info("Connecting to: {}:{}", ip, port);
        final PeerClient peerClient = ctx.getBean(PeerClient.class);
        Executors.newSingleThreadExecutor().submit(new ConnectTask(peerClient, ip, port, remoteId));
    }

    public PeerClient getDefaultPeerClient() {
        return activePeer;
    }

    public void setDefaultPeerClient(PeerClient peerClient) {
        activePeer = peerClient;
    }

    protected class ConnectTask implements Runnable {

        private PeerClient peerClient;

        private String ip;
        private int port;
        private String remoteId;

        public ConnectTask(PeerClient peerClient, String ip, int port, String remoteId) {
            this.peerClient = peerClient;
            this.ip = ip;
            this.port = port;
            this.remoteId = remoteId;
        }

        @Override
        public void run() {
            peerClient.connect(ip, port, remoteId);
        }
    }
}
