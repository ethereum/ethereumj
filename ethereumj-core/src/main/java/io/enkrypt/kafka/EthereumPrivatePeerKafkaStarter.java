package io.enkrypt.kafka;

import io.enkrypt.kafka.config.PrivatePeerKafkaConfig;
import org.ethereum.facade.EthereumFactory;

public class EthereumPrivatePeerKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(PrivatePeerKafkaConfig.class);
  }
}
