package org.ethereum.publish.event;

public class TraceEvent extends Event<String> {

    public TraceEvent(String trace) {
        super(trace);
    }
}
