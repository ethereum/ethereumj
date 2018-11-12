package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.BlockHeaderRecord;
import io.enkrypt.avro.capture.InternalTransactionRecord;
import io.enkrypt.avro.capture.LogInfoRecord;
import io.enkrypt.avro.capture.TransactionRecord;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

public class ObjectMapper implements ObjectMapping {

  private final Map<Key<?, ?>, ObjectMapping> objectMappings;

  public ObjectMapper() {
    this.objectMappings = new HashMap<>();

    add(LogInfo.class, LogInfoRecord.class, new LogInfoMapping());
    add(Transaction.class, TransactionRecord.class, new TransactionMapping());
    add(InternalTransaction.class, InternalTransactionRecord.class, new InternalTransactionMapping());
    add(BlockHeader.class, BlockHeaderRecord.class, new BlockHeaderMapping());
  }

  public <A, B> ObjectMapper add(Class<A> from, Class<B> to, ObjectMapping mapping) {
    final Key<A, B> key = new Key<>(from, to);
    final ObjectMapping existingMapping = objectMappings.get(key);

    checkState(
      existingMapping == null,
      String.format("A mapping already exists for %s => %s", from.getCanonicalName(), to.getCanonicalName())
    );

    objectMappings.put(key, mapping);

    return this;
  }

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {
    if(value == null) return null;

    final Key<A, B> key = new Key<>(from, to);
    final ObjectMapping mapping = this.objectMappings.get(key);

    checkState(
      mapping != null,
      String.format("A mapping was not found for %s => %s", from.getCanonicalName(), to.getCanonicalName())
    );

    return mapping.convert(this, from, to, value);
  }

  private static final class Key<A, B> {

    public final Class<A> from;
    public final Class<B> to;

    private Key(Class<A> from, Class<B> to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key<?, ?> key = (Key<?, ?>) o;
      return Objects.equals(from, key.from) &&
        Objects.equals(to, key.to);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to);
    }
  }

}
