package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.InternalTransactionRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import io.enkrypt.avro.common.Data1;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data32;
import org.ethereum.crypto.ECKey;
import org.ethereum.vm.program.InternalTransaction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class InternalTransactionMapping implements ObjectMapping {

  @Override
  @SuppressWarnings("Duplicates")
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {

    checkArgument(InternalTransaction.class == from);
    checkArgument(InternalTransactionRecord.class == to);

    final InternalTransaction internalTx = (InternalTransaction) value;

    final InternalTransactionRecord.Builder builder = InternalTransactionRecord.newBuilder()
      .setTransactionIndex(internalTx.getIndex())
      .setDepth(internalTx.getDeep())
      .setRejected(internalTx.isRejected())
      .setNote(internalTx.getNote())
      .setNonce(wrap(internalTx.getNonce()))
      .setFrom(new Data20(internalTx.getSender()))
      .setValue(wrap(internalTx.getValue()))
      .setGasPrice(wrap(internalTx.getGasPrice()))
      .setGas(wrap(internalTx.getGasLimit()))
      .setChainId(internalTx.getChainId())
      .setRaw(wrap(internalTx.getEncodedRaw()));

    if(internalTx.getReceiveAddress() != null && internalTx.getReceiveAddress().length == 20) builder.setTo(new Data20(internalTx.getReceiveAddress()));
    if(internalTx.getData() != null) builder.setInput(wrap(internalTx.getData()));
    if(internalTx.getContractAddress() != null) builder.setCreates(new Data20(internalTx.getContractAddress()));

    return to.cast(builder.build());
  }
}
