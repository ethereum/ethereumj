package org.ethereum.publish.event;

import org.ethereum.listener.EthereumListener;

public class SyncDoneEvent extends Event<EthereumListener.SyncState> implements Single {

    public SyncDoneEvent(EthereumListener.SyncState payload) {
        super(payload);
    }
}
