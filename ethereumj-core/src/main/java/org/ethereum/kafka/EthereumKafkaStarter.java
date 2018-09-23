package org.ethereum.kafka;

import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.kafka.config.KafkaConfig;
import org.ethereum.kafka.config.RopstenKafkaConfig;

public class EthereumKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(KafkaConfig.class);
  }

  private static Ethereum createRopstenEthereum() {
    return EthereumFactory.createEthereum(RopstenKafkaConfig.class);
  }
}
