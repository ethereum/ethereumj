package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.InternalTransactionRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data32;
import org.ethereum.vm.program.InternalTransaction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class InternalTransactionMapping implements ObjectMapping {

  @Override
  @SuppressWarnings("Duplicates")
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    checkArgument(InternalTransaction.class == from);
    checkArgument(InternalTransactionRecord.class == to);

    final InternalTransaction internalTx = (InternalTransaction) value;

    final Data32 blockHash = ctx.get("blockHash", Data32.class);
    final Integer txIndex = ctx.get("index", Integer.class);

    final InternalTransactionRecord.Builder builder = InternalTransactionRecord.newBuilder()
      .setBlockHash(blockHash)
      .setTransactionIndex(txIndex)
      .setTransactionIndex(internalTx.getIndex())
      .setDepth(internalTx.getDeep())
      .setRejected(internalTx.isRejected())
      .setNote(internalTx.getNote())
      .setNonce(wrap(internalTx.getNonce().clone()))
      .setFrom(new Data20(internalTx.getSender().clone()))
      .setValue(wrap(internalTx.getValue().clone()))
      .setGasPrice(wrap(internalTx.getGasPrice().clone()))
      .setGas(wrap(internalTx.getGasLimit().clone()))
      .setChainId(internalTx.getChainId())
      .setRaw(wrap(internalTx.getEncodedRaw().clone()));

    if(internalTx.getReceiveAddress() != null && internalTx.getReceiveAddress().length == 20) builder.setTo(new Data20(internalTx.getReceiveAddress().clone()));
    if(internalTx.getData() != null) builder.setInput(wrap(internalTx.getData().clone()));
    if(internalTx.getContractAddress() != null) builder.setCreates(new Data20(internalTx.getContractAddress().clone()));

    return to.cast(builder.build());
  }
}
