package org.ethereum.net.server;

/**
 * @author Tiberius Iliescu
 */
public interface ChannelInitializerListener {

    void onChannelInit(Channel channel);
    void onChannelClose(Channel channel);
}
