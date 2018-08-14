package org.ethereum.publish.event;

import org.ethereum.core.PendingState;

/**
 * PendingState changes on either new pending transaction or new best block receive
 * When a new transaction arrives it is executed on top of the current pending state
 * When a new best block arrives the PendingState is adjusted to the new Repository state
 * and all transactions which remain pending are executed on top of the new PendingState
 *
 * @author Eugene Shevchenko
 */
public class PendingStateChanged extends Event<PendingState> {
    public PendingStateChanged(PendingState state) {
        super(state);
    }
}
