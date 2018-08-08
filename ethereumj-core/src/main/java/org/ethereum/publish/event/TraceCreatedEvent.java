package org.ethereum.publish.event;

public class TraceCreatedEvent extends Event<String> {

    public TraceCreatedEvent(String trace) {
        super(trace);
    }
}
