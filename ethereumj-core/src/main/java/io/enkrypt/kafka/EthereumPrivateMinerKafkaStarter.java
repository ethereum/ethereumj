package io.enkrypt.kafka;

import io.enkrypt.kafka.config.PrivateMinerConfig;
import org.ethereum.facade.EthereumFactory;

public class EthereumPrivateMinerKafkaStarter {

  public static void main(String args[]) {
    EthereumFactory.createEthereum(PrivateMinerConfig.class);
  }
}
