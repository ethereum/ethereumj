package io.enkrypt.kafka.listener;

import io.enkrypt.kafka.Kafka;
import io.enkrypt.kafka.contract.ERC20Abi;
import io.enkrypt.kafka.contract.ERC721Abi;
import io.enkrypt.kafka.models.TokenTransfer;
import io.enkrypt.kafka.models.TokenTransferKey;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.ByteArraySet;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ethereum.util.ByteUtil.toHexString;

public class KafkaEthereumListener implements EthereumListener {

  private static final Logger LOGGER = LoggerFactory.getLogger("kafka");

  private final Kafka kafka;
  private final BlockchainImpl blockchain;
  private final SystemProperties config;

  private int numPendingTxs;
  private long lastBlockTimestampMs;

  private byte[] bestHash;

  private Map<String, Channel> peersMap;

  public KafkaEthereumListener(Kafka kafka, BlockchainImpl blockchain, SystemProperties config) {
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

      if (bestNumber > 0) {
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
  public void onBlock(BlockSummary summary) {

    try {

      // TODO make kafka publishing transactional

      final Block block = summary.getBlock();
      final long number = block.getNumber();

      // set num pending transactions and processing time

      summary
        .getStatistics()
        .setNumPendingTxs(numPendingTxs)
        .setProcessingTimeMs(calculateProcessingTimeMs(block));

      // Send block to kafka

      final byte[] key = ByteUtil.longToBytes(number);
      final byte[] value = summary.getEncoded();

      kafka.send(Kafka.Producer.BLOCKS, key, value);

      //

      if (block.isGenesis()) {

        final Genesis genesis = Genesis.getInstance(config);

        for (ByteArrayWrapper k : genesis.getPremine().keySet()) {
          final Genesis.PremineAccount premineAccount = genesis.getPremine().get(k);
          final AccountState accountState = premineAccount.accountState;
          kafka.send(
            Kafka.Producer.ACCOUNT_STATE,
            k.getData(),
            io.enkrypt.kafka.models.AccountState.newBuilder(accountState).build().getEncoded()
          );
        }

      } else {

        final Repository snapshot = blockchain
          .getRepository()
          .getSnapshotTo(block.getStateRoot());

        int txIdx = 0;

        for (TransactionExecutionSummary executionSummary : summary.getSummaries()) {

          this.publishAccountState(snapshot, block.getCoinbase(), true);
          this.publishAccountState(snapshot, executionSummary.getTouchedAccounts());
          this.publishContractInfo(snapshot, executionSummary.getTransaction());
          this.publishTokenTransfers(block, executionSummary.getTransaction(), txIdx, executionSummary.getLogs());

          txIdx++;
        }

      }

    } catch (Exception ex) {
      LOGGER.error("Processing failure", ex);
      throw new RuntimeException(ex);
    }

  }

  private void publishAccountState(Repository snapshot, ByteArraySet accounts) {
    for (byte[] account : accounts) {
      this.publishAccountState(snapshot, account, false);
    }
  }

  private void publishAccountState(Repository snapshot, byte[] account, boolean miner) {
    final AccountState state = snapshot.getAccountState(account);

    io.enkrypt.kafka.models.AccountState.Builder builder = io.enkrypt.kafka.models.AccountState
      .newBuilder(state);

    if(miner) builder.setMiner(true);

    kafka.send(Kafka.Producer.ACCOUNT_STATE, account, builder.build().getEncoded());
  }

  private void publishContractInfo(Repository snapshot, Transaction tx) {
    if (tx.isContractCreation()) {
      final byte[] contractAddress = tx.getContractAddress();

      final ContractDetails contractDetails = snapshot.getContractDetails(contractAddress);

      kafka.send(
        Kafka.Producer.ACCOUNT_STATE,
        contractAddress,
        io.enkrypt.kafka.models.AccountState.newBuilder()
          .setCreator(tx.getSender())
          .setCode(contractDetails.getCode())
          .build()
          .getEncoded()
      );

    }
  }

  private void publishTokenTransfers(Block block, Transaction tx, int txIdx, List<LogInfo> logInfos) {

    final ERC20Abi erc20 = ERC20Abi.getInstance();
    final ERC721Abi erc721 = ERC721Abi.getInstance();

    int logIdx = 0;

    for (LogInfo logInfo : logInfos) {

      final byte[] data = logInfo.getData();
      final List<DataWord> topics = logInfo.getTopics();

      // ERC20 and ERC721 Transfer events have the same event signature but differing numbers of indexed fields

      final int currentLogIdx = logIdx;

      erc20.matchEvent(topics)
        .filter(e -> ERC20Abi.EVENT_TRANSFER.equals(e.name))
        .ifPresent( e -> {

          byte[] contractAddress = tx.getReceiveAddress();

          final Optional<TokenTransfer.Builder> erc20Transfer = erc20.decodeTransferEvent(data, topics);

          final Optional<TokenTransfer.Builder> erc721Transfer = erc20Transfer.isPresent() ?
            Optional.empty() :
            erc721.decodeTransferEvent(data, topics);

          erc20Transfer
            .filter(builder -> !builder.getValue().equals(BigInteger.ZERO))   // filter out 0 transfers
            .ifPresent(builder -> {


              final BigInteger fromBalance = erc20.balanceOf(blockchain, block, contractAddress, builder.getFrom());
              final BigInteger toBalance = erc20.balanceOf(blockchain, block, contractAddress, builder.getTo());

              final TokenTransfer transfer = builder
                .setAddress(contractAddress)
                .setFromBalance(fromBalance)
                .setToBalance(toBalance)
                .build();

              // kafka key is the tx hash with the tx idx

              final TokenTransferKey key = new TokenTransferKey(tx.getHash(), txIdx, currentLogIdx);

              System.out.println("ERC20 transfer detected: " + transfer.toString());

              kafka.send(Kafka.Producer.TOKEN_TRANSFERS, key.getEncoded(), transfer.getEncoded());
            });

        });

      logIdx++;

    }

  }

  private long calculateProcessingTimeMs(Block block) {
    // calculate processing time for the block, remembering that block timestamp is unix time, seconds since epoch
    final long timestampMs = block.getTimestamp() * 1000;
    final long processingTimeMs = lastBlockTimestampMs == 0 ? 0 : timestampMs - lastBlockTimestampMs;
    lastBlockTimestampMs = timestampMs;
    return processingTimeMs;
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
