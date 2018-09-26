package io.enkrypt.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.listener.KafkaEthereumListener;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import io.enkrypt.kafka.db.KafkaIndexedBlockStore;
import io.enkrypt.kafka.db.KafkaTransactionStore;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  @Autowired
  ApplicationContext appCtx;

  @Autowired
  CommonConfig commonConfig;

  @Autowired
  SystemProperties config;

  public KafkaConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  /**
   * Our main entry point into blockchain state
   *
   * @return
   */
  @Bean(name = "EthereumListener")
  public CompositeEthereumListener ethereumListener() {

    final EthereumListener listener = new KafkaEthereumListener(kafka());

    final CompositeEthereumListener compositeListener = new CompositeEthereumListener();
    compositeListener.addListener(listener);

    return compositeListener;
  }

  @Bean
  public Kafka kafka() {
    final boolean enabled = ((KafkaSystemProperties) config).isKafkaEnabled();
    final String bootstrapServers = ((KafkaSystemProperties) config).getKafkaBootstrapServers();
    final String schemaRegistryUrl = ((KafkaSystemProperties) config).getSchemaRegistryUrl();

    if (!enabled) {
      return new NullKafka();
    }

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ethereumj");

    // we use byte array serialization as we are using rlp where required
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2000000000);

    return new KafkaImpl(new KafkaProducer<>(props));
  }
}
