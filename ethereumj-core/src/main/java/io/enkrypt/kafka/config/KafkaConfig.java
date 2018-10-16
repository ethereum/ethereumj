package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.KafkaImpl;
import io.enkrypt.kafka.NullKafka;
import io.enkrypt.kafka.listener.KafkaEthereumListener;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class KafkaConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  @Autowired
  ApplicationContext appCtx;

  @Autowired
  CommonConfig commonConfig;

  @Autowired
  SystemProperties config;

  @Autowired
  Blockchain blockchain;

  public KafkaConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean @Primary
  public CompositeEthereumListener ethereumListener() {
    CompositeEthereumListener listener = new CompositeEthereumListener();
    listener.addListener(new KafkaEthereumListener(kafka(), blockchain, config));
    return listener;
  }

  @Bean
  public Kafka kafka() {
    final boolean enabled = ((KafkaSystemProperties) config).isKafkaEnabled();
    final String bootstrapServers = ((KafkaSystemProperties) config).getKafkaBootstrapServers();

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
