package io.enkrypt.kafka.listener;

import io.enkrypt.avro.capture.BlockRecord;
import io.enkrypt.avro.capture.PremineBalanceRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.kafka.db.BlockRecordStore;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.ByteBuffer.wrap;

public class BlockSummaryEthereumListener implements EthereumListener {

  private final SystemProperties config;
  private final BlockRecordStore blockRecordStore;
  private final KafkaBlockSummaryPublisher kafkaBlockSummaryPublisher;
  private final KafkaPendingTxsListener pendingTxsListener;
  private final ObjectMapper objectMapper;

  public BlockSummaryEthereumListener(SystemProperties config,
                                      BlockRecordStore blockRecordStore,
                                      KafkaBlockSummaryPublisher kafkaBlockSummaryPublisher,
                                      KafkaPendingTxsListener pendingTxsListener,
                                      ObjectMapper objectMapper) {
    this.config = config;
    this.blockRecordStore = blockRecordStore;
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

    final BlockRecord record = toRecord(blockSummary);

    // persist to store for replay later
    try {
      blockRecordStore.put(number, record);
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

  private BlockRecord toRecord(BlockSummary blockSummary) {

    final Block block = blockSummary.getBlock();

    final BlockRecord.Builder builder = objectMapper.convert(null, BlockSummary.class, BlockRecord.Builder.class, blockSummary);

    builder.setNumPendingTxs(pendingTxsListener.getNumPendingTxs());

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
            .setAddress(new Data20(account))
            .setBalance(wrap(accountState.getBalance().toByteArray()))
            .build()
        );

      }

      builder.setPremineBalances(premineBalances);
    }

    return builder.build();
  }

}
