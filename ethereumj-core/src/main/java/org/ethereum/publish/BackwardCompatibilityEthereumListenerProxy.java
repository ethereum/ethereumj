package org.ethereum.publish;

import org.ethereum.core.*;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.publish.event.Events;

import java.util.List;

public class BackwardCompatibilityEthereumListenerProxy implements EthereumListener {

    private final CompositeEthereumListener compositeListener;
    private final Publisher publisher;

    public BackwardCompatibilityEthereumListenerProxy(CompositeEthereumListener listener, Publisher publisher) {
        this.compositeListener = listener;
        this.publisher = publisher;
    }

    public void addListener(EthereumListener listener) {
        this.compositeListener.addListener(listener);
    }

    @Override
    public void trace(String output) {
        compositeListener.trace(output);
    }

    @Override
    public void onNodeDiscovered(Node node) {
        compositeListener.onNodeDiscovered(node);
        publisher.publish(Events.onNodeDiscovered(node));
    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage message) {
        compositeListener.onHandShakePeer(channel, message);
        publisher.publish(Events.onPeerHanshaked(channel, message));
    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage message) {
        compositeListener.onEthStatusUpdated(channel, message);
        publisher.publish(Events.onEthStatusUpdated(channel, message));
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
        compositeListener.onRecvMessage(channel, message);
        publisher.publish(Events.onMessageReceived(channel, message));
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
        compositeListener.onSendMessage(channel, message);
        publisher.publish(Events.onMessageSent(channel, message));
    }

    @Override
    public void onBlock(BlockSummary blockSummary) {
        compositeListener.onBlock(blockSummary);
        publisher.publish(Events.onBlockAdded(blockSummary));
    }

    @Override
    public void onBlock(BlockSummary blockSummary, boolean best) {
        compositeListener.onBlock(blockSummary, best);
        publisher.publish(Events.onBlockAdded(blockSummary, best));
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
        compositeListener.onPeerDisconnect(host, port);
        publisher.publish(Events.onPeerDisconnected(host, port));
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
        compositeListener.onPendingTransactionsReceived(transactions);
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        compositeListener.onPendingStateChanged(pendingState);
        publisher.publish(Events.onPendingStateChanged(pendingState));
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        compositeListener.onPendingTransactionUpdate(txReceipt, state, block);
        publisher.publish(Events.onPendingTransactionUpdated(block, txReceipt, state));
    }

    @Override
    public void onSyncDone(SyncState state) {
        compositeListener.onSyncDone(state);
        publisher.publish(Events.onSyncDone(state));
    }

    @Override
    public void onNoConnections() {
        compositeListener.onNoConnections();
    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {
        compositeListener.onVMTraceCreated(transactionHash, trace);
        publisher.publish(Events.onVmTraceCreated(transactionHash, trace));
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        compositeListener.onTransactionExecuted(summary);
        publisher.publish(Events.onTransactionExecuted(summary));
    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {
        compositeListener.onPeerAddedToSyncPool(peer);
        publisher.publish(Events.onPeerAddedToSyncPool(peer));
    }
}
