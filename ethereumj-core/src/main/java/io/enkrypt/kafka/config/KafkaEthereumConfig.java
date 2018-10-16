package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.listener.KafkaEthereumListener;
import org.ethereum.core.Blockchain;
import org.ethereum.listener.CompositeEthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ KafkaConfig.class })
public class KafkaEthereumConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  public KafkaEthereumConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean()
  public KafkaEthereumListener kafkaEthereumListener(Blockchain blockchain,
                                                     BlockSummaryStore blockSummaryStore,
                                                     CompositeEthereumListener ethereumListener,
                                                     Kafka kafka) {
    final KafkaEthereumListener result = new KafkaEthereumListener(kafka, blockchain, blockSummaryStore);
    ethereumListener.addListener(result);
    return result;
  }

}
