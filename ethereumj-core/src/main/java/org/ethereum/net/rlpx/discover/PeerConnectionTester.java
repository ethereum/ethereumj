package org.ethereum.net.rlpx.discover;

import org.apache.commons.codec.binary.Hex;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.DiscoverListener;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Anton Nashatyrev on 17.07.2015.
 */
@Component
public class PeerConnectionTester implements DiscoverListener {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    @Autowired
    WorldManager worldManager;

    Map<NodeHandler, Object> connectedCandidates = new IdentityHashMap<>();
    ExecutorService peerConnectionPool = Executors.newFixedThreadPool(8);

    public PeerConnectionTester() {
    }

    private void connectNewPeer(NodeHandler nodeHandler) {
        try {
            if (nodeHandler != null) {
                nodeHandler.getNodeStatistics().rlpxConnectionAttempts.add();
                logger.debug("Trying node connection: " + nodeHandler);
                Node node = nodeHandler.getNode();
                worldManager.getActivePeer().connect(node.getHost(), node.getPort(),
                        Hex.encodeHexString(node.getId()), true);
                connectionTerminated(nodeHandler);
                logger.debug("Terminated node connection: " + nodeHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectionTerminated(NodeHandler nodeHandler) {}

    @Override
    public void nodeStatusChanged(DiscoverNodeEvent event) {
        final NodeHandler nodeHandler = event.getNode();
        if ((nodeHandler.getState() == NodeHandler.State.Active ||
                nodeHandler.getState() == NodeHandler.State.Alive) &&
                !connectedCandidates.containsKey(nodeHandler)) {
            logger.debug("Submitting node for RLPx connection : " + nodeHandler);
            connectedCandidates.put(nodeHandler, null);
            peerConnectionPool.submit(new Runnable() {
                @Override
                public void run() {
                    connectNewPeer(nodeHandler);
                }
            });
        }
    }
}
