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
package org.ethereum.publish.event;

/**
 * Base class for all event types which can be used with {@link org.ethereum.publish.Publisher}.
 *
 * @author Eugene Shevchenko
 */
public abstract class Event<T> {

    private final T payload;
    private final long timestamp;

    public Event(T payload) {
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return event's payload object.
     */
    public T getPayload() {
        return payload;
    }

    /**
     * @return timestamp of event creation.
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " emitted at " + getTimestamp();
    }
}
