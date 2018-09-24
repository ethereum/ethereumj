package io.enkrypt.kafka.config;

import java.io.File;
import org.ethereum.config.SystemProperties;

public class KafkaSystemProperties extends SystemProperties {

  private static SystemProperties CONFIG;

  public static SystemProperties getKafkaSystemProperties() {
    if (CONFIG == null) {
      final String path = Thread.currentThread().getContextClassLoader().getResource("kafka-ethereumj.conf").getPath();
      CONFIG = new KafkaSystemProperties(new File(path));
    }
    return CONFIG;
  }

  static SystemProperties getKafkaRopstenSystemProperties() {
    if (CONFIG == null) {
      final String path = Thread.currentThread().getContextClassLoader().getResource("kafka-ropsten.conf").getPath();
      CONFIG = new KafkaSystemProperties(new File(path));
    }
    return CONFIG;
  }

  static SystemProperties getKafkaPrivateNetSystemProperties() {
    if (CONFIG == null) {
      final String path = Thread.currentThread().getContextClassLoader().getResource("kafka-private-net.conf").getPath();
      CONFIG = new KafkaSystemProperties(new File(path));
    }
    return CONFIG;
  }

  private KafkaSystemProperties(File configFile) {
    super(configFile);
  }

  boolean isKafkaEnabled() {
    return getConfig().getBoolean("kafka.enabled");
  }

  String getKafkaBootstrapServers() {
    return getConfig().getString("kafka.bootstrapServers");
  }

  String getSchemaRegistryUrl() {
    return getConfig().getString("kafka.schema.registryUrl");
  }
}
