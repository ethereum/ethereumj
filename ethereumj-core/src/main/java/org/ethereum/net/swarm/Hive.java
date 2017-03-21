/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.swarm;

import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.NodeEntry;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.ethereum.net.swarm.bzz.BzzPeersMessage;
import org.ethereum.net.swarm.bzz.BzzProtocol;
import org.ethereum.net.swarm.bzz.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

/**
 * Serves as an interface to the Kademlia. Manages the database of Nodes reported
 * by all the peers and selects from DB the nearest nodes to the specified hash Key
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class Hive {
    private final static Logger LOG = LoggerFactory.getLogger("net.bzz");

    private PeerAddress thisAddress;
    protected NodeTable nodeTable;

    private Map<Node, BzzProtocol> connectedPeers = new IdentityHashMap<>();

    public Hive(PeerAddress thisAddress) {
        this.thisAddress = thisAddress;
        nodeTable = new NodeTable(thisAddress.toNode());
    }

    public void start() {}
    public void stop() {}

    public PeerAddress getSelfAddress() {
        return thisAddress;
    }

    public void addPeer(BzzProtocol peer) {
        Node node = peer.getNode().toNode();
        nodeTable.addNode(node);
        connectedPeers.put(node, peer);
        LOG.info("Hive added a new peer: " + peer);
        peersAdded();
    }

    public void removePeer(BzzProtocol peer) {
        nodeTable.dropNode(peer.getNode().toNode());
    }

    /**
     * Finds the nodes which are not connected yet
     * TODO review this method later
     */
    public Collection<PeerAddress> getNodes(Key key, int max) {
        List<Node> closestNodes = nodeTable.getClosestNodes(key.getBytes());
        ArrayList<PeerAddress> ret = new ArrayList<>();
        for (Node node : closestNodes) {
            ret.add(new PeerAddress(node));
            if (--max == 0) break;
        }
        return ret;
    }

    /**
     * Returns the peers in the DB which are closest to the specified key
     * but not more peers than {#maxCount}
     */
    public Collection<BzzProtocol> getPeers(Key key, int maxCount) {
        List<Node> closestNodes = nodeTable.getClosestNodes(key.getBytes());
        ArrayList<BzzProtocol> ret = new ArrayList<>();
        for (Node node : closestNodes) {
            // TODO connect to Node
//            ret.add(thisPeer.getPeer(new PeerAddress(node)));
            BzzProtocol peer = connectedPeers.get(node);
            if (peer != null) {
                ret.add(peer);
                if (--maxCount == 0) break;
            } else {
                LOG.info("Hive connects to node " + node);
                NetStore.getInstance().worldManager.getActivePeer().connect(node.getHost(), node.getPort(), Hex.toHexString(node.getId()));
            }
        }
        return ret;
    }

    public void newNodeRecord(PeerAddress addr) {}

    /**
     * Adds the nodes received in the {@link BzzPeersMessage}
     */
    public void addPeerRecords(BzzPeersMessage req) {
        for (PeerAddress peerAddress : req.getPeers()) {
            nodeTable.addNode(peerAddress.toNode());
        }
        LOG.debug("Hive added new nodes: " + req.getPeers());
        peersAdded();
    }

    protected void peersAdded() {
        for (HiveTask task : new ArrayList<>(hiveTasks.keySet())) {
            if (!task.peersAdded()) {
                hiveTasks.remove(task);
                LOG.debug("HiveTask removed from queue: " + task);
            }
        }
    }

    /**
     * For testing
     */
    public Map<Node, BzzProtocol> getAllEntries() {
        Map<Node, BzzProtocol> ret = new LinkedHashMap<>();
        for (NodeEntry entry : nodeTable.getAllNodes()) {
            Node node = entry.getNode();
            BzzProtocol bzz = connectedPeers.get(node);
            ret.put(node, bzz);
        }
        return ret;
    }

    /**
     *  Adds a task with a search Key parameter. The task has a limited life time
     *  ({@link org.ethereum.net.swarm.Hive.HiveTask#expireTime} and a limited number of
     *  peers to process ({@link org.ethereum.net.swarm.Hive.HiveTask#maxPeers}).
     *  Until the task is alive and new Peer(s) is discovered by the Hive this task
     *  is invoked with another one closest Peer.
     *  This task may complete synchronously (i.e. before the method return) if the
     *  number of Peers in the Hive &gt;= maxPeers for that task.
     */
    public void addTask(HiveTask t) {
        if (t.peersAdded()) {
            LOG.debug("Added a HiveTask to queue: " + t);
            hiveTasks.put(t, null);
        }
    }

    private Map<HiveTask, Object> hiveTasks = new IdentityHashMap<>();

    /**
     * The task to be executed when another one closest Peer is discovered
     * until the timeout or maxPeers is reached.
     */
    public abstract class HiveTask {
        Key targetKey;
        Map<BzzProtocol, Object> processedPeers = new IdentityHashMap<>();
        long expireTime;
        int maxPeers;

        public HiveTask(Key targetKey, long timeout, int maxPeers) {
            this.targetKey = targetKey;
            this.expireTime = Util.curTime() + timeout;
            this.maxPeers = maxPeers;
        }

        /**
         * Notifies the task that new peers were connected.
         * @return true if the task wants to wait further for another peers
         * false if the task is completed
         */
        public boolean peersAdded() {
            if (Util.curTime() > expireTime) return false;
            Collection<BzzProtocol> peers = getPeers(targetKey, maxPeers);
            for (BzzProtocol peer : peers) {
                if (!processedPeers.containsKey(peer)) {
                    processPeer(peer);
                    processedPeers.put(peer, null);
                    if (processedPeers.size() > maxPeers) {
                        return false;
                    }
                }
            }
            return true;
        }

        protected abstract void processPeer(BzzProtocol peer);
    }
}
