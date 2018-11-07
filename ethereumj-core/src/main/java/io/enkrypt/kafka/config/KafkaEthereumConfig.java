package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.listener.BlockSummaryEthereumListener;
import io.enkrypt.kafka.listener.KafkaBlockListener;
import io.enkrypt.kafka.listener.KafkaPendingTxsListener;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.listener.CompositeEthereumListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@Import({ KafkaConfig.class })
public class KafkaEthereumConfig {

  private static Logger logger = LoggerFactory.getLogger("kafka");

  @Autowired
  EventDispatchThread eventDispatchThread;

  public KafkaEthereumConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public KafkaPendingTxsListener kafkaPendingTxsListener(Kafka kafka) {
    return new KafkaPendingTxsListener(kafka);
  }

  @Bean
  public KafkaBlockListener kafkaBlockListener(Kafka kafka, SystemProperties config, KafkaPendingTxsListener pendingTxnsListener) {

    final KafkaBlockListener blockListener = new KafkaBlockListener(kafka, config, pendingTxnsListener);

    // run the block listener with it's own thread and handle shutdown

    final ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(blockListener);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      blockListener.stop();
      executor.shutdown();

      try {
        executor.awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.warn("shutdown: executor interrupted: {}", e.getMessage());
      }

    }));

    return blockListener;
  }


  @Bean @Primary
  public CompositeEthereumListener ethereumListener(Kafka kafka,
                                                    SystemProperties config,
                                                    BlockSummaryStore blockSummaryStore,
                                                    KafkaPendingTxsListener pendingTxsListener,
                                                    KafkaBlockListener blockListener) {

    final CompositeEthereumListener compositeListener = new CompositeEthereumListener();

    // persist block summaries using the main calling thread to ensure data consistency in case of failure

    compositeListener.addInlineListener(new BlockSummaryEthereumListener(blockSummaryStore));

    // listen for pending transactions and blocks in separate event thread

    compositeListener.addListener(pendingTxsListener);
    compositeListener.addListener(blockListener);

    return compositeListener;
  }

}
