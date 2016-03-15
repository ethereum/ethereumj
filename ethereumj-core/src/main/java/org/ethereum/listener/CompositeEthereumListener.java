package org.ethereum.listener;

import org.ethereum.core.*;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Roman Mandeleil
 * @since 12.11.2014
 */
@Component(value = "EthereumListener")
public class CompositeEthereumListener implements EthereumListener {

    List<EthereumListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(EthereumListener listener) {
        listeners.add(listener);
    }
    public void removeListener(EthereumListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void trace(String output) {
        for (EthereumListener listener : listeners) {
            listener.trace(output);
        }
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        for (EthereumListener listener : listeners) {
            listener.onBlock(block, receipts);
        }
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
        for (EthereumListener listener : listeners) {
            listener.onRecvMessage(channel, message);
        }
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
        for (EthereumListener listener : listeners) {
            listener.onSendMessage(channel, message);
        }
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
        for (EthereumListener listener : listeners) {
            listener.onPeerDisconnect(host, port);
        }
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
        for (EthereumListener listener : listeners) {
            listener.onPendingTransactionsReceived(transactions);
        }
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        for (EthereumListener listener : listeners) {
            listener.onPendingStateChanged(pendingState);
        }
    }

    @Override
    public void onSyncDone() {
        for (EthereumListener listener : listeners) {
            listener.onSyncDone();
        }
    }

    @Override
    public void onNoConnections() {
        for (EthereumListener listener : listeners) {
            listener.onNoConnections();
        }
    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
        for (EthereumListener listener : listeners) {
            listener.onHandShakePeer(channel, helloMessage);
        }
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

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage status) {
        for (EthereumListener listener : listeners) {
            listener.onEthStatusUpdated(channel, status);
        }
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        for (EthereumListener listener : listeners) {
            listener.onTransactionExecuted(summary);
        }
    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {
        for (EthereumListener listener : listeners) {
            listener.onPeerAddedToSyncPool(peer);
        }
    }

    @Override
    public void onLongSyncDone() {
        for (EthereumListener listener : listeners) {
            listener.onLongSyncDone();
        }
    }

    @Override
    public void onLongSyncStarted() {
        for (EthereumListener listener : listeners) {
            listener.onLongSyncStarted();
        }
    }
}
