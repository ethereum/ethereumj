package org.ethereum.sync.listener;

import org.ethereum.core.BlockWrapper;

/**
 * @author Mikhail Kalinin
 * @since 04.02.2016
 */
public class SyncListenerAdapter implements SyncListener {

    @Override
    public void onHeadersAdded() {
    }

    @Override
    public void onNewBlockNumber(long number) {
    }

    @Override
    public void onNoParent(BlockWrapper block) {
    }
}
