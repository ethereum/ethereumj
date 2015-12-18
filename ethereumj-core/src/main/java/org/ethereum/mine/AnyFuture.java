package org.ethereum.mine;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;

/**
 *  Future completes when any of child futures completed. All others are cancelled
 *  upon completion.
 */
public class AnyFuture<V> extends AbstractFuture<V> {
    private List<ListenableFuture<V>> futures = new ArrayList<>();

    /**
     * Add a Future delegate
     */
    public synchronized void add(final ListenableFuture<V> f) {
        if (isCancelled() || isDone()) return;

        f.addListener(new Runnable() {
            public void run() {
                futureCompleted(f);
            }
        },  MoreExecutors.sameThreadExecutor());
        futures.add(f);
    }

    private synchronized void futureCompleted(ListenableFuture<V> f) {
        if (isCancelled() || isDone()) return;
        if (f.isCancelled()) return;

        try {
            cancelOthers(f);
            
            V v = f.get();
            postProcess(v);
            set(v);
        } catch (Exception e) {
            setException(e);
        }
    }

    /**
     * Subclasses my override to perform some task on the calculated
     * value before returning it via Future
     */
    protected void postProcess(V v) {}

    private void cancelOthers(ListenableFuture besidesThis) {
        for (ListenableFuture future : futures) {
            if (future != besidesThis) {
                try {
                    future.cancel(true);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected void interruptTask() {
        cancelOthers(null);
    }
}
