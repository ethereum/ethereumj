package org.ethereum.sync.strategy;

import org.ethereum.sync.SyncPool;
import org.ethereum.sync.SyncQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ethereum.util.TimeUtils.secondsToMillis;

/**
 * @author Mikhail Kalinin
 * @since 02.02.2016
 */
public abstract class AbstractSyncStrategy implements SyncStrategy {

    protected final static Logger logger = LoggerFactory.getLogger("sync");

    private static final long WORKER_TIMEOUT = secondsToMillis(1);

    private boolean inProgress = false;

    private ScheduledExecutorService worker = newSingleThreadScheduledExecutor();

    @Autowired
    protected SyncPool pool;

    @Autowired
    protected SyncQueue queue;

    @Override
    public void start() {

        if (inProgress) return;

        inProgress = true;

        worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }, WORKER_TIMEOUT, WORKER_TIMEOUT, MILLISECONDS);
    }

    @Override
    public void stop() {

        if (!inProgress) return;

        worker.shutdown();
        inProgress = false;
    }

    @Override
    public boolean inProgress() {
        return inProgress;
    }

    abstract protected void doWork();
}
