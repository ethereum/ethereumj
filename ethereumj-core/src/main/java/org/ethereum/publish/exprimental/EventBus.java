package org.ethereum.publish.exprimental;

import org.ethereum.publish.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.Objects.isNull;

public class EventBus {

    private static final Logger log = LoggerFactory.getLogger("events");

    private final Map<Class<? extends Event>, EventPublisher> publisherByEvent = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(Thread::new);


    public <E extends Event<P>, P> void subscribe(Class<E> eventType, Consumer<P> handler) {
        publisherByEvent
                .computeIfAbsent(eventType, t -> new EventPublisher<>(eventType, executor).onCompleted(this::removePublisher))
                .subscribe(handler);
    }

    private void removePublisher(EventPublisher publisher) {
        publisherByEvent.remove(publisher.getEventType());
    }

    public <E extends Event<P>, P> void unsubscribe(Class<E> eventType, Consumer<P> handler) {

    }

    public boolean publish(Event event) {
        if (executor.isShutdown()) {
            log.warn("Cannot publish event: event bus is stopped.");
            return false;
        }

        EventPublisher eventPublisher = publisherByEvent.get(event.getClass());
        if (isNull(eventPublisher)) {
            return false;
        }

        return eventPublisher.publish(event);
    }

    public void shutdown() {
        executor.shutdown();
    }

}
