package org.ethereum.publish.event;

import org.ethereum.net.server.Channel;

public class PeerAddedToSyncPool extends Event<Channel> {

    public PeerAddedToSyncPool(Channel channel) {
        super(channel);
    }
}
