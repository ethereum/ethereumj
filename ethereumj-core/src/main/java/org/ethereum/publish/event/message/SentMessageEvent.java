package org.ethereum.publish.event.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.server.Channel;

public class SentMessageEvent extends AbstractMessageEvent<AbstractMessageEvent.Data<Message>> {

    public SentMessageEvent(Channel channel, Message message) {
        super(new Data(channel, message));
    }
}
