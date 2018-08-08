package org.ethereum.publish.event.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.server.Channel;
import org.ethereum.publish.event.Event;

/**
 * Base class for any message events (received, sent, etc.)
 *
 * @author Eugene Shevchenko
 */
public abstract class AbstractMessageEvent<P extends AbstractMessageEvent.Data> extends Event<P> {

    public static class Data<M extends Message> {
        public final Channel channel;
        public final M message;

        public Data(Channel channel, M message) {
            this.channel = channel;
            this.message = message;
        }

        public Channel getChannel() {
            return channel;
        }

        public M getMessage() {
            return message;
        }
    }

    public AbstractMessageEvent(P payload) {
        super(payload);
    }
}
