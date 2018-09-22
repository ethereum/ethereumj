package org.ethereum.kafka.db;

import java.util.ArrayList;
import java.util.List;
import org.ethereum.core.TransactionInfo;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TransactionStore;
import org.ethereum.kafka.Kafka;
import org.ethereum.kafka.models.TransactionInfoList;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;

public class KafkaTransactionStore extends TransactionStore {

  private final Kafka kafka;

  public KafkaTransactionStore(Source<byte[], byte[]> src, Kafka kafka) {
    super(src);
    this.kafka = kafka;
  }

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
    kafka.sendSync(Kafka.Producer.TRANSACTIONS, ByteUtil.toHexString(txHash), new TransactionInfoList(existingInfos));
    put(txHash, existingInfos);

    return true;
  }
}
