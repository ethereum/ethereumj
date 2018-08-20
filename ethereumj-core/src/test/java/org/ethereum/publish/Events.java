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
