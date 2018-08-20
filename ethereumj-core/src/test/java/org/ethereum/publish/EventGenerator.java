package org.ethereum.publish;

import com.google.common.base.Function;
import org.ethereum.publish.event.Event;
import org.ethereum.util.RandomGenerator;

import java.util.Random;

public class EventGenerator extends RandomGenerator<Event> {

    public EventGenerator(Random random) {
        super(random);
    }

    private EventGenerator withGenFunction(Function<Random, Event> function) {
        return (EventGenerator) addGenFunction(function);
    }

    public EventGenerator withIntEvent(int bound) {
        return withGenFunction(r -> new Events.IntEvent(r.nextInt(bound)));
    }

    public EventGenerator withLongEvent(int bound) {
        return withGenFunction(r -> new Events.LongEvent(r.nextInt(bound)));
    }

    public EventGenerator withStringEvent(String... strings) {
        return withGenFunction(r -> new Events.StringEvent(randomFrom(strings)));
    }

    public EventGenerator withOneOffStringEvent(String... strings) {
        return withGenFunction(random -> new Events.OneOffStringEvent(randomFrom(strings)));
    }
}
