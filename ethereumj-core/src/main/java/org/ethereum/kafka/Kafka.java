package org.ethereum.kafka;

import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;

public class Kafka {

  public enum Producer {
    BLOCKS("blocks"),
    BLOCKS_INFO("info"),
    TRANSACTIONS("transactions"),
    PENDING_TRANSACTIONS("pending-transactions"),
    ACCOUNT_STATE("account-state");

    public String topic;

    Producer(String topic) {
      this.topic = topic;
    }
  }

  private final KafkaProducer kafkaProducer;

  public Kafka(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  public <K, V> Future<RecordMetadata> send(Producer producer, K key, V value) {
    final ProducerRecord<K, V> record = new ProducerRecord<>(producer.topic, key, value);

    try {
      return kafkaProducer.send(record);
    } catch (Exception e) {
      throw new KafkaProducerException(e);
    }
  }

  public <K, V> void sendSync(Producer producer, K key, V value) {
    try {
      send(producer, key, value).get();
    } catch (Exception e) {
      throw new KafkaProducerException(e);
    }
  }

  public static class KafkaProducerException extends KafkaException {
    KafkaProducerException(Throwable cause) {
      super(cause);
    }
  }
}
