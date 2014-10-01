package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.net.message.Message;

/**
 * @author Roman Mandeleil
 * Created on: 08/08/2014 15:22
 */
public class EthereumListenerAdapter implements EthereumListener {

    @Override
    public void trace(String output) {
    }

    @Override
    public void onBlock(Block block) {
    }

    @Override
    public void onPreloadedBlock(Block block) {
    }

    @Override
    public void onRecvMessage(Message message) {
    }

    @Override
    public void onSendMessage(Message message) {
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
    }
}
