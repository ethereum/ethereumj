package io.enkrypt.kafka.db;

import io.enkrypt.avro.Bytes20;
import io.enkrypt.avro.Bytes32;
import io.enkrypt.kafka.Kafka;
import org.apache.kafka.common.utils.Bytes;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TransactionStore;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.LogInfo;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KafkaTransactionStore extends TransactionStore {

  private final Kafka kafka;

  public KafkaTransactionStore(Source<byte[], byte[]> src, Kafka kafka) {
    super(src);
    this.kafka = kafka;
  }

  @Override
  public boolean put(TransactionInfo tx) {

    byte[] txHash = tx.getReceipt().getTransaction().getHash();

    List<TransactionInfo> existingInfos = null;
    synchronized (lastSavedTxHash) {
      if (lastSavedTxHash.put(new ByteArrayWrapper(txHash), object) != null || !lastSavedTxHash.isFull()) {
        existingInfos = get(txHash);
      }
    }
    // else it is highly unlikely that the transaction was included into another block
    // earlier than 5000 transactions before with regard to regular block import process

    if (existingInfos == null) {
      existingInfos = new ArrayList<>();
    } else {
      for (TransactionInfo info : existingInfos) {
        if (FastByteComparisons.equal(info.getBlockHash(), tx.getBlockHash())) {
          return false;
        }
      }
    }
    existingInfos.add(tx);

    if (tx.isPending()) {
      kafka.send(
        Kafka.Producer.PENDING_TRANSACTIONS,
        tx.getParentBlockHash(),
        toAvro(tx)
      );
    } else {
      kafka.sendSync(
        Kafka.Producer.TRANSACTIONS,
        tx.getBlockHash(),
        toAvro(tx)
      );
    }

    put(txHash, existingInfos);

    return true;

  }

  private io.enkrypt.avro.Transaction toAvro(TransactionInfo tx) {
    final TransactionReceipt r = tx.getReceipt();
    if (r == null) {
      return null;
    }

    io.enkrypt.avro.TransactionReceipt receipt = io.enkrypt.avro.TransactionReceipt.newBuilder()
      .setStatus(r.isValid() ? 1 : 0)
      .setPostTxState(ByteBuffer.wrap(r.getPostTxState()))
      .setCumulativeGas(ByteBuffer.wrap(r.getCumulativeGas()))
      .setGasUsed(ByteBuffer.wrap(r.getGasUsed()))
      .setBloomFilter(ByteBuffer.wrap(r.getBloomFilter().getData()))
      .setExecutionResult(ByteBuffer.wrap(r.getExecutionResult()))
      .setLogs(r.getLogInfoList().stream().map(this::toAvro).collect(Collectors.toList()))
      .build();

    final Transaction t = r.getTransaction();
    final ECKey.ECDSASignature signature = t.getSignature();

    io.enkrypt.avro.Transaction.Builder builder = io.enkrypt.avro.Transaction.newBuilder()
      .setHash(new Bytes32(t.getHash()))
      .setBlockHash(tx.getBlockHash() != null ? new Bytes32(tx.getBlockHash()) : null)
      .setTransactionIndex(tx.isPending() ? null : tx.getIndex())
      .setNonce(ByteBuffer.wrap(t.getNonce()))
      .setFrom(t.getSender() != null ? new Bytes20(t.getSender()) : null)
      .setTo(t.getReceiveAddress() != null ? new Bytes20(t.getReceiveAddress()) : null)
      .setValue(ByteBuffer.wrap(t.getValue()))
      .setGasPrice(ByteBuffer.wrap(t.getGasPrice()))
      .setGasLimit(ByteBuffer.wrap(t.getGasLimit()))
      .setData(t.getData() != null ? ByteBuffer.wrap(t.getData()) : null)
      .setReceipt(receipt)
      .setV(ByteBuffer.wrap(new byte[]{signature.v}))
      .setR(ByteBuffer.wrap(signature.r.toByteArray()))
      .setS(ByteBuffer.wrap(signature.s.toByteArray()));

    return builder.build();
  }

  private io.enkrypt.avro.LogInfo toAvro(LogInfo i) {
    if (i == null) {
      return null;
    }

    return io.enkrypt.avro.LogInfo.newBuilder()
      .setAddress(new Bytes20(i.getAddress()))
      .setTopics(i.getTopics().stream().map(d -> ByteBuffer.wrap(d.getData())).collect(Collectors.toList()))
      .setData(ByteBuffer.wrap(i.getData()))
      .build();
  }
}
