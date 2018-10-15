package io.enkrypt.kafka;

import org.ethereum.facade.EthereumFactory;
import io.enkrypt.kafka.config.KafkaConfig;

public class EthereumKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(KafkaConfig.class);
  }
}
