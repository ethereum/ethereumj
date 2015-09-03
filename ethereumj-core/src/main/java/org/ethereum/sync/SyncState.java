package org.ethereum.sync;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public interface SyncState {

    boolean is(SyncStateName name);

    void doOnTransition();

    void doMaintain();
}
