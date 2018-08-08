package org.ethereum.publish.event;

import org.ethereum.net.rlpx.Node;

public class NodeDiscoveredEvent extends Event<Node> {
    public NodeDiscoveredEvent(Node payload) {
        super(payload);
    }
}
