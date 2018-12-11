package io.enkrypt.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import io.enkrypt.kafka.db.BlockRecordStore;
import io.enkrypt.kafka.mapping.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  @Autowired
  private CommonConfig commonConfig;

  public KafkaConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public BlockRecordStore blockSummaryStore() {
    return new BlockRecordStore(commonConfig.keyValueDataSource("block-summaries"));
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public Kafka kafka(SystemProperties config) {
    final KafkaSystemProperties kafkaConfig = (KafkaSystemProperties) config;

    if (!kafkaConfig.isKafkaEnabled()) {
      return new NullKafka();
    }

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getKafkaBootstrapServers());

    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfig.getKafkaSchemaRegistryUrl());

    // we use byte array serialization as we are using rlp where required
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);

    return new KafkaImpl(props);
  }
}
