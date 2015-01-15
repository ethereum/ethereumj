package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.net.message.Message;

import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface EthereumListener {

    public void trace(String output);

    public void onBlock(Block block);
    
    public void onRecvMessage(Message message);

    public void onSendMessage(Message message);

    public void onPeerDisconnect(String host, long port);

    public void onPendingTransactionsReceived(Set<Transaction> transactions);

    public void onSyncDone();

    public void onNoConnections();

    public void onHandShakePeer();

}
