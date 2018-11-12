package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public class NullKafka implements Kafka {

  @Override
  public KafkaProducer<byte[], TransactionRecord> getPendingTransactionsProducer() {
    return null;
  }

  @Override
  public KafkaProducer<Long, BlockSummaryRecord> getBlockSummaryProducer() {
    return null;
  }
}
