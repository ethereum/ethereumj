package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockKeyRecord;
import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public interface Kafka {

  String TOPIC_BLOCKS = "blocks";
  String TOPIC_PENDING_TRANSACTIONS = "pending-transactions";

  KafkaProducer<TransactionKeyRecord, TransactionRecord> getPendingTransactionsProducer();

  KafkaProducer<BlockKeyRecord, BlockRecord> getBlockProducer();

}
