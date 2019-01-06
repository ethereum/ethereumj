package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.LogRecord;
import io.enkrypt.avro.capture.TransactionReceiptRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data256;
import io.enkrypt.avro.common.Data32;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.vm.LogInfo;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class TransactionReceiptMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    checkArgument(TransactionReceipt.class == from);
    checkArgument(TransactionReceiptRecord.class == to);

    final ObjectMapping mappers = ctx.mappers();

    final TransactionReceipt r = (TransactionReceipt) value;
    final Transaction tx = r.getTransaction();

    final Data32 blockHash = ctx.get("blockHash", Data32.class);
    final Integer index = ctx.get("index", Integer.class);

    TransactionReceiptRecord.Builder builder = TransactionReceiptRecord.newBuilder()
      .setBlockHash(blockHash)
      .setTransactionIndex(index)
      .setTransactionHash(new Data32(tx.getHash().clone()))
      .setCumulativeGasUsed(wrap(r.getCumulativeGas().clone()))
      .setGasUsed(wrap(r.getGasUsed().clone()))
      .setLogsBloom(new Data256(r.getBloomFilter().getData().clone()))
      .setRaw(wrap(r.getEncoded().clone()))
      .setError(r.getError());

    if(tx.getContractAddress() != null) builder.setContractAddress(new Data20(tx.getContractAddress().clone()));
    if(r.getPostTxState() != null) builder.setStatus(wrap(r.getPostTxState().clone()));

    builder.setLogs(
      r.getLogInfoList()
      .stream()
        .map(l -> mappers.convert(ctx, LogInfo.class, LogRecord.class, l))
      .collect(Collectors.toList())
    );

    return to.cast(builder.build());
  }
}
