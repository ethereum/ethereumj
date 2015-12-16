package org.ethereum.sync;

/**
 * @author Mikhail Kalinin
 * @since 16.12.2015
 */
public interface StateInitiator {

    SyncStateName initiate();
}
