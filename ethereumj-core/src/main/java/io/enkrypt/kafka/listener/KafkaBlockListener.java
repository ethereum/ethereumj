package io.enkrypt.kafka.listener;

import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.BlockSummaryRecord;
import io.enkrypt.kafka.Kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KafkaBlockListener implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger("kafka-listener");

  private final Kafka kafka;

  private final ConcurrentLinkedQueue<BlockSummaryRecord> queue;
  private final ArrayList<BlockSummaryRecord> batch;

  private final long intervalMs = 500;
  private final int batchSize = 512;

  private volatile boolean running = true;

  public KafkaBlockListener(Kafka kafka) {
    this.kafka = kafka;
    this.queue = new ConcurrentLinkedQueue<>();
    this.batch = new ArrayList<>(batchSize);
  }

  public void onBlock(BlockSummaryRecord record) {
    queue.add(record);
  }

  public void stop() {
    running = false;
  }

  public void run() {

    BlockSummaryRecord next;

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
          logger.debug("Sleeping {} ms", intervalMs);
          // nothing more to process so sleep for a while
          Thread.sleep(intervalMs);
        }

      } catch (Exception ex) {
        logger.error("Processing failure, stopping", ex);
        running = false;
      }

    }

    logger.info("Stopped");
  }

  private void publishBatch(List<BlockSummaryRecord> batch) {

    final KafkaProducer<Long, BlockSummaryRecord> producer = kafka.getBlockSummaryProducer();

    producer.beginTransaction();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    try {

      for (BlockSummaryRecord record : batch) {

        final BlockRecord block = record.getBlock();
        final long number = block.getHeader().getNumber();

        // publish block summary

        futures.add(producer.send(new ProducerRecord<>(Kafka.TOPIC_BLOCKS, number, record)));

        // special handling for genesis block


          // TODO handle genesis block

//          final Genesis genesis = Genesis.getInstance(config);
//          for (Map.Entry<ByteArrayWrapper, Genesis.PremineAccount> entry : genesis.getPremine().entrySet()) {
//
//            final byte[] account = entry.getKey().getData();
//            final io.enkrypt.kafka.models.AccountState state = io.enkrypt.kafka.models.AccountState.newBuilder(entry.getValue().accountState)
//              .build();
//
//            futures.add(producer.send(new ProducerRecord<>(Kafka.TOPIC_ACCOUNT_STATE, account, state)));
//
//          }

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
