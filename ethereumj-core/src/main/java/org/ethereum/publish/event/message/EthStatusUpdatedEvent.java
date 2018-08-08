package org.ethereum.publish.event.message;

import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.server.Channel;

public class EthStatusUpdatedEvent extends AbstractMessageEvent<EthStatusUpdatedEvent.Data> {

    public static class Data extends AbstractMessageEvent.Data<StatusMessage> {

        public Data(Channel channel, StatusMessage message) {
            super(channel, message);
        }

    }

    public EthStatusUpdatedEvent(Channel channel, StatusMessage message) {
        super(new Data(channel, message));
    }
}
