package org.ethereum.net.rlpx.discover;

import org.ethereum.util.Functional;

/**
 * Allows to handle discovered nodes state changes
 *
 * Created by Anton Nashatyrev on 21.07.2015.
 */
public interface DiscoverListener {

    /**
     * Invoked whenever a new node appeared which meets criteria specified
     * in the {@link NodeManager#addDiscoverListener} method
     */
    void nodeAppeared(NodeHandler handler);

    /**
     * Invoked whenever a node stops meeting criteria.
     */
    void nodeDisappeared(NodeHandler handler);

    public static class Adapter implements DiscoverListener {
        public void nodeAppeared(NodeHandler handler) {}
        public void nodeDisappeared(NodeHandler handler) {}
    }
}
