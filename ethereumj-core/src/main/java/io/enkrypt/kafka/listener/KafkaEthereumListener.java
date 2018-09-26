package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import org.ethereum.core.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;

import java.util.List;

public class KafkaEthereumListener implements EthereumListener {

    private final Kafka kafka;

    public KafkaEthereumListener(Kafka kafka) {
        this.kafka = kafka;
    }

    @Override
    public void trace(String output) {

    }

    @Override
    public void onNodeDiscovered(Node node) {

    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {

    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage status) {

    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {

    }

    @Override
    public void onSendMessage(Channel channel, Message message) {

    }

    @Override
    public void onBlock(BlockSummary blockSummary) {
        final byte[] hash = blockSummary.getBlock().getHash();
        kafka.send(Kafka.Producer.BLOCKS, hash, blockSummary.getEncoded());
    }

    @Override
    public void onPeerDisconnect(String host, long port) {

    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        // deprecated in favour of onPendingTransactionUpdate
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {

        byte[] txHash = txReceipt.getTransaction().getHash();

        switch(state) {

            case DROPPED:
            case INCLUDED:
                // send a tombstone to 'remove' as any included transactions will be sent in the onBlock and
                // we no longer care about dropped transactions
                kafka.send(Kafka.Producer.PENDING_TRANSACTIONS, txHash, null);
                break;

            case NEW_PENDING:
            case PENDING:
                kafka.send(Kafka.Producer.PENDING_TRANSACTIONS, txHash, txReceipt.getEncoded());
                break;

            default:
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    @Override
    public void onSyncDone(SyncState state) {

    }

    @Override
    public void onNoConnections() {

    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {
        final byte[] txHashBytes = ByteUtil.hexStringToBytes(transactionHash);
        kafka.send(Kafka.Producer.TRANSACTION_TRACES, txHashBytes, trace.getBytes());

    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {

        // NOTE currently the TransactionExecutor does not encode touched storage or storage diff.

        // NOTE this will likely be called for pending and included transactions

        // TODO modify TransactionExecutionSummary to include latest AccountState and other data which will be of interest
        // instead of just differential changes. Look at TransactionExecutor Line 437

        final byte[] txHash = summary.getTransactionHash();
        kafka.send(Kafka.Producer.TRANSACTION_EXECUTIONS, txHash, summary.getEncoded());
    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {

    }
}
