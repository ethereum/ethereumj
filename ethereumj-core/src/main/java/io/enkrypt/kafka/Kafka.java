package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public interface Kafka {

  String TOPIC_BLOCKS = "blocks";
  String TOPIC_PENDING_TRANSACTIONS = "pending-transactions";

  KafkaProducer<byte[], TransactionRecord> getPendingTransactionsProducer();

  KafkaProducer<Long, BlockSummaryRecord> getBlockSummaryProducer();

}
