package org.ethereum.net.server;

/**
 * @author Tiberius Iliescu
 */
public interface ChannelListener {

    void onChannelInit(Channel channel);
    void onChannelActive(Channel channel);
    void onChannelClose(Channel channel);
}
