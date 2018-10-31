package io.enkrypt.kafka.config;

import org.ethereum.config.SystemProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaEthereumConfig.class)
public class PrivatePeerKafkaConfig {

  @Bean
  public SystemProperties systemProperties() {
    return KafkaSystemProperties.getKafkaPrivatePeerSystemProperties();
  }

}
