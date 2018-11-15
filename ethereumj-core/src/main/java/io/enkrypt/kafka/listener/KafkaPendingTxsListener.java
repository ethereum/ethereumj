package io.enkrypt.kafka.listener;

import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.mapping.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ethereum.core.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaPendingTxsListener implements EthereumListener {

  private final Producer<TransactionKeyRecord, TransactionRecord> producer;
  private final ObjectMapper objectMapper;

  private final AtomicInteger numPendingTxs = new AtomicInteger(0);

  public KafkaPendingTxsListener(Kafka kafka, ObjectMapper objectMapper) {
    this.producer = kafka.getPendingTransactionsProducer();
    this.objectMapper = objectMapper;
  }

  public int getNumPendingTxs() {
    return numPendingTxs.get();
  }

  @Override
  public void onPendingTransactionUpdate(final TransactionReceipt txReceipt, final PendingTransactionState state, final Block block) {

    final byte[] txHash = txReceipt.getTransaction().getHash();

    final TransactionKeyRecord key = TransactionKeyRecord.newBuilder()
      .setHash(ByteBuffer.wrap(txHash))
      .build();

    try {

      switch (state) {

        case DROPPED:
        case INCLUDED:
          // send a tombstone to 'remove' as any included transactions will be sent in the onBlock and
          // we no longer care about dropped transactions

          producer.send(new ProducerRecord<>(Kafka.TOPIC_PENDING_TRANSACTIONS, key, null)).get();
          numPendingTxs.decrementAndGet();
          break;

        case NEW_PENDING:
          final TransactionRecord record = objectMapper.convert(null, Transaction.class, TransactionRecord.class, txReceipt.getTransaction());
          producer.send(new ProducerRecord<>(Kafka.TOPIC_PENDING_TRANSACTIONS, key, record)).get();
          numPendingTxs.incrementAndGet();
          break;

        default:
          // Do nothing
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

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
  public void onSyncDone(SyncState state) {

  }

  @Override
  public void onNoConnections() {

  }

  @Override
  public void onVMTraceCreated(String transactionHash, String trace) {

  }

  @Override
  public void onTransactionExecuted(TransactionExecutionSummary summary) {

  }

  @Override
  public void onPeerAddedToSyncPool(Channel peer) {

  }
}
