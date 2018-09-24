package io.enkrypt.kafka;

import io.enkrypt.kafka.config.PrivateNetKafkaConfig;
import org.ethereum.facade.EthereumFactory;

public class EthereumPrivateNetKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(PrivateNetKafkaConfig.class);
  }
}
