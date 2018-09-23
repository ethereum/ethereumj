package org.ethereum.kafka;

import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;

public class Kafka {

  public enum Producer {
    BLOCKS("blocks"),
    BLOCKS_INFO("blocks-info"),
    TRANSACTIONS("transactions"),
    STATE("state");

    public String topic;

    Producer(String topic) {
      this.topic = topic;
    }
  }

  private final KafkaProducer<Long, Object> longObjectKafkaProducer;
  private final KafkaProducer<String, Object> stringObjectProducer;
  private final KafkaProducer<String, byte[]> stateProducer;

  public Kafka(
      KafkaProducer<Long, Object> longObjectKafkaProducer,
      KafkaProducer<String, Object> stringObjectProducer,
      KafkaProducer<String, byte[]> stateProducer
  ) {
    this.longObjectKafkaProducer = longObjectKafkaProducer;
    this.stringObjectProducer = stringObjectProducer;
    this.stateProducer = stateProducer;
  }

  public <K, V> Future<RecordMetadata> send(Producer producer, K key, V value) {
    final ProducerRecord<K, V> record = new ProducerRecord<>(producer.topic, key, value);
    final KafkaProducer kafkaProducer = toProducer(producer);
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

  private KafkaProducer toProducer(Producer from) {
    switch (from) {
      case BLOCKS:
        return stringObjectProducer;
      case BLOCKS_INFO:
        return longObjectKafkaProducer;
      case TRANSACTIONS:
        return stringObjectProducer;
      case STATE:
        return stateProducer;
    }
    throw new IllegalArgumentException("Invalid producer passed!");
  }

  public static class KafkaProducerException extends KafkaException {
    KafkaProducerException(Throwable cause) {
      super(cause);
    }
  }
}
