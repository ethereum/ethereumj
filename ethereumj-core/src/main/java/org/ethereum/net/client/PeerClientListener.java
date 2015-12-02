package org.ethereum.net.client;


import org.ethereum.net.server.Channel;

/**
 * @author Tiberius Iliescu
 */
public interface PeerClientListener {
    void onChannelInit(PeerClient peerClient, Channel channel);
    void onChannelClose(PeerClient peerClient, Channel channel);
    void onConnectException(PeerClient peerClient);
}
