package org.ethereum.kafka;

import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.kafka.config.KafkaConfig;

public class EthereumKafkaStarter {

  public static void main(String args[]) {
    createMainnetEthereum();
  }

  private static Ethereum createMainnetEthereum() {
    return EthereumFactory.createEthereum(KafkaConfig.class);
  }

  private static Ethereum createRopstenEthereum() {
    return EthereumFactory.createEthereum(KafkaConfig.class);
  }
}
