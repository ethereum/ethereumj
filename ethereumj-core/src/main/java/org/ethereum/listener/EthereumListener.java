package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.vm.program.InternalTransaction;

import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface EthereumListener {

    void trace(String output);

    void onBlock(Block block, List<TransactionReceipt> receipts);

    void onRecvMessage(Message message);

    void onSendMessage(Message message);

    void onPeerDisconnect(String host, long port);

    void onPendingTransactionsReceived(Set<Transaction> transactions);

    void onSyncDone();

    void onNoConnections();

    void onHandShakePeer(Node node, HelloMessage helloMessage);

    void onVMTraceCreated(String transactionHash, String trace);

    void onTransactionExecuted(TransactionExecutionSummary summary);

    void onNodeDiscovered(Node node);

    void onEthStatusUpdated(Node node, StatusMessage status);
}
