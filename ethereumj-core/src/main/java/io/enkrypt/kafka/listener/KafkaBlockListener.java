package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.models.TokenTransfer;
import io.enkrypt.kafka.models.TokenTransferKey;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KafkaBlockListener extends AbstractKafkaEthereumListener implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger("kafka-listener");

  private final Kafka kafka;
  private final SystemProperties config;
  private final KafkaPendingTxsListener pendingTxnsListener;

  private final ConcurrentLinkedQueue<BlockSummary> queue;
  private final ArrayList<BlockSummary> batch;

  private final long intervalMs = 500;
  private final int batchSize = 512;

  private long lastBlockTimestampMs = 0L;

  private volatile boolean running = true;

  public KafkaBlockListener(Kafka kafka, SystemProperties config, KafkaPendingTxsListener pendingTxnsListener) {
    this.kafka = kafka;
    this.config = config;
    this.pendingTxnsListener = pendingTxnsListener;
    this.queue = new ConcurrentLinkedQueue<>();
    this.batch = new ArrayList<>(batchSize);
  }

  @Override
  public void onBlock(BlockSummary blockSummary, boolean best) {
    queue.add(blockSummary);
  }

  public void stop() {
    running = false;
  }

  public void run() {

    BlockSummary next;

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
          logger.info("Published {} block(s) and related state", count);
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

  private void publishBatch(List<BlockSummary> batch) {

    final KafkaProducer<Long, BlockSummary> producer = kafka.getTransactionalProducer();

    producer.beginTransaction();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    try {

      for (BlockSummary blockSummary : batch) {

        final Block block = blockSummary.getBlock();
        final long number = block.getNumber();

        // set num pending transactions and processing time

        blockSummary
          .getStatistics()
          .setNumPendingTxs(pendingTxnsListener.getNumPendingTxs())
          .setProcessingTimeMs(calculateProcessingTimeMs(block));

        // Send block to kafka

        futures.add(producer.send(new ProducerRecord<>(Kafka.TOPIC_BLOCKS, number, blockSummary)));

        //

        if (block.isGenesis()) {

          futures.addAll(this.publishGenesisAccountState());

        } else {

          for (TransactionExecutionSummary executionSummary : blockSummary.getSummaries()) {

            futures.addAll(publishAccountStates(executionSummary));
            futures.addAll(publishDeletedAccounts(executionSummary));
            futures.addAll(publishTokenTransfers(executionSummary));

          }

        }

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

  private List<Future<RecordMetadata>> publishGenesisAccountState() {

    final KafkaProducer<byte[], io.enkrypt.kafka.models.AccountState> producer = kafka.getTransactionalProducer();

    final Genesis genesis = Genesis.getInstance(config);

    Set<ByteArrayWrapper> premineKeys = genesis.getPremine().keySet();
    final List<Future<RecordMetadata>> futures = new ArrayList<>(premineKeys.size());

    for (ByteArrayWrapper key : premineKeys) {

      final Genesis.PremineAccount premineAccount = genesis.getPremine().get(key);
      final AccountState accountState = premineAccount.accountState;

      final ProducerRecord<byte[], io.enkrypt.kafka.models.AccountState> record = new ProducerRecord<>(
        Kafka.TOPIC_ACCOUNT_STATE,
        key.getData(),
        io.enkrypt.kafka.models.AccountState.newBuilder(accountState).build()
      );

      futures.add(producer.send(record));

    }

    return futures;
  }

  private List<Future<RecordMetadata>> publishAccountStates(TransactionExecutionSummary executionSummary) {

    final KafkaProducer<byte[], io.enkrypt.kafka.models.AccountState> producer = kafka.getTransactionalProducer();
    final Map<ByteArrayWrapper, io.enkrypt.kafka.models.AccountState> accountStates = executionSummary.getAccountStates();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    for (ByteArrayWrapper account : accountStates.keySet()) {

      final byte[] key = account.getData();
      final io.enkrypt.kafka.models.AccountState value = accountStates.get(account);
      final ProducerRecord<byte[], io.enkrypt.kafka.models.AccountState> record = new ProducerRecord<>(Kafka.TOPIC_ACCOUNT_STATE, key, value);

      futures.add(producer.send(record));
    }

    return futures;
  }

  private List<Future<RecordMetadata>> publishDeletedAccounts(TransactionExecutionSummary executionSummary) {

    final KafkaProducer<byte[], io.enkrypt.kafka.models.AccountState> producer = kafka.getTransactionalProducer();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    for (DataWord account : executionSummary.getDeletedAccounts()) {
      final byte[] key = account.getData();
      futures.add(producer.send(new ProducerRecord<>(kafka.TOPIC_ACCOUNT_STATE, key, null)));     // send tombstone
    }

    if(!futures.isEmpty()) {
      logger.info("Publishing {} account deletion(s)", futures.size());
    }

    return futures;
  }

  private List<Future<RecordMetadata>> publishTokenTransfers(TransactionExecutionSummary executionSummary) {
    final KafkaProducer<TokenTransferKey, TokenTransfer> producer = kafka.getTransactionalProducer();
    final Map<TokenTransferKey, TokenTransfer> tokenTransfers = executionSummary.getTokenTransfers();

    final List<Future<RecordMetadata>> futures = new ArrayList<>();

    for (TokenTransferKey key : tokenTransfers.keySet()) {
      final TokenTransfer value = tokenTransfers.get(key);
      futures.add(producer.send(new ProducerRecord<>(kafka.TOPIC_TOKEN_TRANSFERS, key, value)));
    }

    if(!futures.isEmpty()) {
      logger.info("Publishing {} token transfer(s)", futures.size());
    }

    return futures;
  }

  private long calculateProcessingTimeMs(Block block) {
    // calculate processing time for the block, remembering that block timestamp is unix time, seconds since epoch
    final long timestampMs = block.getTimestamp() * 1000;
    final long processingTimeMs = lastBlockTimestampMs == 0 ? 0 : timestampMs - lastBlockTimestampMs;
    lastBlockTimestampMs = timestampMs;
    return processingTimeMs;
  }

}
