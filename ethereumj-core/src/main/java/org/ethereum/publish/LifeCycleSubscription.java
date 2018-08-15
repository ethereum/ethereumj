package org.ethereum.publish;

import org.ethereum.publish.event.Event;

import java.util.function.BiConsumer;

/**
 * Abstraction that holds together event type, event processor (a.k.a consumer) and optional conditions that resolves
 * event processing and auto-unsubscribe.
 *
 * @author Eugene Shevchenko
 */
public class LifeCycleSubscription<E extends Event<D>, D> extends Subscription<E, D> {

    public static class LifeCycle {
        private final LifeCycleSubscription subscription;

        private LifeCycle(LifeCycleSubscription subscription) {
            this.subscription = subscription;
        }

        public void unsubscribe() {
            subscription.unsubscribeAfter(data -> true);
        }
    }

    private final BiConsumer<D, LifeCycle> biConsumer;
    private final LifeCycle lifeCycle;

    LifeCycleSubscription(Class<E> eventType, BiConsumer<D, LifeCycle> biConsumer) {
        super(eventType, null);
        this.biConsumer = biConsumer;
        this.lifeCycle = new LifeCycle(this);
    }

    @Override
    protected void handlePayload(D payload) {
        biConsumer.accept(payload, lifeCycle);
    }

    /**
     * Short static alias for {@link LifeCycleSubscription} constructor.
     *
     * @param eventType  event type to process;
     * @param biConsumer callback that consumes event's payload;
     * @param <E>        event type that should be process;
     * @param <D>        payload's type of specified event type;
     * @return new {@link LifeCycleSubscription} instance.
     */
    public static <E extends Event<D>, D> LifeCycleSubscription<E, D> to(Class<E> eventType, BiConsumer<D, LifeCycle> biConsumer) {
        return new LifeCycleSubscription<>(eventType, biConsumer);
    }
}
