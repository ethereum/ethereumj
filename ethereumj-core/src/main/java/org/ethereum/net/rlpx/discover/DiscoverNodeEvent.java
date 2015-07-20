package org.ethereum.net.rlpx.discover;

/**
 * Created by Anton Nashatyrev on 17.07.2015.
 */
public class DiscoverNodeEvent {
    NodeHandler node;

    public DiscoverNodeEvent(NodeHandler node) {
        this.node = node;
    }

    public NodeHandler getNode() {
        return node;
    }
}
