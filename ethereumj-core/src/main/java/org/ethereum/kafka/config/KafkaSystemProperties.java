package org.ethereum.kafka.config;

import org.ethereum.config.SystemProperties;

public class KafkaSystemProperties extends SystemProperties {

  private static SystemProperties CONFIG;

  public static SystemProperties getKafkaSystemProperties() {
    if (CONFIG == null) {
      CONFIG = new KafkaSystemProperties();
    }
    return CONFIG;
  }

  public String getKafkaBootstrapServers() {
    return getConfig().getString("kafka.bootstrapServers");
  }

}
