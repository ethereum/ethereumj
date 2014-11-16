package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.net.message.Message;

import java.util.Set;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 12/11/2014 08:34
 */

public class EthereumListenerWrapper implements EthereumListener{

    EthereumListener listener;


    @Override
    public void trace(String output) {
        if (listener != null)
            listener.trace(output);
    }

    @Override
    public void onBlock(Block block) {
        if (listener != null)
            listener.onBlock(block);
    }


    @Override
    public void onRecvMessage(Message message) {
        if (listener != null)
            listener.onRecvMessage(message);
    }

    @Override
    public void onSendMessage(Message message) {
        if (listener != null)
            listener.onSendMessage(message);
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
        if (listener != null)
            listener.onPeerDisconnect(host, port);
    }

    @Override
    public void onPendingTransactionsReceived(Set<Transaction> transactions) {
        if (listener != null)
            listener.onPendingTransactionsReceived(transactions);
    }

    @Override
    public void onSyncDone() {
        if (listener != null)
            listener.onSyncDone();
    }

    public void addListener(EthereumListener listener){
        if (listener != null)
            this.listener = listener;
    }
}
