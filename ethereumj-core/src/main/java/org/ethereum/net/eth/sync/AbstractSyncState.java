package org.ethereum.net.eth.sync;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public abstract class AbstractSyncState implements SyncState {

    protected SyncManager syncManager;

    protected SyncStateName name;

    protected AbstractSyncState(SyncStateName name) {
        this.name = name;
    }

    @Override
    public boolean is(SyncStateName name) {
        return this.name == name;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public void doOnTransition() {
    }

    @Override
    public void doMaintain() {
    }

    public void setSyncManager(SyncManager syncManager) {
        this.syncManager = syncManager;
    }
}
