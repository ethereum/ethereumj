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

import org.ethereum.publish.event.Event;
import org.ethereum.publish.event.OneOffEvent;

public class Events {

    static class IntEvent extends Event<Integer> {
        IntEvent(Integer payload) {
            super(payload);
        }
    }

    static class LongEvent extends Event<Long> {
        LongEvent(long payload) {
            super(payload);
        }
    }

    static class StringEvent extends Event<String> {
        StringEvent(String payload) {
            super(payload);
        }
    }

    static class OneOffStringEvent extends StringEvent implements OneOffEvent {
        OneOffStringEvent(String payload) {
            super(payload);
        }
    }
}
