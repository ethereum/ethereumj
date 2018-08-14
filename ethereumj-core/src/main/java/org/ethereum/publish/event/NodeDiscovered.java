package org.ethereum.publish.event;

import org.ethereum.net.rlpx.Node;

public class NodeDiscovered extends Event<Node> {
    public NodeDiscovered(Node payload) {
        super(payload);
    }
}
