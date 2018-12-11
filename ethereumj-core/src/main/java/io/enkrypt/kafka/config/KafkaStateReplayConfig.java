package io.enkrypt.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import io.enkrypt.kafka.db.BlockRecordStore;
import io.enkrypt.kafka.listener.KafkaBlockSummaryPublisher;
import io.enkrypt.kafka.listener.KafkaPendingTxsListener;
import io.enkrypt.kafka.mapping.ObjectMapper;
import io.enkrypt.kafka.replay.StateReplayer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.rocksdb.RocksDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class KafkaStateReplayConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  public KafkaStateReplayConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public SystemProperties systemProperties() {
    return KafkaSystemProperties.getKafkaSystemProperties();
  }

  @Bean
  public StateReplayer stateReplayer() {
    return new StateReplayer();
  }

  DbSource<byte[]> dbSource(String name, DbSettings settings) {
    final RocksDbDataSource ds = new RocksDbDataSource();
    ds.setName(name);
    ds.init(settings);
    return ds;
  }

  @Bean
  public BlockRecordStore blockSummaryStore() {
    return new BlockRecordStore(dbSource("block-summaries", DbSettings.DEFAULT));
  }

  @Bean
  public ExecutorService executorService() {
    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      executor.shutdown();

      try {
        executor.awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.warn("shutdown: executor interrupted: {}", e.getMessage());
      }

    }));

    return executor;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public Kafka kafka(SystemProperties config) {

    final KafkaSystemProperties kafkaConfig = (KafkaSystemProperties) config;

    final boolean enabled = kafkaConfig.isKafkaEnabled();
    final String bootstrapServers = kafkaConfig.getKafkaBootstrapServers();

    if (!enabled) {
      return new NullKafka();
    }

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj-state-replayer");

    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfig.getKafkaSchemaRegistryUrl());

    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);

    return new KafkaImpl(props);
  }

  @Bean
  public KafkaPendingTxsListener kafkaPendingTxsListener(Kafka kafka, ObjectMapper objectMapper) {
    return new KafkaPendingTxsListener(kafka, objectMapper);
  }

  @Bean
  public KafkaBlockSummaryPublisher kafkaBlockListener(Kafka kafka,
                                                       ExecutorService executor) {

    final KafkaBlockSummaryPublisher blockListener = new KafkaBlockSummaryPublisher(kafka);

    // run the block listener with it's own thread and handle shutdown

    executor.submit(blockListener);

    Runtime.getRuntime().addShutdownHook(new Thread(blockListener::stop));

    return blockListener;
  }
}
