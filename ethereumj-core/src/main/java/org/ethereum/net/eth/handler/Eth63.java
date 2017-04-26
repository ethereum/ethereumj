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
package org.ethereum.net.eth.handler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.db.StateSource;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessage;
import org.ethereum.net.eth.message.GetNodeDataMessage;
import org.ethereum.net.eth.message.GetReceiptsMessage;
import org.ethereum.net.eth.message.NodeDataMessage;
import org.ethereum.net.eth.message.ReceiptsMessage;

import org.ethereum.sync.PeerState;
import org.ethereum.util.ByteArraySet;
import org.ethereum.util.Value;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.net.eth.EthVersion.V63;

/**
 * Fast synchronization (PV63) Handler
 */
@Component("Eth63")
@Scope("prototype")
public class Eth63 extends Eth62 {

    private static final EthVersion version = V63;

    @Autowired
    private StateSource stateSource;

    private List<byte[]> requestedReceipts;
    private SettableFuture<List<List<TransactionReceipt>>> requestReceiptsFuture;
    private Set<byte[]> requestedNodes;
    private SettableFuture<List<Pair<byte[], byte[]>>> requestNodesFuture;

    public Eth63() {
        super(version);
    }

    @Autowired
    public Eth63(final SystemProperties config, final Blockchain blockchain, BlockStore blockStore,
                 final CompositeEthereumListener ethereumListener) {
        super(version, config, blockchain, blockStore, ethereumListener);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        // Only commands that were added in V63, V62 are handled in child
        switch (msg.getCommand()) {
            case GET_NODE_DATA:
                processGetNodeData((GetNodeDataMessage) msg);
                break;
            case NODE_DATA:
                processNodeData((NodeDataMessage) msg);
                break;
            case GET_RECEIPTS:
                processGetReceipts((GetReceiptsMessage) msg);
                break;
            case RECEIPTS:
                processReceipts((ReceiptsMessage) msg);
                break;
            default:
                break;
        }
    }

    protected synchronized void processGetNodeData(GetNodeDataMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetNodeData, size [{}]",
                channel.getPeerIdShort(),
                msg.getNodeKeys().size()
        );

        List<Value> nodeValues = new ArrayList<>();
        for (byte[] nodeKey : msg.getNodeKeys()) {
            byte[] rawNode = stateSource.get(nodeKey);
            if (rawNode != null) {
                Value value = new Value(rawNode);
                nodeValues.add(value);
                logger.trace("Eth63: " + Hex.toHexString(nodeKey).substring(0, 8) + " -> " + value);
            }
        }

        sendMessage(new NodeDataMessage(nodeValues));
    }

    protected synchronized void processGetReceipts(GetReceiptsMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetReceipts, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHashes().size()
        );

        List<List<TransactionReceipt>> receipts = new ArrayList<>();
        for (byte[] blockHash : msg.getBlockHashes()) {
            Block block = blockchain.getBlockByHash(blockHash);
            if (block == null) continue;

            List<TransactionReceipt> blockReceipts = new ArrayList<>();
            for (Transaction transaction : block.getTransactionsList()) {
                TransactionInfo transactionInfo = blockchain.getTransactionInfo(transaction.getHash());
                if (transactionInfo == null) break;
                blockReceipts.add(transactionInfo.getReceipt());
            }
            receipts.add(blockReceipts);
        }

        sendMessage(new ReceiptsMessage(receipts));
    }

    public synchronized ListenableFuture<List<Pair<byte[], byte[]>>> requestTrieNodes(List<byte[]> hashes) {
        if (peerState != PeerState.IDLE) return null;

        GetNodeDataMessage msg = new GetNodeDataMessage(hashes);
        requestedNodes = new ByteArraySet();
        requestedNodes.addAll(hashes);

        requestNodesFuture = SettableFuture.create();
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        peerState = PeerState.NODE_RETRIEVING;
        return requestNodesFuture;
    }

    public synchronized ListenableFuture<List<List<TransactionReceipt>>> requestReceipts(List<byte[]> hashes) {
        if (peerState != PeerState.IDLE) return null;

        GetReceiptsMessage msg = new GetReceiptsMessage(hashes);
        requestedReceipts = hashes;
        peerState = PeerState.RECEIPT_RETRIEVING;

        requestReceiptsFuture = SettableFuture.create();
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        return requestReceiptsFuture;
    }

    protected synchronized void processNodeData(NodeDataMessage msg) {
        if (requestedNodes == null) {
            logger.debug("Received NodeDataMessage when requestedNodes == null. Dropping peer " + channel);
            dropConnection();
        }

        List<Pair<byte[], byte[]>> ret = new ArrayList<>();
        if(msg.getDataList().isEmpty()) {
            String err = "Received NodeDataMessage contains empty node data. Dropping peer " + channel;
            dropUselessPeer(err);
            return;
        }

        for (Value nodeVal : msg.getDataList()) {
            byte[] hash = nodeVal.hash();
            if (!requestedNodes.contains(hash)) {
                String err = "Received NodeDataMessage contains non-requested node with hash :" + Hex.toHexString(hash) + " . Dropping peer " + channel;
                dropUselessPeer(err);
                return;
            }
            ret.add(Pair.of(hash, nodeVal.encode()));
        }
        requestNodesFuture.set(ret);

        requestedNodes = null;
        requestNodesFuture = null;
        processingTime += (System.currentTimeMillis() - lastReqSentTime);
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }

    protected synchronized void processReceipts(ReceiptsMessage msg) {
        if (requestedReceipts == null) {
            logger.debug("Received ReceiptsMessage when requestedReceipts == null. Dropping peer " + channel);
            dropConnection();
        }


        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing Receipts, size [{}]",
                channel.getPeerIdShort(),
                msg.getReceipts().size()
        );

        List<List<TransactionReceipt>> receipts = msg.getReceipts();

        requestReceiptsFuture.set(receipts);

        requestedReceipts = null;
        requestReceiptsFuture = null;
        processingTime += (System.currentTimeMillis() - lastReqSentTime);
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }


    private void dropUselessPeer(String err) {
        logger.debug(err);
        requestNodesFuture.setException(new RuntimeException(err));
        dropConnection();
    }

    @Override
    public String getSyncStats() {
        double nodesPerSec = 1000d * channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRetrieveTime.get();
        double missNodesRatio = 1 - (double) channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRequested.get();
        long lifeTime = System.currentTimeMillis() - connectedTime;
        return super.getSyncStats() + String.format("\tNodes/sec: %1$.2f, miss: %2$.2f", nodesPerSec, missNodesRatio);
    }
}
