package org.ethereum.listener;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;

import java.util.List;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 08.08.2014
 */
public class EthereumListenerAdapter implements EthereumListener {

    @Override
    public void trace(String output) {
    }

    @Override
    public void onBlock(Block block) {
    }

    @Override
    public void onRecvMessage(Message message) {
    }

    @Override
    public void onSendMessage(Message message) {
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
    }

    @Override
    public void onPendingTransactionsReceived(Set<Transaction> transactions) {
    }

    @Override
    public void onSyncDone() {

    }

    @Override
    public void onHandShakePeer(HelloMessage helloMessage) {

    }

    @Override
    public void onNoConnections() {

    }

    @Override
    public void onBlockReciepts(List<TransactionReceipt> receipts) {

    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {

    }
}
