package io.enkrypt.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class KafkaImpl implements Kafka {

  private KafkaProducer<byte[], byte[]> producer;
  private KafkaProducer<byte[], byte[]> transactionalProducer;

  public KafkaImpl(Properties baseConfig) {
    init(baseConfig);
  }

  private void init(Properties config){

    this.producer = new KafkaProducer<>(config);

    config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    config.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj-transactional");
    config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "ethereumj");

    this.transactionalProducer = new KafkaProducer<>(config);
    transactionalProducer.initTransactions();

  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> KafkaProducer<K, V> getProducer() {
    return (KafkaProducer<K, V>) this.producer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> KafkaProducer<K, V> getTransactionalProducer() {
    return (KafkaProducer<K, V>) this.transactionalProducer;
  }

}
