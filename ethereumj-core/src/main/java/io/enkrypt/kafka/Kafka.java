package io.enkrypt.kafka;

import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;

public interface Kafka {

  enum Producer {

    BLOCKS("blocks"),
    TRANSACTIONS("transactions"),
    TRANSACTION_EXECUTIONS("transaction-executions"),
    TRANSACTION_TRACES("transaction-traces"),
    PENDING_TRANSACTIONS("pending-transactions"),
    ACCOUNT_STATE("account-state");

    public String topic;

    Producer(String topic) {
      this.topic = topic;
    }
  }

  <K, V> Future<RecordMetadata> send(Kafka.Producer producer, K key, V value);

  <K, V> void sendSync(Kafka.Producer producer, K key, V value);
}
