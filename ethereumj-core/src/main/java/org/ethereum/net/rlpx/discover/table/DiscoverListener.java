package org.ethereum.net.rlpx.discover.table;

import org.ethereum.net.rlpx.discover.DiscoverNodeEvent;

/**
 * Created by Anton Nashatyrev on 17.07.2015.
 */
public interface DiscoverListener {
    void nodeStatusChanged(DiscoverNodeEvent event);
}
