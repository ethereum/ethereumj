/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.core.PendingState;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.publish.Publisher;
import org.ethereum.publish.event.Events;
import org.ethereum.publish.event.PendingTransactionUpdated;
import org.ethereum.publish.event.SyncDone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Backward compatibility component that holds old and new event publishing implementations.<br>
 * Proxies all events to both implementations.
 * Will be removed after {@link EthereumListener} eviction.
 *
 * @author Eugene Shevchenko
 */
@Primary
@Component
public class BackwardCompatibilityEthereumListenerProxy implements EthereumListener {

    private final CompositeEthereumListener compositeListener;
    private final Publisher publisher;

    @Autowired
    public BackwardCompatibilityEthereumListenerProxy(CompositeEthereumListener listener, Publisher publisher) {
        this.compositeListener = listener;
        this.publisher = publisher;
    }

    public CompositeEthereumListener getCompositeListener() {
        return compositeListener;
    }

    public Publisher getPublisher() {
        return publisher;
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
        compositeListener.onBlock(blockSummary);
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
        PendingTransactionUpdated.State translatedState = translate(state, PendingTransactionUpdated.State.class);
        publisher.publish(Events.onPendingTransactionUpdated(block, txReceipt, translatedState));
    }

    @Override
    public void onSyncDone(SyncState state) {
        compositeListener.onSyncDone(state);
        SyncDone.State translatedState = translate(state, SyncDone.State.class);
        publisher.publish(Events.onSyncDone(translatedState));
    }

    private static <S extends Enum<S>, T extends Enum<T>> T translate(S source, Class<T> targetType) {
        return Enum.valueOf(targetType, source.name());
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

    public static BackwardCompatibilityEthereumListenerProxy createDefault() {
        EventDispatchThread eventDispatchThread = EventDispatchThread.getDefault();
        CompositeEthereumListener compositeEthereumListener = new CompositeEthereumListener(eventDispatchThread);
        Publisher publisher = new Publisher(eventDispatchThread);

        return new BackwardCompatibilityEthereumListenerProxy(compositeEthereumListener, publisher);
    }
}
