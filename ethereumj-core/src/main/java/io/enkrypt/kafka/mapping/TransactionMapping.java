package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.TransactionRecord;
import io.enkrypt.avro.common.Address;
import io.enkrypt.avro.common.Hash;
import io.enkrypt.avro.common.Nonce;
import org.ethereum.core.Transaction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class TransactionMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {
    checkArgument(Transaction.class == from);
    checkArgument(TransactionRecord.class == to);

    final Transaction tx = (Transaction) value;

    final TransactionRecord.Builder builder = TransactionRecord.newBuilder()
      .setNonce(wrap(tx.getNonce()))
      .setFrom(wrap(tx.getSender()))
      .setGasPrice(wrap(tx.getGasPrice()))
      .setGasLimit(wrap(tx.getGasLimit()));

    if(tx.getHash() != null) { builder.setHash(wrap(tx.getHash())); }
    if(tx.getReceiveAddress() != null) { builder.setTo(wrap(tx.getReceiveAddress())); }
    if(tx.getValue() != null) { builder.setValue(wrap(tx.getValue())); }
    if(tx.getData() != null) { builder.setData(wrap(tx.getData())); }
    if(tx.getChainId() != null) { builder.setChainId(tx.getChainId()); }

    return to.cast(builder.build());
  }
}
