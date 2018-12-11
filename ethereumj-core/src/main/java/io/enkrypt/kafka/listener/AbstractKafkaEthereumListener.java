package io.enkrypt.kafka.listener;

import org.ethereum.core.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;

import java.util.List;

public abstract class AbstractKafkaEthereumListener implements EthereumListener {


  @Override
  public void trace(String output) {

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
  public void onBlock(BlockSummary blockSummary) {
    onBlock(blockSummary, false);
  }

  @Override
  public void onBlock(BlockSummary blockSummary, boolean best) {

  }

  @Override
  public void onPeerDisconnect(String host, long port) {

  }

  @Override
  public void onPendingTransactionsReceived(List<Transaction> transactions) {

  }

  @Override
  public void onPendingStateChanged(PendingState pendingState) {

  }

  @Override
  public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {

  }

  @Override
  public void onSyncDone(SyncState state) {

  }

  @Override
  public void onNoConnections() {

  }

  @Override
  public void onVMTraceCreated(String transactionHash, String trace) {

  }

  @Override
  public void onTransactionExecuted(TransactionExecutionSummary summary) {

  }

  @Override
  public void onPeerAddedToSyncPool(Channel peer) {

  }
}
