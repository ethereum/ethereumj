package io.enkrypt.kafka;

import org.ethereum.facade.EthereumFactory;
import io.enkrypt.kafka.config.KafkaEthereumConfig;

public class EthereumKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(KafkaEthereumConfig.class);
  }
}
