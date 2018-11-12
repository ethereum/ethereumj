package io.enkrypt.kafka.replay;

import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.listener.KafkaBlockListener;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.TransactionExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.Long.parseLong;
import static org.ethereum.util.ByteUtil.longToBytes;

public class StateReplayer {

  @Autowired
  KafkaBlockListener blockListener;

  @Autowired
  BlockSummaryStore store;

  @Autowired
  SystemProperties config;

  @Autowired
  Kafka kafka;

  @Autowired
  ExecutorService executor;

  private final Logger logger = LoggerFactory.getLogger("state-replay");

  public StateReplayer() {
  }

  public void replay() {

    BlockSummaryRecord blockSummary;
    long number = parseLong(config.getProperty("replay.from", "0"));

    logger.info("Attempting to replay from number = {}", number);

    try {
      do {

        blockSummary = store.get(number);


        if (blockSummary != null) {

          blockListener.onBlock(blockSummary);

          if (number % 10000 == 0) {
            logger.info("Replayed until number = {}", number);
          }

          number++;
        }

      } while (blockSummary != null);

      logger.info("Replay complete, last number = {}", number - 1);

      System.exit(0);

    } catch (IOException e) {
      logger.error("Failure", e);
      System.exit(1);
    }



  }

}

