package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;

import org.ethereum.net.rlpx.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 12.11.2014
 */
@Component(value = "EthereumListener")
public class CompositeEthereumListener implements EthereumListener {

    List<EthereumListener> listeners = new ArrayList<>();


    @Override
    public void trace(String output) {
        for (EthereumListener listener : listeners)
            listener.trace(output);
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        for (EthereumListener listener : listeners)
            listener.onBlock(block, receipts);
    }

    @Override
    public void onRecvMessage(Message message) {
        for (EthereumListener listener : listeners)
            listener.onRecvMessage(message);
    }

    @Override
    public void onSendMessage(Message message) {
        for (EthereumListener listener : listeners)
                listener.onSendMessage(message);
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
        for (EthereumListener listener : listeners)
            listener.onPeerDisconnect(host, port);
    }

    @Override
    public void onPendingTransactionsReceived(Set<Transaction> transactions) {
        for (EthereumListener listener : listeners)
            listener.onPendingTransactionsReceived(transactions);
    }

    @Override
    public void onSyncDone() {
        for (EthereumListener listener : listeners)
            listener.onSyncDone();
    }


    @Override
    public void onNoConnections() {
        for (EthereumListener listener : listeners)
            listener.onNoConnections();
    }

    @Override
    public void onHandShakePeer(HelloMessage helloMessage) {
        for (EthereumListener listener : listeners)
            listener.onHandShakePeer(helloMessage);
    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {
        for (EthereumListener listener : listeners) {
            listener.onVMTraceCreated(transactionHash, trace);
        }
    }

    @Override
    public void onNodeDiscovered(Node node) {
        for (EthereumListener listener : listeners) {
            listener.onNodeDiscovered(node);
        }
    }

    public void addListener(EthereumListener listener) {
        listeners.add(listener);
    }

}
