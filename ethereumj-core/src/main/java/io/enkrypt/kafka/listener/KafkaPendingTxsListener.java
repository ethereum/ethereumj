package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;

import java.util.concurrent.atomic.AtomicInteger;

public class KafkaPendingTxsListener extends AbstractKafkaEthereumListener {

  private final Producer<byte[], Transaction> producer;

  private final AtomicInteger numPendingTxs = new AtomicInteger(0);

  public KafkaPendingTxsListener(Kafka kafka) {
    this.producer = kafka.getProducer();
  }

  public int getNumPendingTxs() {
    return numPendingTxs.get();
  }

  @Override
  public void onPendingTransactionUpdate(final TransactionReceipt txReceipt, final PendingTransactionState state, final Block block) {
    final byte[] txHash = txReceipt.getTransaction().getHash();

    try {

      switch (state) {

        case DROPPED:
        case INCLUDED:
          // send a tombstone to 'remove' as any included transactions will be sent in the onBlock and
          // we no longer care about dropped transactions

          producer.send(new ProducerRecord<>(Kafka.TOPIC_PENDING_TRANSACTIONS, txHash, null)).get();
          numPendingTxs.decrementAndGet();
          break;

        case NEW_PENDING:
          producer.send(new ProducerRecord<>(Kafka.TOPIC_PENDING_TRANSACTIONS, txHash, txReceipt.getTransaction())).get();
          numPendingTxs.incrementAndGet();
          break;

        default:
          // Do nothing
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }


  }

}
