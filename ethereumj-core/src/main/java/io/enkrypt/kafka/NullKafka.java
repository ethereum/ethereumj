package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockSummaryKeyRecord;
import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public class NullKafka implements Kafka {

  @Override
  public KafkaProducer<TransactionKeyRecord, TransactionRecord> getPendingTransactionsProducer() {
    return null;
  }

  @Override
  public KafkaProducer<BlockSummaryKeyRecord, BlockSummaryRecord> getBlockSummaryProducer() {
    return null;
  }
}
