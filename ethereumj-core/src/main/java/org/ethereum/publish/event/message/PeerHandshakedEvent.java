package org.ethereum.publish.event.message;

import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.server.Channel;

public class PeerHandshakedEvent extends AbstractMessageEvent<PeerHandshakedEvent.Data> {

    public static class Data extends AbstractMessageEvent.Data<HelloMessage> {

        public Data(Channel channel, HelloMessage message) {
            super(channel, message);
        }
    }


    public PeerHandshakedEvent(Channel channel, HelloMessage message) {
        super(new Data(channel, message));
    }
}
