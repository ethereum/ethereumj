package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.TransactionRecord;

import io.enkrypt.avro.common.Data1;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data32;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class TransactionMapping implements ObjectMapping {

  @SuppressWarnings("Duplicates")
  @Override
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    checkArgument(Transaction.class == from);
    checkArgument(TransactionRecord.class == to);

    final Transaction tx = (Transaction) value;

    final ECKey.ECDSASignature signature = tx.getSignature();

    final Long timestamp = ctx.get("timestamp", Long.class);
    final Data32 blockHash = ctx.get("blockHash", Data32.class);
    final Integer index = ctx.get("index", Integer.class);

    final TransactionRecord.Builder builder = TransactionRecord.newBuilder()
      .setTimestamp(timestamp)
      .setBlockHash(blockHash)
      .setTransactionIndex(index)
      .setHash(new Data32(tx.getHash()))
      .setNonce(wrap(tx.getNonce().clone()))
      .setFrom(new Data20(tx.getSender().clone()))
      .setValue(wrap(tx.getValue().clone()))
      .setGasPrice(wrap(tx.getGasPrice().clone()))
      .setGas(wrap(tx.getGasLimit().clone()))
      .setV(new Data1(new byte[]{ signature.v }))
      .setR(wrap(signature.r.toByteArray()))
      .setS(wrap(signature.s.toByteArray()))
      .setChainId(tx.getChainId())
      .setRaw(wrap(tx.getEncodedRaw().clone()));

    if(tx.getReceiveAddress() != null && tx.getReceiveAddress().length == 20) builder.setTo(new Data20(tx.getReceiveAddress().clone()));
    if(tx.getData() != null) builder.setInput(wrap(tx.getData().clone()));
    if(tx.getContractAddress() != null) builder.setCreates(new Data20(tx.getContractAddress().clone()));

    return to.cast(builder.build());
  }
}
