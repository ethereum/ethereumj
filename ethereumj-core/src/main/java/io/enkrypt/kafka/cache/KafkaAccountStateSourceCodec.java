package io.enkrypt.kafka.cache;

import io.enkrypt.kafka.Kafka;
import org.apache.kafka.common.utils.Bytes;
import org.ethereum.core.AccountState;
import org.ethereum.datasource.Serializer;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.util.ByteUtil;

import java.nio.ByteBuffer;

public class KafkaAccountStateSourceCodec extends SourceCodec.BytesKey<AccountState, byte[]> {

  private final Kafka kafka;

  public KafkaAccountStateSourceCodec(Source<byte[], byte[]> src, Serializer<AccountState, byte[]> valSerializer, Kafka kafka) {
    super(src, valSerializer);
    this.kafka = kafka;
  }

  @Override
  public void put(byte[] bytes, AccountState val) {
    kafka.send(Kafka.Producer.ACCOUNT_STATE, bytes, toAvro(val));
    super.put(bytes, val);
  }

  @Override
  public void delete(byte[] bytes) {
    kafka.send(Kafka.Producer.ACCOUNT_STATE, bytes, null);
    super.delete(bytes);
  }

  private io.enkrypt.avro.AccountState toAvro(AccountState state) {
    return io.enkrypt.avro.AccountState.newBuilder()
      .setBalance(ByteBuffer.wrap(state.getBalance().toByteArray()))
      .setCodeHash(ByteBuffer.wrap(state.getCodeHash()))
      .setNonce(ByteBuffer.wrap(state.getNonce().toByteArray()))
      .setStateRoot(ByteBuffer.wrap(state.getStateRoot()))
      .build();
  }

}
