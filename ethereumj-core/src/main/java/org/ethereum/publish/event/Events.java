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
package org.ethereum.publish.event;

import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.PendingState;
import org.ethereum.core.PendingTransaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.publish.event.message.AbstractMessageEvent;
import org.ethereum.publish.event.message.EthStatusUpdated;
import org.ethereum.publish.event.message.MessageReceived;
import org.ethereum.publish.event.message.MessageSent;
import org.ethereum.publish.event.message.PeerHandshaked;
import org.ethereum.sync.SyncManager;

public final class Events {

    public interface Type {

        Class<? extends Event<EthStatusUpdated.Data>> ETH_STATUS_UPDATED = EthStatusUpdated.class;

        Class<? extends Event<AbstractMessageEvent.Data<Message>>> MESSAGE_RECEIVED = MessageReceived.class;

        Class<? extends Event<AbstractMessageEvent.Data<Message>>> MESSAGE_SENT = MessageSent.class;

        Class<? extends Event<PeerHandshaked.Data>> PEER_HANDSHAKED = PeerHandshaked.class;

        Class<? extends Event<BlockAdded.Data>> BLOCK_ADED = BlockAdded.class;

        Class<? extends Event<Node>> NODE_DISCOVERED = NodeDiscovered.class;

        Class<? extends Event<Channel>> PEER_ADDED_TO_SYNC_POOL = PeerAddedToSyncPool.class;

        Class<? extends Event<PeerDisconnected.Data>> PEER_DISCONNECTED = PeerDisconnected.class;

        Class<? extends Event<PendingState>> PENDING_STATE_CHANGED = PendingStateChanged.class;

        Class<? extends Event<PendingTransactionUpdated.Data>> PENDING_TRANSACTION_UPDATED = PendingTransactionUpdated.class;

        Class<? extends Event<SyncManager.State>> SYNC_DONE = SyncDone.class;

        Class<? extends Event<TransactionExecutionSummary>> TRANSACTION_EXECUTED = TransactionExecuted.class;

        Class<? extends Event<VmTraceCreated.Data>> VM_TRACE_CREATED = VmTraceCreated.class;
    }

    private Events() {
    }

    public static Event onEthStatusUpdated(Channel channel, StatusMessage message) {
        return new EthStatusUpdated(channel, message);
    }

    public static Event onMessageReceived(Channel channel, Message message) {
        return new MessageReceived(channel, message);
    }

    public static Event onMessageSent(Channel channel, Message message) {
        return new MessageSent(channel, message);
    }

    public static Event onPeerHanshaked(Channel channel, HelloMessage message) {
        return new PeerHandshaked(channel, message);
    }

    public static Event onBlockAdded(BlockSummary summary, boolean isBest) {
        return new BlockAdded(summary, isBest);
    }

    public static Event onBlockAdded(BlockSummary summary) {
        return onBlockAdded(summary, false);
    }

    public static Event onNodeDiscovered(Node node) {
        return new NodeDiscovered(node);
    }

    public static Event onPeerAddedToSyncPool(Channel channel) {
        return new PeerAddedToSyncPool(channel);
    }

    public static Event onPeerDisconnected(String host, long port) {
        return new PeerDisconnected(host, port);
    }

    public static Event onPendingStateChanged(PendingState state) {
        return new PendingStateChanged(state);
    }

    public static Event onPendingTransactionUpdated(Block block, TransactionReceipt receipt, PendingTransaction.State state) {
        return new PendingTransactionUpdated(block, receipt, state);
    }

    public static Event onSyncDone(SyncManager.State state) {
        return new SyncDone(state);
    }

    public static Event onTransactionExecuted(TransactionExecutionSummary summary) {
        return new TransactionExecuted(summary);
    }

    public static Event onVmTraceCreated(String txHash, String trace) {
        return new VmTraceCreated(txHash, trace);
    }
}
