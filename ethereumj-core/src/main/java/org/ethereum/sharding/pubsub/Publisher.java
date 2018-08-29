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
package org.ethereum.sharding.pubsub;

import org.ethereum.core.EventDispatchThread;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Primitive pub/sub implementation.
 *
 * <p>
 *      Uses {@link EventDispatchThread} to process subscriptions
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class Publisher {

    EventDispatchThread eventDispatchThread;

    protected Map<Class<? extends Event>, List<Consumer>> subscriptionMap = new ConcurrentHashMap<>();

    public Publisher(EventDispatchThread eventDispatchThread) {
        this.eventDispatchThread = eventDispatchThread;
    }

    /**
     * Subscribes to specified event.
     *
     * @param type event type
     * @param consumer consumer that is called when subscribed event is triggered
     */
    public <T> void subscribe(Class<? extends Event<T>> type, Consumer<T> consumer) {
        List<Consumer> list = subscriptionMap.computeIfAbsent(type, event -> new CopyOnWriteArrayList<>());
        list.add(consumer);
    }

    /**
     * Triggers event subscriptions.
     */
    public void publish(final Event event) {
        List<Consumer> subs = subscriptionMap.getOrDefault(event.getClass(), Collections.emptyList());
        subs.forEach(s -> eventDispatchThread.invokeLater(() -> s.accept(event.getData())));
    }
}
