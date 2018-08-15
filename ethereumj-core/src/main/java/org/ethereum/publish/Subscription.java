package org.ethereum.publish;

import org.ethereum.publish.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Class<E> eventType;
    private final Consumer<D> consumer;
    private Function<D, Boolean> handleCondition;
    private Function<D, Boolean> unsubscribeCondition;

    public Subscription(Class<E> eventType, Consumer<D> consumer) {
        this.eventType = eventType;
        this.consumer = consumer;
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
            consumer.accept(event.getPayload());
        } catch (Throwable e) {
            log.error(eventType.getSimpleName() + " handling error: ", e);
        }
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
        return new Subscription<>(eventType, consumer);
    }
}
