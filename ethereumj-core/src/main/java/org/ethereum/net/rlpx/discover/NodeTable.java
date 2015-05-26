package org.ethereum.net.rlpx.discover;

import org.ethereum.net.dht.Bucket;
import org.ethereum.net.rlpx.Node;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

/**
 * Created by kest on 5/25/15.
 */
public class NodeTable {

    private final Node node;  // our node
    private transient NodeBucket[] buckets;
    private transient Set<NodeEntry> nodes;

    NodeTable(Node n) {
        this.node = n;
        initialize();
        addNode(this.node);
    }

    public final void initialize()
    {
        nodes = new HashSet<>();
        buckets = new NodeBucket[KademliaOptions.BINS];
        for (int i = 0; i < KademliaOptions.BINS; i++)
        {
            buckets[i] = new NodeBucket();
        }
    }

    public void addNode(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        nodes.add(e);
        buckets[getBucketId(e)].addNode(e);
    }

    public void dropNode(Node n) {
        NodeEntry e = new NodeEntry(node.getId(), n);
        buckets[getBucketId(e)].dropNode(e);
        nodes.remove(e);
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

    public int getBucketId(NodeEntry e) {
        int id = e.getDistance() - 1;
        return id < 0 ? 0 : id;
    }

    public List<NodeEntry> getAllNodes()
    {
        List<NodeEntry> nodes = new ArrayList<>();

        for (NodeBucket b : buckets)
        {
            for (NodeEntry e : b.getNodes())
            {
                nodes.add(e);
            }
        }

        return nodes;
    }

    public List<Node> getClosestNodes(byte[] targetId) {
        List<NodeEntry> closestEntries = getAllNodes();
        List<Node> closestNodes = new ArrayList<>();
        Collections.sort(closestEntries, new NodeEntryComparator(targetId));
        if (closestEntries.size() > KademliaOptions.BUCKET_SIZE) {
            closestEntries = closestEntries.subList(0, KademliaOptions.BUCKET_SIZE);
        }

        for (NodeEntry e : closestEntries) {
            closestNodes.add(e.getNode());
        }
        return closestNodes;
    }
}
