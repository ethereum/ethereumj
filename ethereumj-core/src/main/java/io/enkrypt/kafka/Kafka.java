package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockSummaryKeyRecord;
import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public interface Kafka {

  String TOPIC_BLOCKS = "block-summaries";
  String TOPIC_PENDING_TRANSACTIONS = "pending-transactions";

  KafkaProducer<TransactionKeyRecord, TransactionRecord> getPendingTransactionsProducer();

  KafkaProducer<BlockSummaryKeyRecord, BlockSummaryRecord> getBlockSummaryProducer();

}
