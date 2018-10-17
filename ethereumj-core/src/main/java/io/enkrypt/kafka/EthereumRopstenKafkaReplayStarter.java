package io.enkrypt.kafka;

import io.enkrypt.kafka.config.RopstenKafkaStateReplayConfig;
import io.enkrypt.kafka.replay.StateReplayer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class EthereumRopstenKafkaReplayStarter {

  public static void main(String[] args) {

    final AbstractApplicationContext context = new AnnotationConfigApplicationContext(RopstenKafkaStateReplayConfig.class);
    context.registerShutdownHook();

    final StateReplayer replayer = context.getBean(StateReplayer.class);
    replayer.replay();

    System.exit(0);

  }

}
