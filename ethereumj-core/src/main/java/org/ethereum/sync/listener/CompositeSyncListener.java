package org.ethereum.sync.listener;

import org.ethereum.core.BlockWrapper;
import org.springframework.stereotype.Component;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Mikhail Kalinin
 * @since 04.02.2016
 */
@Component
public class CompositeSyncListener implements SyncListener {

    private Map<SyncListener, SyncListener> listeners = new IdentityHashMap<>();

    public void add(SyncListener listener) {
        listeners.put(listener, listener);
    }

    public void remove(SyncListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onHeadersAdded() {
        for (SyncListener listener : listeners.keySet())
            listener.onHeadersAdded();
    }

    @Override
    public void onNewBlockNumber(long number) {
        for (SyncListener listener : listeners.keySet())
            listener.onNewBlockNumber(number);
    }

    @Override
    public void onNoParent(BlockWrapper block) {
        for (SyncListener listener : listeners.keySet())
            listener.onNoParent(block);
    }
}
