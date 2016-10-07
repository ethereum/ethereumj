package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessage;
import org.ethereum.net.eth.message.GetNodeDataMessage;
import org.ethereum.net.eth.message.GetReceiptsMessage;
import org.ethereum.net.eth.message.NodeDataMessage;
import org.ethereum.net.eth.message.ReceiptsMessage;

import org.ethereum.util.Value;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.net.eth.EthVersion.V63;

/**
 * Fast synchronization (PV63) Handler
 */
@Component
@Scope("prototype")
public class Eth63 extends Eth62 {

    @Autowired
    private RepositoryImpl repository;

    private static final EthVersion version = V63;

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
                // TODO: Implement
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
            Value value = repository.getState(stateRoot);
            if (value != null) {
                states.add(value);
                logger.trace("Eth63: " + Hex.toHexString(stateRoot).substring(0, 8) + " -> " + value);
            } else {
                System.out.println("Not found: " + Hex.toHexString(stateRoot));
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
        };

        sendMessage(new ReceiptsMessage(receipts));
    }
}
