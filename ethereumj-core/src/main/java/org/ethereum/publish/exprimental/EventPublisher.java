package org.ethereum.publish.exprimental;

import org.ethereum.publish.event.Event;
import org.ethereum.publish.event.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

public class EventPublisher<E extends Event<P>, P> {

    public static final Logger log = LoggerFactory.getLogger("event");

    private final Class<E> eventType;
    private final BlockingQueue<E> eventsQueue = new LinkedBlockingQueue<>();
    private final List<Consumer<P>> consumers = new CopyOnWriteArrayList<>();
    private final ExecutorService executor;

    private final Thread listener;
    private Consumer<EventPublisher<E, P>> completedCallback;

    public EventPublisher(Class<E> eventType, ExecutorService executor) {
        this.eventType = eventType;
        this.executor = executor;
        this.listener = new Thread(this::listenEvents, "publisher-" + eventType.getSimpleName());
    }

    public EventPublisher<E, P> onCompleted(Consumer<EventPublisher<E, P>> callback) {
        this.completedCallback = callback;
        return this;
    }

    public Class<E> getEventType() {
        return eventType;
    }

    public boolean publish(E event) {
        return eventsQueue.offer(event);
    }

    public void subscribe(Consumer<P> consumer) {
        boolean isFirstConsumer = consumers.isEmpty();
        consumers.add(consumer);
        if (isFirstConsumer && !listener.isAlive()) {
            listener.start();
        }
    }

    public void unsubscribe(Consumer<P> consumer) {
        consumers.remove(consumer);
        if (consumers.isEmpty() && eventsQueue.isEmpty()) {
            listener.interrupt();
        }
    }

    private void listenEvents() {
        while (!(consumers.isEmpty() || executor.isShutdown())) {

            try {
                E event = eventsQueue.take();


                notify(event);

                if (event instanceof Single) {
                    consumers.clear();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (nonNull(completedCallback)) {
            completedCallback.accept(this);
        }
    }

    private void notify(Consumer<P> consumer, E event) {
        try {
            consumer.accept(event.getPayload());
        } catch (Throwable err) {
            log.error("Event handling error: ", err);
        }
    }

    private void notifyParallel(List<Consumer<P>> consumers, E event) throws InterruptedException, ExecutionException {
        CompletableFuture[] futures = consumers.stream()
                .map(consumer -> CompletableFuture.runAsync(() -> notify(consumer, event), executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).get();
    }

    private void notify(E event) throws InterruptedException, ExecutionException {
        LocalDateTime start = now();
        try {
            if (consumers.size() == 1) {
                notify(consumers.get(0), event);
            } else {
                notifyParallel(consumers, event);
            }
        } finally {
            log.debug("{} subscriber(s) processing took {} ms.", consumers.size(), Duration.between(start, now()).toMillis());
        }
    }
}
