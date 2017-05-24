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
package org.ethereum.net.rlpx.discover.table;

import org.ethereum.net.rlpx.Node;

import java.util.*;

/**
 * Created by kest on 5/25/15.
 */
public class NodeTable {

    private final Node node;  // our node
    private transient NodeBucket[] buckets;
    private transient List<NodeEntry> nodes;
    private Map<Node, Node> evictedCandidates = new HashMap<>();
    private Map<Node, Date> expectedPongs = new HashMap<>();

    public NodeTable(Node n) {
        this(n, true);
    }

    public NodeTable(Node n, boolean includeHomeNode) {
        this.node = n;
        initialize();
        if (includeHomeNode) {
            addNode(this.node);
        }
    }

    public Node getNode() {
        return node;
    }

    public final void initialize()
    {
        nodes = new ArrayList<>();
        buckets = new NodeBucket[KademliaOptions.BINS];
        for (int i = 0; i < KademliaOptions.BINS; i++)
        {
            buckets[i] = new NodeBucket(i);
        }
    }

    public synchronized Node addNode(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        NodeEntry lastSeen = buckets[getBucketId(e)].addNode(e);
        if (lastSeen != null) {
            return lastSeen.getNode();
        }
        if (!nodes.contains(e)) {
            nodes.add(e);
        }
        return null;
    }

    public synchronized void dropNode(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        buckets[getBucketId(e)].dropNode(e);
        nodes.remove(e);
    }

    public synchronized boolean contains(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        for (NodeBucket b : buckets) {
            if (b.getNodes().contains(e)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void touchNode(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        for (NodeBucket b : buckets) {
            if (b.getNodes().contains(e)) {
                b.getNodes().get(b.getNodes().indexOf(e)).touch();
                break;
            }
        }
    }

    public int getBucketsCount() {
        int i = 0;
        for (NodeBucket b : buckets) {
            if (b.getNodesCount() > 0) {
                i++;
            }
        }
        return i;
    }

    public synchronized NodeBucket[] getBuckets() {
        return buckets;
    }

    public int getBucketId(NodeEntry e) {
        int id = e.getDistance() - 1;
        return id < 0 ? 0 : id;
    }

    public synchronized int getNodesCount() {
        return nodes.size();
    }

    public synchronized List<NodeEntry> getAllNodes()
    {
        List<NodeEntry> nodes = new ArrayList<>();

        for (NodeBucket b : buckets)
        {
//            nodes.addAll(b.getNodes());
            for (NodeEntry e : b.getNodes())
            {
                if (!e.getNode().equals(node)) {
                    nodes.add(e);
                }
            }
        }

//        boolean res = nodes.remove(node);
        return nodes;
    }

    public synchronized List<Node> getClosestNodes(byte[] targetId) {
        List<NodeEntry> closestEntries = getAllNodes();
        List<Node> closestNodes = new ArrayList<>();
        Collections.sort(closestEntries, new DistanceComparator(targetId));
        if (closestEntries.size() > KademliaOptions.BUCKET_SIZE) {
            closestEntries = closestEntries.subList(0, KademliaOptions.BUCKET_SIZE);
        }

        for (NodeEntry e : closestEntries) {
            if (!e.getNode().isDiscoveryNode()) {
                closestNodes.add(e.getNode());
            }
        }
        return closestNodes;
    }
}
