package org.ethereum.listener;

import org.ethereum.core.*;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.vm.program.InternalTransaction;

import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface EthereumListener {

    enum PendingTransactionState {
        DROPPED,
        PENDING,
        INCLUDED
    }

    void trace(String output);

    void onNodeDiscovered(Node node);

    void onHandShakePeer(Channel channel, HelloMessage helloMessage);

    void onEthStatusUpdated(Channel channel, StatusMessage status);

    void onRecvMessage(Channel channel, Message message);

    void onSendMessage(Channel channel, Message message);

    void onBlock(Block block, List<TransactionReceipt> receipts);

    void onPeerDisconnect(String host, long port);

    void onPendingTransactionsReceived(List<Transaction> transactions);

    void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state);

    void onPendingStateChanged(PendingState pendingState);

    void onSyncDone();

    void onNoConnections();

    void onVMTraceCreated(String transactionHash, String trace);

    void onTransactionExecuted(TransactionExecutionSummary summary);

    void onPeerAddedToSyncPool(Channel peer);
}
