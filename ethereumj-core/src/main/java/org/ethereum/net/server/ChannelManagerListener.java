package org.ethereum.net.server;

/**
 * Channel manager events
 *
 * @author Tiberius Iliescu
 */
public interface ChannelManagerListener {

    void onNewPeerChannel(Channel peer);

    void onPeerChannelDisconnected(Channel peer);
}
