package io.enkrypt.kafka.serialization;

import io.enkrypt.kafka.models.TokenTransferKey;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class EthereumKeySerializer implements Serializer<Object> {

  private final Serializer<String> stringSerializer = new StringSerializer();
  private final Serializer<Long> longSerializer = new LongSerializer();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    checkArgument(isKey, "isKey must be true");
  }

  @Override
  public byte[] serialize(String topic, Object data) {
    if(data == null) return null;

    final Class<?> type = data.getClass();

    // we can get away with == comparison within the same class loader
    // faster than .equals

    if(byte[].class == type) {
      return (byte[]) data;
    } else if(String.class == type) {
      return stringSerializer.serialize(topic, (String) data);
    } else if(Long.class == type) {
      return longSerializer.serialize(topic, (Long) data);
    } else if(TokenTransferKey.class == type){
      return ((TokenTransferKey) data).getEncoded();
    } else {
      throw new IllegalStateException("Unexpected type: " + type);
    }
  }

  @Override
  public void close() {

  }
}
