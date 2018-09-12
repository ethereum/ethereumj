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
