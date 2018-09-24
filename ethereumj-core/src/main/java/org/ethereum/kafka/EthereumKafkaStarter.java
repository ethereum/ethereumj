package org.ethereum.kafka;

import org.ethereum.facade.EthereumFactory;
import org.ethereum.kafka.config.KafkaConfig;

public class EthereumKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(KafkaConfig.class);
  }
}
