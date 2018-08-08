package org.ethereum.publish.event.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.server.Channel;

public class ReceivedMessageEvent extends AbstractMessageEvent<AbstractMessageEvent.Data<Message>> {

    public ReceivedMessageEvent(Channel channel, Message message) {
        super(new Data(channel, message));
    }
}
