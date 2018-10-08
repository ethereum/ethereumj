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
package org.ethereum.publish;

import org.apache.commons.lang3.text.StrBuilder;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.facade.Ethereum;
import org.ethereum.publish.event.Event;
import org.ethereum.publish.event.OneOffEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.ethereum.publish.Subscription.to;

/**
 * Event publisher that uses pub/sub model to deliver event messages.<br>
 * Uses {@link EventDispatchThread} as task executor, and subscribers notifying in parallel thread depends on
 * {@link EventDispatchThread} implementation passed via constructor.<br>
 * <p>
 * Usage examples:
 * <pre>
 * {@code
 *
 *     // Publisher creating and subscribing
 *     EventDispatchThread edt = new EventDispatchThread();
 *     Publisher publisher = new Publisher(edt)
 *             .subscribe(to(SingleEvent.class, singleEventPayload -> handleOnce(singleEventPayload)))
 *             .subscribe(to(SomeEvent.class, someEventPayload -> doSmthWith(someEventPayload)))
 *             .subscribe(to(SomeEvent.class, someEventPayload -> doSmthWithElse(someEventPayload);}))
 *             .subscribe(to(AnotherEvent.class, SubscriberClass::handleAnotherEventPayload))
 *             .subscribe(to(OneMoreEvent.class, subscriberInstance::processOneMoreEventPayload)
 *                     .conditionally(oneMoreEventPayload -> shouldHandleOrNot(oneMoreEventPayload)));
 *
 *     // Publishing events
 *     publisher
 *             .publish(new OneMoreEvent())    // will fire processOneMoreEventPayload if shouldHandleOrNot return true
 *             .publish(new SomeEvent())       // will fire doSmthWith and doSmthWithElse with the same payload argument
 *             .publish(new UnknownEvent())    // do nothing, because there is no subscription for this event type
 *             .publish(new SingleEvent())     // will fire handleOnce and unsubscribe all subscribers of this event type
 *             .publish(new SingleEvent());    // do nothing, because there is no subscription for this event type
 * }
 * </p>
 *
 * @see Subscription
 * @see Event
 * @see OneOffEvent
 *
 * @author Eugene Shevchenko
 */
public class Publisher {

    private static final Logger log = LoggerFactory.getLogger("events");

    private class Command implements Runnable {
        private final Subscription subscription;
        private final Event event;

        private Command(Subscription subscription, Event event) {
            this.subscription = subscription;
            this.event = event;
        }

        @Override
        public void run() {
            try {
                subscription.handle(event);
            } finally {
                if (subscription.needUnsubscribeAfter(event)) {
                    Publisher.this.unsubscribe(subscription);
                }
            }
        }

        @Override
        public String toString() {
            return event.toString();
        }
    }

    private final Executor executor;
    private final Map<Class<? extends Event>, List<Subscription>> subscriptionsByEvent = new ConcurrentHashMap<>();

    public Publisher(Executor executor) {
        this.executor = executor;
    }

    /**
     * Publishes specified event for all its subscribers.<br>
     * Concurrent execution depends on implementation of nested {@link EventDispatchThread}.
     *
     * @param event event to publish;
     * @return current {@link Publisher} instance to support fluent API.
     */
    public Publisher publish(Event event) {
        List<Subscription> subscriptions = subscriptionsByEvent.getOrDefault(event.getClass(), emptyList());
        subscriptions.stream()
                .filter(subscription -> subscription.matches(event))
                .map(subscription -> new Command(subscription, event))
                .forEach(executor::execute);

        subscriptions.stream()
                .filter(subscription -> subscription.needUnsubscribeAfter(event))
                .forEach(this::unsubscribe);

        if (event instanceof OneOffEvent) {
            subscriptionsByEvent.remove(event.getClass());
        }

        return this;
    }

