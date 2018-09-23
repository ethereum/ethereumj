package org.ethereum.kafka;

import org.ethereum.facade.EthereumFactory;
import org.ethereum.kafka.config.RopstenKafkaConfig;

public class EthereumRopstenKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(RopstenKafkaConfig.class);
  }
}
