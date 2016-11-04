package org.ethereum.net.eth.handler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessage;
import org.ethereum.net.eth.message.GetNodeDataMessage;
import org.ethereum.net.eth.message.GetReceiptsMessage;
import org.ethereum.net.eth.message.NodeDataMessage;
import org.ethereum.net.eth.message.ReceiptsMessage;

import org.ethereum.sync.SyncState;
import org.ethereum.util.ByteArraySet;
import org.ethereum.util.Value;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
@Scope("prototype")
public class Eth63 extends Eth62 {

    private static final EthVersion version = V63;

    @Autowired
    private Repository repository;

    private Set<byte[]> requestedNodes;
    private SettableFuture<List<Pair<byte[], byte[]>>> requestNodesFuture;

    private long connectedTime = System.currentTimeMillis();
    private long processingTime = 0;
    private long lastReqSentTime;

    public Eth63() {
        super(version);
    }

    @Autowired
    public Eth63(final SystemProperties config, final Blockchain blockchain,
                 final CompositeEthereumListener ethereumListener) {
        super(version, config, blockchain, ethereumListener);
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
                // TODO: Implement
                break;
            default:
                break;
        }
    }

    protected synchronized void processGetNodeData(GetNodeDataMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetNodeData, size [{}]",
                channel.getPeerIdShort(),
                msg.getStateRoots().size()
        );

        List<Value> states = new ArrayList<>();
        for (byte[] stateRoot : msg.getStateRoots()) {
            Value value = ((RepositoryImpl) repository).getState(stateRoot);
            if (value != null) {
                states.add(value);
                logger.trace("Eth63: " + Hex.toHexString(stateRoot).substring(0, 8) + " -> " + value);
            } else {
            }
        }

        sendMessage(new NodeDataMessage(states));
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
                blockReceipts.add(transactionInfo.getReceipt());
            }
            receipts.add(blockReceipts);
        }

        sendMessage(new ReceiptsMessage(receipts));
    }

    public synchronized ListenableFuture<List<Pair<byte[], byte[]>>> requestTrieNodes(List<byte[]> hashes) {
        GetNodeDataMessage msg = new GetNodeDataMessage(hashes);
        requestedNodes = new ByteArraySet();
        requestedNodes.addAll(hashes);
        requestNodesFuture = SettableFuture.create();
        syncState = SyncState.NODE_RETRIEVING;
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();
        return requestNodesFuture;
    }

    protected synchronized void processNodeData(NodeDataMessage msg) {
        if (requestedNodes == null) {
            logger.debug("Received NodeDataMessage when requestedNodes == null. Dropping peer");
            dropConnection();
        }

        List<Pair<byte[], byte[]>> ret = new ArrayList<>();
        for (Value nodeVal : msg.getDataList()) {
            byte[] hash = nodeVal.hash();
            if (!requestedNodes.contains(hash)) {
                String err = "Received NodeDataMessage contains non-requested node with hash :" + Hex.toHexString(hash) + " . Dropping peer";
                logger.debug(err);
                requestNodesFuture.setException(new RuntimeException(err));
                dropConnection();
                return;
            }
            ret.add(Pair.of(hash, nodeVal.encode()));
        }
        requestNodesFuture.set(ret);

        requestedNodes = null;
        requestNodesFuture = null;
        processingTime += (System.currentTimeMillis() - lastReqSentTime);
        syncState = SyncState.IDLE;
    }

    @Override
    public String getSyncStats() {
        double nodesPerSec = 1000d * channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRetrieveTime.get();
        double missNodesRatio = 1 - (double) channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRequested.get();
        long lifeTime = System.currentTimeMillis() - connectedTime;
        return super.getSyncStats() + String.format("\tNodes/sec: %1$.2f, miss: %2$.2f", nodesPerSec, missNodesRatio) +
                "\tLife: " + lifeTime / 1000 + "s,\tIdle: " + (lifeTime - processingTime) + "ms";
    }
}
