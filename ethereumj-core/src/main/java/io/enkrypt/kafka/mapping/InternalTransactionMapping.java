package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.InternalTransactionRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.ethereum.vm.program.InternalTransaction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class InternalTransactionMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {
    checkArgument(InternalTransaction.class == from);
    checkArgument(InternalTransactionRecord.class == to);

    final InternalTransaction internalTx = (InternalTransaction) value;

    final TransactionRecord.Builder baseBuilder = TransactionRecord.newBuilder()
      .setNonce(wrap(internalTx.getNonce()))
      .setFrom(wrap(internalTx.getSender()))
      .setGasPrice(wrap(internalTx.getGasPrice()))
      .setGasLimit(wrap(internalTx.getGasLimit()));

    if(internalTx.getHash() != null) { baseBuilder.setHash(wrap(internalTx.getHash())); }
    if(internalTx.getReceiveAddress() != null) { baseBuilder.setTo(wrap(internalTx.getReceiveAddress())); }
    if(internalTx.getValue() != null) { baseBuilder.setValue(wrap(internalTx.getValue())); }
    if(internalTx.getData() != null) { baseBuilder.setData(wrap(internalTx.getData())); }
    if(internalTx.getChainId() != null) { baseBuilder.setChainId(internalTx.getChainId()); }

    final InternalTransactionRecord record = InternalTransactionRecord.newBuilder()
      .setBase(baseBuilder.build())
      .setParentHash(internalTx.getParentHash() != null ? wrap(internalTx.getParentHash()) : null)
      .setDeep(internalTx.getDeep())
      .setIndex(internalTx.getIndex())
      .setRejected(internalTx.isRejected())
      .setNote(internalTx.getNote())
      .build();

    return to.cast(record);
  }
}
