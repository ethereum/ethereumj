package org.ethereum.net.rlpx.discover;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kest on 5/25/15.
 */
public class NodeBucket {

    private final int depth;
    private LinkedList<NodeEntry> nodes = new LinkedList<>();

    NodeBucket(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void addNode(NodeEntry e) {
        if (!nodes.contains(e)) {
            if (nodes.size() >= KademliaOptions.BUCKET_SIZE) {
                nodes.removeFirst();
                nodes.addLast(e);
            } else {
                nodes.addLast(e);
            }
        }
    }

    public void dropNode(NodeEntry entry) {
        for (NodeEntry e : nodes) {
            if (e.getId().equals(entry.getId())) {
                nodes.remove(e);
                break;
            }
        }
    }

    public int getNodesCount() {
        return nodes.size();
    }

    public List<NodeEntry> getNodes() {
        List<NodeEntry> nodes = new ArrayList<>();
        for (NodeEntry e : this.nodes) {
            nodes.add(e);
        }
        return nodes;
    }
}
