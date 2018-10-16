package io.enkrypt.kafka;

import io.enkrypt.kafka.config.KafkaReplayConfig;
import io.enkrypt.kafka.replay.BlockSummaryReplayer;
import org.ethereum.config.CommonConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class EthereumKafkaReplayStarter {

  public static void main(String[] args) {

    final AbstractApplicationContext context = new AnnotationConfigApplicationContext(CommonConfig.class, KafkaReplayConfig.class);
    context.registerShutdownHook();

    final BlockSummaryReplayer replayer = context.getBean(BlockSummaryReplayer.class);
    replayer.replay();

    System.exit(0);

  }

}
