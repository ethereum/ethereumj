package io.enkrypt.kafka.listener;

import io.enkrypt.avro.capture.BlockKeyRecord;
import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.kafka.Kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KafkaBlockSummaryPublisher implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger("kafka-listener");

  private static final long PUBLISH_INTERVAL_MS = 500;

  private final Kafka kafka;

  private final ConcurrentLinkedQueue<BlockRecord> queue;
  private final ArrayList<BlockRecord> batch;

  private final int batchSize = 512;

  private volatile boolean running = true;

  public KafkaBlockSummaryPublisher(Kafka kafka) {
    this.kafka = kafka;
    this.queue = new ConcurrentLinkedQueue<>();
    this.batch = new ArrayList<>(batchSize);
  }

  public void onBlock(BlockRecord record) {
    queue.add(record);
  }

  public void stop() {
    running = false;
  }

  public void run() {

    BlockRecord next;

    while (running) {
      try {

        // gather up the batch
        while (batch.size() < batchSize && (next = queue.poll()) != null) {
          batch.add(next);
        }

        final int count = batch.size();
        publishBatch(batch);
        batch.clear();

        if (count > 0) {
          logger.info("Published {} block(s)", count);
        }

        if (queue.isEmpty()) {
          logger.debug("Sleeping {} ms", PUBLISH_INTERVAL_MS);
          // nothing more to process so sleep for a while
          Thread.sleep(PUBLISH_INTERVAL_MS);
        }

      } catch (Exception ex) {
        logger.error("Processing failure, stopping", ex);
        running = false;
      }

    }

    logger.info("Stopped");
  }

  private void publishBatch(List<BlockRecord> batch) {

    final KafkaProducer<BlockKeyRecord, BlockRecord> producer = kafka.getBlockProducer();

    producer.beginTransaction();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    try {

      for (BlockRecord record : batch) {

        final ByteBuffer number = record.getHeader().getNumber();

        final BlockKeyRecord key = BlockKeyRecord.newBuilder()
          .setNumber(number)
          .build();

        // publish block summary

        futures.add(producer.send(new ProducerRecord<>(Kafka.TOPIC_BLOCKS, key, record)));

      }

      // wait on all the futures to complete and then commit

      for (Future<RecordMetadata> future : futures) {
        future.get(30, TimeUnit.SECONDS);
      }

      producer.commitTransaction();

    } catch (ProducerFencedException ex) {

      logger.error("Fenced exception", ex);
      throw new RuntimeException(ex);

    } catch (Exception ex) {

      producer.abortTransaction();
      logger.error("Fatal exception", ex);
      throw new RuntimeException(ex);

    }

  }

}
