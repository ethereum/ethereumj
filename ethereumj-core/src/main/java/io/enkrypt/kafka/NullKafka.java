package io.enkrypt.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;

public class NullKafka implements Kafka {

  private static final RecordMetadata EMPTY_RECORD_METADATA =
      new RecordMetadata(null, -1, -1, -1, null, 0, 0);

  @Override public <K, V> Future<RecordMetadata> send(Kafka.Producer producer, K key, V value) {
    return CompletableFuture.completedFuture(EMPTY_RECORD_METADATA);
  }

  @Override public <K, V> void sendSync(Kafka.Producer producer, K key, V value) {
  }
}
