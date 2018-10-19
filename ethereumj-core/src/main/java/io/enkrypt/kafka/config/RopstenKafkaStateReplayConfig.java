package io.enkrypt.kafka.config;

import org.ethereum.config.SystemProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaStateReplayConfig.class)
public class RopstenKafkaStateReplayConfig {

  @Bean
  public SystemProperties systemProperties() {
    return KafkaSystemProperties.getKafkaRopstenSystemProperties();
  }
}
