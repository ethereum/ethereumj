package org.ethereum.publish.event.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.server.Channel;

public class MessageReceived extends AbstractMessageEvent<AbstractMessageEvent.Data<Message>> {

    public MessageReceived(Channel channel, Message message) {
        super(new Data(channel, message));
    }
}
