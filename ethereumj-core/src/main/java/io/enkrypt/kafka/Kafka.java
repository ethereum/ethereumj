package io.enkrypt.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

public interface Kafka {

  String TOPIC_METADATA = "metadata";
  String TOPIC_BLOCKS = "blocks";
  String TOPIC_PENDING_TRANSACTIONS = "pending-transactions";
  String TOPIC_ACCOUNT_STATE = "account-state";
  String TOPIC_TOKEN_TRANSFERS = "token-transfers";

  <K, V> KafkaProducer<K, V> getProducer();

  <K, V> KafkaProducer<K, V> getTransactionalProducer();

}
