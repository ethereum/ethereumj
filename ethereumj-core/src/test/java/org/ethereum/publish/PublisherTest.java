package org.ethereum.publish;

import org.ethereum.core.EventDispatchThread;
import org.ethereum.publish.event.Event;
import org.ethereum.publish.event.Single;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.ethereum.publish.Subscription.to;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PublisherTest {

    private class IntEvent extends Event<Integer> {
        IntEvent(Integer payload) {
            super(payload);
        }
    }

    private class LongEvent extends Event<Long> {
        LongEvent(long payload) {
            super(payload);
        }
    }

    private class StringEvent extends Event<String> {
        StringEvent(String payload) {
            super(payload);
        }
    }

    private class SingleStringEvent extends StringEvent implements Single {
        SingleStringEvent(String payload) {
            super(payload);
        }
    }


    @Test
    public void testDuplicateSubscription() {
        Subscription<IntEvent, Integer> subscription = Subscription.to(IntEvent.class, System.out::print);

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
                .subscribe(to(SingleStringEvent.class, strings::add))
                .publish(new SingleStringEvent(payload))
                .subscribersCount(SingleStringEvent.class);

        assertEquals(0, subscribersCount);
        assertTrue(strings.contains(payload));
    }

    @Test
    public void testConditionallySubscription() {

        AtomicLong expectedSum = new AtomicLong();
        AtomicInteger expectedEvenSum = new AtomicInteger();

        int[] numbers = IntStream.rangeClosed(1, 10).toArray();
        int sum = IntStream.of(numbers).sum();
        int evenSum = IntStream.of(numbers).filter(num -> isEven(num)).sum();

        Publisher publisher = createPublisher()
                .subscribe(to(LongEvent.class, expectedSum::getAndAdd))
                .subscribe(to(IntEvent.class, expectedEvenSum::getAndAdd)
                        .conditionally(PublisherTest::isEven));

        IntStream.of(numbers)
                .forEach(num -> publisher
                        .publish(new LongEvent(num))
                        .publish(new IntEvent(num)));

        assertEquals(sum, expectedSum.get());
        assertEquals(evenSum, expectedEvenSum.get());
    }

    @Test
    public void testPublishing() {

        AtomicLong longEvenSum = new AtomicLong();
        AtomicInteger firstIntSum = new AtomicInteger();
        AtomicInteger secondIntSum = new AtomicInteger();
        List<String> expectedStrings = new ArrayList<>();

        List<String> strings = Arrays.asList("some event", "another event", "incredible event");
        int[] numbers = IntStream.rangeClosed(1, 10).toArray();
        int sum = IntStream.of(numbers).sum();
        int evenSum = IntStream.of(numbers).filter(num -> isEven(num)).sum();


        Publisher publisher = createPublisher()
                .subscribe(to(IntEvent.class, firstIntSum::getAndAdd))
                .subscribe(to(IntEvent.class, secondIntSum::getAndAdd))
                .subscribe(to(StringEvent.class, expectedStrings::add))
                .subscribe(to(LongEvent.class, longEvenSum::getAndAdd)
                        .conditionally(PublisherTest::isEven));

        IntStream.of(numbers)
                .forEach(num -> publisher
                        .publish(new IntEvent(num))
                        .publish(new LongEvent(num)));

        strings.stream()
                .forEach(str -> publisher
                        .publish(new StringEvent(str)));

        assertEquals(sum, firstIntSum.get());
        assertEquals(sum, secondIntSum.get());
        assertEquals(evenSum, longEvenSum.intValue());
        assertEquals(strings.size(), expectedStrings.stream()
                .filter(strings::contains)
                .count());
    }

    private static Publisher createPublisher() {
        return new Publisher(EventDispatchThread.getDefault());
    }

    private static boolean isEven(long number) {
        return number % 2 == 0;
    }
}