package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.contract.ERC20Abi;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.listener.BlockSummaryEthereumListener;
import io.enkrypt.kafka.listener.KafkaEthereumListener;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.net.URISyntaxException;

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
                                                    BlockchainImpl blockchain,
                                                    SystemProperties config) {

    CompositeEthereumListener listener = new CompositeEthereumListener();

    // async dispatch
    listener.addListener(new KafkaEthereumListener(kafka, blockchain, config));

    return listener;
  }

}
