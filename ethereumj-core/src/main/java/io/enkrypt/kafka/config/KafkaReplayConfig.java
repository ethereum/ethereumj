package io.enkrypt.kafka.config;

import io.enkrypt.kafka.replay.BlockSummaryReplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ KafkaConfig.class })
public class KafkaReplayConfig {

  private static Logger logger = LoggerFactory.getLogger("general");

  public KafkaReplayConfig() {
    // TODO: We can intercept KafkaException to stop completely the app in case of a bad crash
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }

  @Bean
  public BlockSummaryReplayer blockSummaryReplayer() {
    return new BlockSummaryReplayer();
  }
}
