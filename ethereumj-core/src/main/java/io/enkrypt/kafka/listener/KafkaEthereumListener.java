package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;

import java.util.List;
import java.util.Map;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteUtil;

public class KafkaEthereumListener implements EthereumListener {

  private final Kafka kafka;
  private final Blockchain blockchain;
  private final SystemProperties config;

  private int numPendingTxs;

  public KafkaEthereumListener(Kafka kafka, Blockchain blockchain, SystemProperties config) {
    this.kafka = kafka;
    this.blockchain = blockchain;
    this.config = config;
    init();
  }

  private void init(){
    // TODO clear sync number pre-emptively on shut down
    publishSyncNumber(-1L);
    numPendingTxs = 0;
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
        numPendingTxs--;
        break;

      case NEW_PENDING:
        kafka.send(Kafka.Producer.PENDING_TRANSACTIONS, txHash, txReceipt.getTransaction().getEncoded());
        numPendingTxs++;
        break;

      default:
        // Do nothing
    }
  }

  @Override
  public void onBlock(BlockSummary blockSummary) {

    // set no pending transactions
    blockSummary.getStatistics().setNumPendingTxs(numPendingTxs);

    // Send blocks

    final Block block = blockSummary.getBlock();
    final long number = block.getNumber();

    final byte[] key = ByteUtil.longToBytes(number);
    final byte[] value = blockSummary.getEncoded();

    //
    kafka.send(Kafka.Producer.BLOCKS, key, value);

    // Send account balances

    if (!block.isGenesis()) {
      for (TransactionExecutionSummary summary : blockSummary.getSummaries()) {
        final Map<byte[], AccountState> touchedAccounts = summary.getTouchedAccounts();
        for (Map.Entry<byte[], AccountState> entry : touchedAccounts.entrySet()) {
          kafka.send(Kafka.Producer.ACCOUNT_STATE, entry.getKey(), entry.getValue().getEncoded());
        }
      }
    } else {
      final Genesis genesis = Genesis.getInstance(config);
      for (ByteArrayWrapper k : genesis.getPremine().keySet()) {
        final Genesis.PremineAccount premineAccount = genesis.getPremine().get(k);
        final AccountState accountState = premineAccount.accountState;
        kafka.send(Kafka.Producer.ACCOUNT_STATE, k.getData(), accountState.getEncoded());
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
