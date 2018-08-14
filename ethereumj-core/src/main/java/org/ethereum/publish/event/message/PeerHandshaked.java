package org.ethereum.publish.event.message;

import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.server.Channel;

public class PeerHandshaked extends AbstractMessageEvent<PeerHandshaked.Data> {

    public static class Data extends AbstractMessageEvent.Data<HelloMessage> {

        public Data(Channel channel, HelloMessage message) {
            super(channel, message);
        }
    }


    public PeerHandshaked(Channel channel, HelloMessage message) {
        super(new Data(channel, message));
    }
}
