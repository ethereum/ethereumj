package org.ethereum.listener;

import org.ethereum.core.*;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;

import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 08.08.2014
 */
public class EthereumListenerAdapter implements EthereumListener {

    @Override
    public void trace(String output) {
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
    }

    @Override
    public void onSyncDone() {

    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {

    }

    @Override
    public void onNoConnections() {

    }


    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {

    }

    @Override
    public void onNodeDiscovered(Node node) {

    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {

    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {

    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {

    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state) {

    }
}
