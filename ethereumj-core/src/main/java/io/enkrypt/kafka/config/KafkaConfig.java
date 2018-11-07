package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.serialization.EthereumKeySerializer;
import io.enkrypt.kafka.serialization.EthereumValueSerializer;
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
  public BlockSummaryStore blockSummaryStore() {
    return new BlockSummaryStore(commonConfig.keyValueDataSource("block-summaries"));
  }

  @Bean
  public Kafka kafka(SystemProperties config) {
    final boolean enabled = ((KafkaSystemProperties) config).isKafkaEnabled();
    final String bootstrapServers = ((KafkaSystemProperties) config).getKafkaBootstrapServers();

    if (!enabled) {
      return new NullKafka();
    }

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj");

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, EthereumKeySerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EthereumValueSerializer.class.getName());

    // we use byte array serialization as we are using rlp where required
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);

    return new KafkaImpl(props);
  }
}
