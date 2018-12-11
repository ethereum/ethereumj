package io.enkrypt.kafka.config;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockRecordStore;
import io.enkrypt.kafka.listener.BlockSummaryEthereumListener;
import io.enkrypt.kafka.listener.KafkaBlockSummaryPublisher;
import io.enkrypt.kafka.listener.KafkaPendingTxsListener;
import io.enkrypt.kafka.mapping.ObjectMapper;
import org.ethereum.config.SystemProperties;
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
@Import({KafkaConfig.class})
public class KafkaEthereumConfig {

  private static Logger logger = LoggerFactory.getLogger("kafka");

  @Autowired
  EventDispatchThread eventDispatchThread;

  public KafkaEthereumConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public KafkaPendingTxsListener kafkaPendingTxsListener(Kafka kafka, ObjectMapper objectMapper) {
    return new KafkaPendingTxsListener(kafka, objectMapper);
  }

  @Bean
  public KafkaBlockSummaryPublisher kafkaBlockListener(Kafka kafka) {

    final KafkaBlockSummaryPublisher blockListener = new KafkaBlockSummaryPublisher(kafka);

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


  @Bean
  @Primary
  public CompositeEthereumListener ethereumListener(SystemProperties config,
                                                    ObjectMapper objectMapper,
                                                    BlockRecordStore blockRecordStore,
                                                    KafkaPendingTxsListener pendingTxsListener,
                                                    KafkaBlockSummaryPublisher blockListener) {

    final CompositeEthereumListener compositeListener = new CompositeEthereumListener();

    // TODO make block listening inline to ensure failure to persist causes all processing to stop

    final BlockSummaryEthereumListener blockSummaryListener =
      new BlockSummaryEthereumListener(
        config,
        blockRecordStore,
        blockListener,
        pendingTxsListener,
        objectMapper
      );

    compositeListener.addListener(blockSummaryListener);

    return compositeListener;
  }

}
