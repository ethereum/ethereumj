package org.ethereum.sync.state;

import org.ethereum.sync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikhail Kalinin
 * @since 13.08.2015
 */
public abstract class AbstractSyncState implements SyncState {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    protected SyncManager syncManager;

    protected SyncQueue queue;

    protected SyncPool pool;

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
        logger.trace("Transit to {} state", name);
    }

    @Override
    public void doMaintain() {
        logger.trace("Maintain {} state", name);
    }

    public void setSyncManager(SyncManager syncManager) {
        this.syncManager = syncManager;
    }

    public void setQueue(SyncQueue queue) {
        this.queue = queue;
    }

    public void setPool(SyncPool pool) {
        this.pool = pool;
    }
}
