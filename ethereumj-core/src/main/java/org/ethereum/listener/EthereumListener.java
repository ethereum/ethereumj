package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface EthereumListener {

    void trace(String output);

    void onBlock(Block block);

    void onBlockReciepts(List<TransactionReceipt> receipts);

    void onRecvMessage(Message message);

    void onSendMessage(Message message);

    void onPeerDisconnect(String host, long port);

    void onPendingTransactionsReceived(Set<Transaction> transactions);

    void onSyncDone();

    void onNoConnections();

    void onHandShakePeer(HelloMessage helloMessage);

    void onVMTraceCreated(String transactionHash, String trace);
}