    /**
     * Adds specified {@link Subscription} to publisher.<br>
     * Do nothing if specified subscription already added.
     *
     * @param subscription
     * @param <E>          {@link Event} subclass which describes specific event type;
     * @param <P>          payload type of specified event type;
     * @return current {@link Publisher} instance to support fluent API.
     */
    public <E extends Event<P>, P> Publisher subscribe(Subscription<E, P> subscription) {
        List<Subscription> subscriptions = subscriptionsByEvent.computeIfAbsent(subscription.getEventType(), t -> new CopyOnWriteArrayList<>());
        if (subscriptions.contains(subscription)) {
            log.warn("Specified subscription already exists {}.", subscription.getEventType().getSimpleName());
        } else {
            subscriptions.add(subscription);
            log.debug("{} added to publisher.", subscription);
        }
        return this;
    }

    /**
     * Subscribes client's handler to specific Ethereum event. Does the same thing as {@link #subscribe(Subscription)},
     * in more convenient way, but you don't have access to created {@link Subscription} instance.
     * <p>
     * Supported events list you can find here {@link org.ethereum.publish.event.Events.Type}
     *
     * @param type    event type to subscribe;
     * @param handler event handler;
     * @param <T>     event payload which will be passed to handler;
     * @return {@link Ethereum} instance to support fluent API.
     */
    public <T> Publisher subscribe(Class<? extends Event<T>> type, Consumer<T> handler){
        return subscribe(to(type, handler));
    }

    /**
     * More advanced version of {@link #subscribe(Class, Consumer)}
     * where besides of event's payload to client's handler passes subscription's {@link org.ethereum.publish.Subscription.LifeCycle}.
     * <p>
     * Supported events list you can find here {@link org.ethereum.publish.event.Events.Type}
     *
     * @param type    event type to subscribe;
     * @param handler extended event handler;
     * @param <T>     event payload which will be passed to handler;
     * @return {@link Publisher} instance to support fluent API.
     */
    public <T> Publisher subscribe(Class<? extends Event<T>> type, BiConsumer<T, Subscription.LifeCycle> handler) {
        return subscribe(to(type, handler));
    }


    /**
     * Removes specified {@link Subscription} from publisher.
     *
     * @param subscription subscription to remove;
     * @return current {@link Publisher} instance to support fluent API.
     */
    public Publisher unsubscribe(Subscription subscription) {
        List<Subscription> subscriptions = subscriptionsByEvent.get(subscription.getEventType());
        if (nonNull(subscriptions)) {
            subscriptions.remove(subscription);
            log.debug("{} removed from publisher.", subscription);
            if (subscriptions.isEmpty()) {
                subscriptionsByEvent.remove(subscription.getEventType());
            }
        }

        return this;
    }

    /**
     * Calculates specific event type {@link Subscription}s amount added to current {@link Publisher}.
     *
     * @param eventType event type to filter {@link Subscription}s;
     * @return specified event type {@link Subscription}s count.
     */
    public int subscribersCount(Class<? extends Event> eventType) {
        return subscriptionsByEvent.getOrDefault(eventType, emptyList()).size();
    }

    /**
     * Calculates total amount {@link Subscription}s added to current {@link Publisher}.
     *
     * @return all subscribers total count.
     */
    public int subscribersCount() {
        return subscriptionsByEvent.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Gets events set subscribed via current publisher.
     *
     * @return all events which have subscribers.
     */
    public Set<Class<? extends Event>> events() {
        return subscriptionsByEvent.keySet();
    }

    @Override
    public String toString() {
        StrBuilder builder = new StrBuilder("Publisher info:\n");
        if (subscriptionsByEvent.isEmpty()) {
            builder.append("\tempty.\n");
        } else {
            subscriptionsByEvent.forEach((type, subscriptions) -> builder
                    .append("\t- ")
                    .append(type.getSimpleName())
                    .append(": ")
                    .append(subscriptions.size())
                    .append(" subscription(s);\n"));
        }

        return builder.toString();
    }
}
