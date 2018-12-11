package io.enkrypt.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.enkrypt.avro.capture.BlockKeyRecord;
import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.TransactionKeyRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Map;
import java.util.Properties;

public class KafkaImpl implements Kafka {

  private KafkaProducer<BlockKeyRecord, BlockRecord> blockSummaryProducer;
  private KafkaProducer<TransactionKeyRecord, TransactionRecord> txProducer;

  public KafkaImpl(Properties config) {

    final Properties txConfig = copy(config);
    txConfig.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj-pending-transactions");
    txConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    txConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

    this.txProducer = new KafkaProducer<>(txConfig);

    final Properties blockSummaryConfig = copy(config);
    blockSummaryConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    blockSummaryConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

    // transactional settings
    blockSummaryConfig.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj-block-summaries");
    blockSummaryConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    blockSummaryConfig.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "ethereumj");

    this.blockSummaryProducer = new KafkaProducer<>(blockSummaryConfig);
    this.blockSummaryProducer.initTransactions();
  }

  @Override
  public KafkaProducer<TransactionKeyRecord, TransactionRecord> getPendingTransactionsProducer() {
    return txProducer;
  }

  @Override
  public KafkaProducer<BlockKeyRecord, BlockRecord> getBlockProducer() {
    return blockSummaryProducer;
  }

  private Properties copy(Properties config) {
    final Properties result = new Properties();
    for (Map.Entry<Object, Object> entry : config.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
