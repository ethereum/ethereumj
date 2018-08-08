package org.ethereum.publish.event;

/**
 * Base class for signal events (without any payload).
 *
 * @author Eugene Shevchenko
 */
public abstract class SignalEvent extends Event<Void> {

    public SignalEvent() {
        super(null);
    }
}
