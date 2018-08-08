package org.ethereum.publish.event;

import org.ethereum.net.server.Channel;

public class PeerAddedToSyncPoolEvent extends Event<Channel> {

    public PeerAddedToSyncPoolEvent(Channel channel) {
        super(channel);
    }
}
