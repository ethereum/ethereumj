package io.enkrypt.kafka;

import io.enkrypt.kafka.config.RopstenKafkaConfig;
import org.ethereum.facade.EthereumFactory;

public class EthereumRopstenKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(RopstenKafkaConfig.class);
  }
}
