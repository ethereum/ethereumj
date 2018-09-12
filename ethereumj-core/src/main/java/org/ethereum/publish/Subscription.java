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

import org.ethereum.publish.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Abstraction that holds together event type, event processor (a.k.a consumer) and optional conditions that resolves
 * event processing and auto-unsubscribe.
 *
 * @author Eugene Shevchenko
 */
public class Subscription<E extends Event<D>, D> {

    private final static Logger log = LoggerFactory.getLogger("event");

    public static class LifeCycle {
        private final Subscription subscription;

        private LifeCycle(Subscription subscription) {
            this.subscription = subscription;
        }

        public void unsubscribe() {
            subscription.unsubscribeAfter(data -> true);
        }
    }


    private final Class<E> eventType;
    private final BiConsumer<D, LifeCycle> biConsumer;
    private final LifeCycle lifeCycle;

    private Function<D, Boolean> handleCondition;
    private Function<D, Boolean> unsubscribeCondition;

    Subscription(Class<E> eventType, BiConsumer<D, LifeCycle> biConsumer) {
        this.eventType = eventType;
        this.lifeCycle = new LifeCycle(this);
        this.biConsumer = biConsumer;
    }

    /**
     * Gets event type that current {@link Subscription} processes.
     *
     * @return type of event.
     */
    public Class<E> getEventType() {
        return eventType;
    }

    /**
     * Optionally adds conditional clause that indicates should consumes tested event or not.
     *
     * @param condition function that resolves sequential event consumption.
     * @return current {@link Subscription} instance to support fluent API.
     */
    public Subscription<E, D> conditionally(Function<D, Boolean> condition) {
        this.handleCondition = condition;
        return this;
    }

    /**
     * Optionally adds unsubscribe condition after event consuming.
     *
     * @param condition function that resolves unsubscribing after event consumption.
     * @return current {@link Subscription} instance to support fluent API.
     */
    public Subscription<E, D> unsubscribeAfter(Function<D, Boolean> condition) {
        this.unsubscribeCondition = condition;
        return this;
    }

    /**
     * Optionally adds {@link #conditionally(Function)} and {@link #unsubscribeAfter(Function)} clauses with the same
     * condition. It helps achieve specific behavior, when subscriber consumes and then unsubscribe from event source.
     *
     * @param condition
     * @return current {@link Subscription} instance to support fluent API.
     */
    public Subscription<E, D> oneOff(Function<D, Boolean> condition) {
        return this
                .conditionally(condition)
                .unsubscribeAfter(condition);
    }

    /**
     * Tests specified event whether it should be consumed.
     *
     * @param event event to test;
     * @return <code>true</code> if event should be consumed, <code>false</code> otherwise.
     */
    boolean matches(E event) {
        return isNull(handleCondition) || handleCondition.apply(event.getPayload());
    }

    /**
     * Safely (catches all exceptions with logging only) consumes specified event.
     *
     * @param event event to consume.
     */
    void handle(E event) {
        try {
            handlePayload(event.getPayload());
        } catch (Throwable e) {
            log.error(eventType.getSimpleName() + " handling error: ", e);
        }
    }

    protected void handlePayload(D payload) {
        biConsumer.accept(payload, lifeCycle);
    }

    /**
     * Tests whether publisher should remove current {@link Subscription} after specified event handling.
     *
     * @param event event to test;
     * @return <code>true</code> if after event consumption {@link Subscription} should be unsubscribed, <code>false</code> otherwise.
     */
    boolean needUnsubscribeAfter(E event) {
        return nonNull(unsubscribeCondition) && unsubscribeCondition.apply(event.getPayload());
    }

    public static <E extends Event<D>, D> Subscription<E, D> to(Class<E> eventType, BiConsumer<D, LifeCycle> biConsumer) {
        return new Subscription<>(eventType, biConsumer);
    }

    /**
     * Short static alias for {@link Subscription} constructor.
     *
     * @param eventType event type to process;
     * @param consumer  callback that consumes event's payload;
     * @param <E>       event type that should be process;
     * @param <D>       payload's type of specified event type;
     * @return new {@link Subscription} instance.
     */
    public static <E extends Event<D>, D> Subscription<E, D> to(Class<E> eventType, Consumer<D> consumer) {
        return new Subscription<>(eventType, (payload, lifeCycle) -> consumer.accept(payload));
    }

    @Override
    public String toString() {
        return eventType.getSimpleName() + " subscription";
    }
}
