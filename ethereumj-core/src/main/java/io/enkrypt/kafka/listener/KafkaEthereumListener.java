package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.db.BlockSummaryStore;

import java.util.List;
import java.util.Map;

import org.ethereum.core.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;

public class KafkaEthereumListener implements EthereumListener {

  private final Kafka kafka;
  private final Blockchain blockchain;
  private final BlockSummaryStore blockSummaryStore;

  public KafkaEthereumListener(Kafka kafka, Blockchain blockchain, BlockSummaryStore blockSummaryStore) {
    this.kafka = kafka;
    this.blockchain = blockchain;
    this.blockSummaryStore = blockSummaryStore;
    init();
  }

  private void init(){
    // TODO clear sync number pre-emptively on shut down
    publishSyncNumber(-1L);
  }

  private void publishSyncNumber(long number) {
    byte[] key = "sync_number".getBytes();
    byte[] value = Long.toHexString(number).getBytes();
    kafka.send(Kafka.Producer.METADATA, key, value);
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
    switch (state) {

      case COMPLETE:
        publishSyncNumber(blockchain.getBestBlock().getNumber());
        break;

      default:
        throw new IllegalStateException("Unexpected state: " + state);
    }
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
    byte[] txHash = txReceipt.getTransaction().getHash();

    switch (state) {

      case DROPPED:
      case INCLUDED:
        // send a tombstone to 'remove' as any included transactions will be sent in the onBlock and
        // we no longer care about dropped transactions
        kafka.send(Kafka.Producer.PENDING_TRANSACTIONS, txHash, null);
        break;

      case NEW_PENDING:
        kafka.send(Kafka.Producer.PENDING_TRANSACTIONS, txHash, txReceipt.getTransaction().getEncoded());
        break;

      default:
        // Do nothing
    }
  }

  @Override
  public void onBlock(BlockSummary blockSummary) {

    // Send blocks

    final long number = blockSummary.getBlock().getNumber();
    final byte[] key = ByteUtil.longToBytes(number);
    final byte[] value = blockSummary.setCanonical(true).getEncoded();

    // retrieve previous version before persisting
    final BlockSummary prevSummary = blockSummaryStore.get(number);

    // locally persist first before sending to kafka. Can be used later for a replay
    blockSummaryStore.put(key, value);

    if(prevSummary != null) {
      final Block prevBlock = prevSummary.getBlock();
      Block currentBlock = blockSummary.getBlock();

      if(!FastByteComparisons.equal(prevBlock.getHash(), currentBlock.getHash())) {

        // we are re-publishing a previously seen block number with a new block
        // chain re-org is happening

        // re-send the prev version of the summary but mark it as no longer canonical
        kafka.send(Kafka.Producer.BLOCKS, key, prevSummary.setCanonical(false).getEncoded());
      }
    }

    kafka.send(Kafka.Producer.BLOCKS, key, value);

    // Send account balances
    for (TransactionExecutionSummary summary : blockSummary.getSummaries()) {
      final Map<byte[], AccountState> touchedAccounts = summary.getTouchedAccounts();
      for (Map.Entry<byte[], AccountState> entry : touchedAccounts.entrySet()) {
        kafka.send(Kafka.Producer.ACCOUNT_STATE, entry.getKey(), entry.getValue().getEncoded());
      }
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
}
