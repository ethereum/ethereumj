package io.enkrypt.kafka.listener;

import io.enkrypt.avro.capture.*;
import io.enkrypt.kafka.db.BlockSummaryStore;
import io.enkrypt.kafka.mapping.ObjectMapper;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.ByteBuffer.wrap;

public class BlockSummaryEthereumListener implements EthereumListener {

  private final SystemProperties config;
  private final BlockSummaryStore blockSummaryStore;
  private final KafkaBlockSummaryPublisher kafkaBlockSummaryPublisher;
  private final KafkaPendingTxsListener pendingTxsListener;
  private final ObjectMapper objectMapper;

  public BlockSummaryEthereumListener(SystemProperties config,
                                      BlockSummaryStore blockSummaryStore,
                                      KafkaBlockSummaryPublisher kafkaBlockSummaryPublisher,
                                      KafkaPendingTxsListener pendingTxsListener,
                                      ObjectMapper objectMapper) {
    this.config = config;
    this.blockSummaryStore = blockSummaryStore;
    this.kafkaBlockSummaryPublisher = kafkaBlockSummaryPublisher;
    this.pendingTxsListener = pendingTxsListener;
    this.objectMapper = objectMapper;
  }

  @Override
  public void onNodeDiscovered(Node node) {

  }

  @Override
  public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {

  }

  @Override
  public void onEthStatusUpdated(Channel channel, StatusMessage status) {
  }

  @Override
  public void onRecvMessage(Channel channel, Message message) {

  }

  @Override
  public void onSendMessage(Channel channel, Message message) {

  }

  @Override
  public void onSyncDone(SyncState state) {

  }

  @Override
  public void onNoConnections() {

  }

  @Override
  public void onPeerAddedToSyncPool(Channel peer) {

  }

  @Override
  public void onPeerDisconnect(String host, long port) {

  }

  @Override
  public void onPendingTransactionsReceived(List<Transaction> transactions) {
  }

  @Override
  public void onPendingStateChanged(PendingState pendingState) {
    // deprecated in favour of onPendingTransactionUpdate
  }

  @Override
  public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {

  }

  @Override
  public void onBlock(BlockSummary blockSummary) {

    final Block block = blockSummary.getBlock();
    final long number = block.getNumber();

    final BlockSummaryRecord record = toRecord(blockSummary);

    // persist to store for replay later
    try {
      blockSummaryStore.put(number, record);
      kafkaBlockSummaryPublisher.onBlock(record);
    } catch (IOException e) {

      // TODO ensure we stop all processing

      throw new RuntimeException(e);
    }

  }

  @Override
  public void onVMTraceCreated(String transactionHash, String trace) {
  }

  @Override
  public void onTransactionExecuted(TransactionExecutionSummary summary) {
  }

  @Override
  public void trace(String output) {
  }

  private BlockSummaryRecord toRecord(BlockSummary blockSummary) {

    final Block block = blockSummary.getBlock();

    final BlockRecord.Builder blockBuilder = BlockRecord.newBuilder()
      .setHeader(objectMapper.convert(null, BlockHeader.class, BlockHeaderRecord.class, block.getHeader()))
      .setUncles(
        block.getUncleList().stream()
          .map(u -> objectMapper.convert(null, BlockHeader.class, BlockHeaderRecord.class, u))
          .collect(Collectors.toList())
      );

    final Map<ByteBuffer, Transaction> txnsByHash = block.getTransactionsList()
      .stream()
      .collect(Collectors.toMap(t -> wrap(t.getHash()), t -> t));

    final Map<ByteBuffer, TransactionExecutionSummary> execSummariesByHash = blockSummary.getSummaries()
      .stream()
      .collect(Collectors.toMap(s -> wrap(s.getTransactionHash()), s -> s));

    final Map<ByteBuffer, TransactionReceipt> receiptsByHash = blockSummary.getReceipts()
      .stream()
      .collect(Collectors.toMap(r -> wrap(r.getTransaction().getHash()), r -> r));


    blockBuilder.setTransactions(
      txnsByHash.entrySet().stream()
        .map(entry -> {

          final ByteBuffer key = entry.getKey();
          final Transaction tx = entry.getValue();

          final TransactionExecutionSummary execSummary = execSummariesByHash.get(key);
          final TransactionReceipt receipt = receiptsByHash.get(key);

          checkState(execSummary != null, "execSummary cannot be null");
          checkState(receipt != null, "receipt cannot be null");

          return TransactionReceiptRecord.newBuilder()
            .setTx(objectMapper.convert(null, Transaction.class, TransactionRecord.class, tx))
            .setPostTxState(wrap(receipt.getPostTxState()))
            .setCumulativeGas(wrap(receipt.getCumulativeGas()))
            .setBloomFilter(wrap(receipt.getBloomFilter().getData()))
            .setGasPrice(wrap(execSummary.getGasPrice().toByteArray()))
            .setGasLimit(wrap(execSummary.getGasLimit().toByteArray()))
            .setGasUsed(wrap(execSummary.getGasUsed().toByteArray()))
            .setGasLeftover(wrap(execSummary.getGasLeftover().toByteArray()))
            .setGasRefund(wrap(execSummary.getGasRefund().toByteArray()))
            .setResult(wrap(execSummary.getResult()))
            .setLogInfos(
              execSummary.getLogs().stream()
                .map(l -> objectMapper.convert(null, LogInfo.class, LogInfoRecord.class, l))
                .collect(Collectors.toList())
            ).setInternalTxs(
              execSummary.getInternalTransactions().stream()
                .map(t -> objectMapper.convert(null, InternalTransaction.class, InternalTransactionRecord.class, t))
                .collect(Collectors.toList())
            ).setDeletedAccounts(
              execSummary.getDeletedAccounts().stream()
                .map(a -> wrap(a.getData()))
                .collect(Collectors.toList())
            ).build();


        }).collect(Collectors.toList())
    );

    final BlockSummaryRecord.Builder builder = BlockSummaryRecord.newBuilder()
      .setReverse(false)
      .setTotalDifficulty(wrap(blockSummary.getTotalDifficulty().toByteArray()))
      .setBlockBuilder(blockBuilder)
      .setNumPendingTxs(pendingTxsListener.getNumPendingTxs())
      .setRewards(
        blockSummary.getRewards().entrySet().stream()
          .map(e -> BlockRewardRecord.newBuilder()
            .setAddress(wrap(e.getKey()))
            .setReward(wrap(e.getValue().toByteArray()))
            .build()
          ).collect(Collectors.toList())
      );

    if (block.isGenesis()) {

      final Genesis genesis = Genesis.getInstance(config);

      final Map<ByteArrayWrapper, Genesis.PremineAccount> premine = genesis.getPremine();
      final List<PremineBalanceRecord> premineBalances = new ArrayList<>(premine.size());

      for (Map.Entry<ByteArrayWrapper, Genesis.PremineAccount> entry : premine.entrySet()) {

        final byte[] account = entry.getKey().getData();
        final AccountState accountState = entry.getValue().accountState;

        premineBalances.add(
          PremineBalanceRecord
            .newBuilder()
            .setAddress(wrap(account))
            .setAmount(wrap(accountState.getBalance().toByteArray()))
            .build()
        );

      }

      builder.setPremineBalances(premineBalances);

    }

    return builder.build();
  }

}
