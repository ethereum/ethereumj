package org.ethereum.net.rlpx.discover.table;

import java.util.Comparator;

/**
 * Created by kest on 5/26/15.
 */
public class DistanceComparator implements Comparator<NodeEntry>  {
    byte[] targetId;

    DistanceComparator(byte[] targetId) {
        this.targetId = targetId;
    }

    @Override
    public int compare(NodeEntry e1, NodeEntry e2) {
        int d1 = NodeEntry.distance(targetId, e1.getNode().getId());
        int d2 = NodeEntry.distance(targetId, e2.getNode().getId());

        if (d1 > d2) {
            return 1;
        } else if (d1 < d2) {
            return -1;
        } else {
            return 0;
        }
    }
}
