package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;

import java.net.InetSocketAddress;
import java.util.HashMap;
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
import org.ethereum.util.FastByteComparisons;

public class KafkaEthereumListener implements EthereumListener {

  private final Kafka kafka;
  private final Blockchain blockchain;
  private final SystemProperties config;

  private int numPendingTxs;
  private long lastBlockTimestampMs;

  private byte[] bestHash;

  private Map<String, Channel> peersMap;

  public KafkaEthereumListener(Kafka kafka, Blockchain blockchain, SystemProperties config) {
    this.kafka = kafka;
    this.blockchain = blockchain;
    this.config = config;

    init();
  }

  private void init() {

    numPendingTxs = 0;
    lastBlockTimestampMs = 0L;

    peersMap = new HashMap<>();
    bestHash = new byte[0];

    // find latest block timestamp remembering that the block timestamp is unix time, seconds since epoch

    final Block bestBlock = blockchain.getBestBlock();
    lastBlockTimestampMs = bestBlock == null ? 0L : bestBlock.getTimestamp() * 1000;
  }

  private void publishBestNumber(long number) {
    byte[] key = "best_number".getBytes();
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

    // when a new latest block is detected we publish the best known block number to the metadata topic

    if (!FastByteComparisons.equal(bestHash, status.getBestHash())) {

      long bestNumber = 0L;

      for (Channel entry : peersMap.values()) {
        final BlockIdentifier bestKnown = entry.getEthHandler().getBestKnownBlock();
        if (bestKnown != null) {
          bestNumber = Math.max(bestNumber, bestKnown.getNumber());
        }
      }

      if(bestNumber > 0) {
        this.publishBestNumber(bestNumber);
      }

      this.bestHash = status.getBestHash();
    }

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
    final InetSocketAddress inet = peer.getInetSocketAddress();
    peersMap.put(inet.toString(), peer);
  }

  @Override
  public void onPeerDisconnect(String host, long port) {
    final String key = String.format("%s:%d", host, port);
    peersMap.remove(key);
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

    // calculate processing time for the block, remembering that block timestamp is unix time, seconds since epoch
    final long timestampMs = blockSummary.getBlock().getTimestamp() * 1000;

    final long processingTimeMs = lastBlockTimestampMs == 0 ? 0 : timestampMs - lastBlockTimestampMs;
    lastBlockTimestampMs = timestampMs;

    // set num pending transactions and processing time

    blockSummary.getStatistics()
      .setNumPendingTxs(numPendingTxs)
      .setProcessingTimeMs(processingTimeMs);

    // Send block to kafka

    final Block block = blockSummary.getBlock();
    final long number = block.getNumber();

    final byte[] key = ByteUtil.longToBytes(number);
    final byte[] value = blockSummary.getEncoded();

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
