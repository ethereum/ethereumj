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

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {

    checkArgument(Transaction.class == from);
    checkArgument(TransactionRecord.class == to);

    final Transaction tx = (Transaction) value;

    final ECKey.ECDSASignature signature = tx.getSignature();

    final TransactionRecord.Builder builder = TransactionRecord.newBuilder()
      .setHash(new Data32(tx.getHash()))
      .setNonce(wrap(tx.getNonce()))
      .setFrom(new Data20(tx.getSender()))
      .setValue(wrap(tx.getValue()))
      .setGasPrice(wrap(tx.getGasPrice()))
      .setGas(wrap(tx.getGasLimit()))
      .setV(new Data1(new byte[]{ signature.v }))
      .setR(wrap(signature.r.toByteArray()))
      .setS(wrap(signature.s.toByteArray()))
      .setChainId(tx.getChainId())
      .setRaw(wrap(tx.getEncodedRaw()));

    if(tx.getReceiveAddress() != null && tx.getReceiveAddress().length == 20) builder.setTo(new Data20(tx.getReceiveAddress()));
    if(tx.getData() != null) builder.setInput(wrap(tx.getData()));
    if(tx.getContractAddress() != null) builder.setCreates(new Data20(tx.getContractAddress()));

    return to.cast(builder.build());
  }
}
