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

import org.ethereum.core.EventDispatchThread;
import org.ethereum.util.RandomGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.ethereum.publish.Subscription.to;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PublisherTest {

    @Test
    public void testDuplicateSubscription() {
        Subscription<Events.IntEvent, Integer> subscription = Subscription.to(Events.IntEvent.class, System.out::print);

        int subscribersCount = createPublisher()
                .subscribe(subscription)
                .subscribe(subscription)
                .subscribersCount(subscription.getEventType());

        assertEquals(1, subscribersCount);
    }

    @Test
    public void testSingleEvent() {
        final String payload = "desired event";
        final List<String> strings = new ArrayList<>();

        int subscribersCount = createPublisher()
                .subscribe(Events.OneOffStringEvent.class, s -> strings.add(s))
                .publish(new Events.OneOffStringEvent(payload))
                .subscribersCount(Events.OneOffStringEvent.class);

        assertEquals(0, subscribersCount);
        assertTrue(strings.contains(payload));
    }

    @Test
    public void testConditionallySubscription() {

        AtomicLong actualSum = new AtomicLong();
        AtomicInteger actualEvenSum = new AtomicInteger();

        int[] numbers = IntStream.rangeClosed(1, 10).toArray();
        int sum = IntStream.of(numbers).sum();
        int evenSum = IntStream.of(numbers).filter(num -> isEven(num)).sum();

        Publisher publisher = createPublisher()
                .subscribe(to(Events.LongEvent.class, actualSum::getAndAdd))
                .subscribe(to(Events.IntEvent.class, actualEvenSum::getAndAdd)
                        .conditionally(PublisherTest::isEven));

        IntStream.of(numbers)
                .forEach(num -> publisher
                        .publish(new Events.LongEvent(num))
                        .publish(new Events.IntEvent(num)));

        assertEquals(sum, actualSum.get());
        assertEquals(evenSum, actualEvenSum.get());
    }

    @Test
    public void testUnsubscribeAfter() {
        AtomicInteger actualSum = new AtomicInteger();

        int limit = 10;
        int[] numbers = IntStream.rangeClosed(1, limit).toArray();
        int sum = IntStream.of(numbers).sum();

        Publisher publisher = createPublisher()
                .subscribe(to(Events.IntEvent.class, actualSum::addAndGet)
                        .unsubscribeAfter(num -> num == limit));

        IntStream.rangeClosed(1, limit * 2)
                .mapToObj(Events.IntEvent::new)
                .forEach(publisher::publish);

        assertEquals(sum, actualSum.get());
    }

    @Test
    public void testOneOffSubscription() {
        AtomicInteger actual = new AtomicInteger();
        final int expected = 5;

        Publisher publisher = createPublisher()
                .subscribe(to(Events.IntEvent.class, actual::set)
                        .oneOff(num -> num == expected));

        IntStream.rangeClosed(1, 10)
                .mapToObj(Events.IntEvent::new)
                .forEach(publisher::publish);

        assertEquals(expected, actual.get());
    }

    @Test
    public void testLifeCycleSubscription() {
        AtomicInteger actual = new AtomicInteger();
        final int expected = 5;

        Publisher publisher = createPublisher()
                .subscribe(to(Events.IntEvent.class, (num, lc) -> {
                    if (num == expected) {
                        actual.set(num);
                        lc.unsubscribe();
                    }
                }));

        IntStream.rangeClosed(1, 10)
                .mapToObj(Events.IntEvent::new)
                .forEach(publisher::publish);

        assertEquals(expected, actual.get());
    }

    @Test
    public void testPublishing() {

        AtomicLong longEvenSum = new AtomicLong();
        AtomicInteger firstIntSum = new AtomicInteger();
        AtomicInteger secondIntSum = new AtomicInteger();
        List<String> expectedStrings = new ArrayList<>();

        List<String> strings = asList("some event", "another event", "incredible event");
        int[] numbers = IntStream.rangeClosed(1, 10).toArray();
        int sum = IntStream.of(numbers).sum();
        int evenSum = IntStream.of(numbers).filter(num -> isEven(num)).sum();


        Publisher publisher = createPublisher()
                .subscribe(to(Events.IntEvent.class, firstIntSum::getAndAdd))
                .subscribe(to(Events.IntEvent.class, secondIntSum::getAndAdd))
                .subscribe(to(Events.StringEvent.class, s -> expectedStrings.add(s)))
                .subscribe(to(Events.LongEvent.class, longEvenSum::getAndAdd)
                        .conditionally(PublisherTest::isEven));

        IntStream.of(numbers)
                .forEach(num -> publisher
                        .publish(new Events.IntEvent(num))
                        .publish(new Events.LongEvent(num)));

        strings.stream()
                .forEach(str -> publisher
                        .publish(new Events.StringEvent(str)));

        assertEquals(sum, firstIntSum.get());
        assertEquals(sum, secondIntSum.get());
        assertEquals(evenSum, longEvenSum.intValue());
        assertEquals(strings.size(), expectedStrings.stream()
                .filter(strings::contains)
                .count());
    }

    @Test
    public void testHandlingWithException() {
        AtomicInteger actual = new AtomicInteger();
        int expected = 5;

        createPublisher()
                .subscribe(to(Events.IntEvent.class, num -> {
                    throw new RuntimeException();
                }))
                .subscribe(to(Events.IntEvent.class, num -> actual.set(num)))
                .publish(new Events.IntEvent(expected));

        assertEquals(expected, actual.get());
    }

    @Test
    public void testConcurrentAccess() {
        BlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<>();
        ExecutorService executor = new ThreadPoolExecutor(10, 10, 0L,
                TimeUnit.MILLISECONDS, executorQueue);

        Random random = new Random();
        final String[] strings1 = {"one", "two", "three", "thousand", "one hundred", "zero"};
        final int limit = 1000;

        EventGenerator eGen = new EventGenerator(random)
                .withIntEvent(limit)
                .withLongEvent(limit)
                .withStringEvent(strings1)
                .withOneOffStringEvent(strings1);


        RandomGenerator<Subscription> sGen = new RandomGenerator<Subscription>(random)
                .addGenFunction(r -> to(Events.OneOffStringEvent.class, s -> sleepSilent(r.nextInt(10))))
                .addGenFunction(r -> to(Events.StringEvent.class, s -> sleepSilent(r.nextInt(10)))
                        .conditionally(s -> s.startsWith("t"))
                        .unsubscribeAfter(s -> s.startsWith("z")))
                .addGenFunction(r -> to(Events.IntEvent.class, i -> sleepSilent(r.nextInt(10)))
                        .unsubscribeAfter(i -> i < r.nextInt(limit)))
                .addGenFunction(r -> to(Events.IntEvent.class, (i, lifeCycle) -> {
                    sleepSilent(r.nextInt(10));
                    if (i < r.nextInt(limit)) {
                        lifeCycle.unsubscribe();
                    }
                }))
                .addGenFunction(r -> to(Events.LongEvent.class, i -> sleepSilent(r.nextInt(10)))
                        .oneOff(i -> i < r.nextInt(limit)));


        try {
            Publisher publisher = new Publisher(executor);
            // prints publisher state info
            executor.execute(() -> {
                while (true) {
                    System.out.println(publisher);
                    sleepSilent(300);
                }
            });

            AtomicBoolean running = new AtomicBoolean(true);
            try {
                // generates events
                executor.execute(() -> {
                    while (running.get()) {
                        publisher.publish(eGen.genNext());
                        if (executorQueue.size() > limit) {
                            sleepSilent(100);
                        }
                    }
                });
                // generates subscriptions
                executor.execute(() -> {
                    while (running.get()) {
                        publisher.subscribe(sGen.genNext());
                        if (publisher.subscribersCount() > limit) {
                            sleepSilent(100);
                        }
                    }
                });

                sleepSilent(5000);
            } finally {
                running.set(false);
            }

            while (!executorQueue.isEmpty()) {
                sleepSilent(100);
            }

        } finally {
            executor.shutdown();
        }
    }

    private static void sleepSilent(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Publisher createPublisher() {
        return new Publisher(EventDispatchThread.getDefault());
    }

    private static boolean isEven(long number) {
        return number % 2 == 0;
    }
}