/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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
