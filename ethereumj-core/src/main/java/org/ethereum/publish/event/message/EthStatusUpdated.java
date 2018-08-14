package org.ethereum.publish.event.message;

import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.server.Channel;

public class EthStatusUpdated extends AbstractMessageEvent<EthStatusUpdated.Data> {

    public static class Data extends AbstractMessageEvent.Data<StatusMessage> {

        public Data(Channel channel, StatusMessage message) {
            super(channel, message);
        }

    }

    public EthStatusUpdated(Channel channel, StatusMessage message) {
        super(new Data(channel, message));
    }
}
