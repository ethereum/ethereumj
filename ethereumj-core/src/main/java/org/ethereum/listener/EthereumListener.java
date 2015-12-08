package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
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

    void trace(String output);

    void onNodeDiscovered(Node node);

    void onHandShakePeer(Channel channel, HelloMessage helloMessage);

    void onEthStatusUpdated(Channel channel, StatusMessage status);

    void onRecvMessage(Message message);

    void onSendMessage(Message message);

    void onBlock(Block block, List<TransactionReceipt> receipts);

    void onPeerDisconnect(String host, long port);

    void onPendingTransactionsReceived(Set<Transaction> transactions);

    void onSyncDone();

    void onNoConnections();

    void onVMTraceCreated(String transactionHash, String trace);

    void onTransactionExecuted(TransactionExecutionSummary summary);
}
