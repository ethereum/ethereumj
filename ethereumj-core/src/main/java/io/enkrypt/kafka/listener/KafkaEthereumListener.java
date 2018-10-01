package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.models.Account;
import java.util.List;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.PendingState;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;

public class KafkaEthereumListener implements EthereumListener {

  private final Kafka kafka;

  public KafkaEthereumListener(Kafka kafka) {
    this.kafka = kafka;
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
  public void onSyncDone(SyncState state) {

  }

  @Override
  public void onNoConnections() {

  }

  @Override
  public void onPeerAddedToSyncPool(Channel peer) {

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

    switch (state) {
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
    }
  }

  @Override
  public void onBlock(BlockSummary blockSummary) {
    // Send blocks

    final long number = blockSummary.getBlock().getNumber();
    final byte[] key = ByteUtil.longToBytes(number);

    kafka.send(Kafka.Producer.BLOCKS, key, blockSummary.getEncoded());

    // Send account balances
    for (TransactionExecutionSummary summary : blockSummary.getSummaries()) {
      final List<Account> touchedAccounts = summary.getTouchedAccounts();
      for (Account account : touchedAccounts) {
        kafka.send(Kafka.Producer.ACCOUNT_STATE, account.getAddress(), account.getRLPEncoded());
      }
    }
  }

  @Override
  public void onVMTraceCreated(String transactionHash, String trace) {

  }

  @Override
  public void onTransactionExecuted(TransactionExecutionSummary summary) {
    //final byte[] txHash = summary.getTransactionHash();
    //kafka.send(Kafka.Producer.TRANSACTION_EXECUTIONS, txHash, summary.getEncoded());
  }

  @Override
  public void trace(String output) {

  }
}
