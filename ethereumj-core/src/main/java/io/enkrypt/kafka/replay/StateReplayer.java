package io.enkrypt.kafka.replay;

import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockRecordStore;
import io.enkrypt.kafka.listener.KafkaBlockSummaryPublisher;
import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static java.lang.Long.parseLong;

public class StateReplayer {

  @Autowired
  KafkaBlockSummaryPublisher blockListener;

  @Autowired
  BlockRecordStore store;

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

    BlockRecord blockSummary;
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

