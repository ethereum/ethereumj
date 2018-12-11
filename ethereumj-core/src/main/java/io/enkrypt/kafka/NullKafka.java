package io.enkrypt.kafka;

import io.enkrypt.avro.capture.BlockKeyRecord;
import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

public class NullKafka implements Kafka {

  @Override
  public KafkaProducer<TransactionKeyRecord, TransactionRecord> getPendingTransactionsProducer() {
    return null;
  }

  @Override
  public KafkaProducer<BlockKeyRecord, BlockRecord> getBlockProducer() {
    return null;
  }
}
