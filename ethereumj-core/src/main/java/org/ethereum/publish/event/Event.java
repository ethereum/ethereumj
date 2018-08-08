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
