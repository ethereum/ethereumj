package org.ethereum.publish;

import org.ethereum.core.EventDispatchThread;
import org.ethereum.publish.event.Event;
import org.ethereum.publish.event.OneOffEvent;
import org.ethereum.publish.event.SignalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

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
 * @see SignalEvent
 * @see OneOffEvent
 *
 * @author Eugene Shevchenko
 */
public class Publisher {

    private static final Logger log = LoggerFactory.getLogger("events");

    private class Command implements Runnable {
        private final List<Subscription> subscriptions;
        private final Event event;

        private Command(List<Subscription> subscriptions, Event event) {
            this.subscriptions = subscriptions;
            this.event = event;
        }

        @Override
        public void run() {
            subscriptions.forEach(subscription -> subscription.handle(event));
        }

        @Override
        public String toString() {
            return format("%s: consumed by %d subscriber(s).", event, subscriptions.size());
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
        if (!subscriptions.isEmpty()) {

            List<Subscription> toHandle = subscriptions.stream()
                    .filter(subscription -> subscription.matches(event))
                    .collect(toList());

            subscriptions.stream()
                    .filter(subscription -> subscription.needUnsubscribeAfter(event))
                    .forEach(this::unsubscribe);

            if (event instanceof OneOffEvent) {
                subscriptionsByEvent.remove(event.getClass());
            }


            if (!toHandle.isEmpty()) {
                executor.execute(new Command(toHandle, event));
            }
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
        }
        return this;
    }

    /**
     * Creates {@link Subscription} from specified parameters and adds it to current publisher.
     *
     * @param eventType even's type to subscribe;
     * @param handler   callback that will be invoked after event will be published;
     * @param <E>       event's type;
     * @param <P>       payload of specified event;
     * @return created {@link Subscription} instance.
     */
    public <E extends Event<P>, P> Subscription<E, P> subscribe(Class<E> eventType, Consumer<P> handler) {
        Subscription<E, P> subscription = new Subscription<>(eventType, handler);
        subscribe(subscription);
        return subscription;
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
}
