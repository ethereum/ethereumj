package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.listener.BlockSummaryEthereumListener;
import io.enkrypt.kafka.listener.KafkaEthereumListener;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.listener.CompositeEthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({ KafkaConfig.class })
public class KafkaEthereumConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  public KafkaEthereumConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean @Primary
  public CompositeEthereumListener ethereumListener(Kafka kafka,
                                                    Blockchain blockchain,
                                                    SystemProperties config,
                                                    BlockSummaryStore blockSummaryStore) {


    CompositeEthereumListener listener = new CompositeEthereumListener();

    // sync dispatch
    listener.addInlineListener(new BlockSummaryEthereumListener(blockSummaryStore));

    // async dispatch
    listener.addListener(new KafkaEthereumListener(kafka, blockchain, config));

    return listener;
  }

}
