package io.enkrypt.kafka.serialization;

import io.enkrypt.kafka.models.AccountState;
import io.enkrypt.kafka.models.TokenTransfer;
import org.apache.kafka.common.serialization.Serializer;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.Transaction;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class EthereumValueSerializer implements Serializer<Object> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    checkArgument(!isKey, "isKey must be false");
  }

  @Override
  public byte[] serialize(String topic, Object data) {
    if(data == null) return null;

    final Class<?> type = data.getClass();

    // we can get away with == comparison within the same class loader
    // faster than .equals

    if(BlockSummary.class == type) {
      return ((BlockSummary) data).getEncoded();
    } else if(AccountState.class == type) {
      return ((AccountState) data).getEncoded();
    } else if(Transaction.class == type) {
      return ((Transaction) data).getEncoded();
    } else if(TokenTransfer.class == type) {
      return ((TokenTransfer) data).getEncoded();
    } else {
      throw new IllegalStateException("Unexpected type: " + type);
    }

  }

  @Override
  public void close() {

  }
}
