package io.enkrypt.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

public class NullKafka implements Kafka {

  @Override
  public <K, V> KafkaProducer<K, V> getProducer() {
    return null;
  }

  @Override
  public <K, V> KafkaProducer<K, V> getTransactionalProducer() {
    return null;
  }
}
