package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.*;
import org.ethereum.core.*;
import org.ethereum.core.BlockHeader;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public class ObjectMapper implements ObjectMapping {

  private final Map<Key<?, ?>, ObjectMapping> objectMappings;

  public ObjectMapper() {
    this.objectMappings = new HashMap<>();

    add(LogInfo.class, LogRecord.class, new LogInfoMapping());
    add(TransactionReceipt.class, TransactionReceiptRecord.class, new TransactionReceiptMapping());
    add(Transaction.class, TransactionRecord.class, new TransactionMapping());
    add(InternalTransaction.class, InternalTransactionRecord.class, new InternalTransactionMapping());
    add(BlockHeader.class, BlockHeaderRecord.class, new BlockHeaderMapping());
    add(BlockSummary.class, BlockRecord.Builder.class, new BlockSummaryMapping());
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
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {

    if(value == null) return null;

    final Key<A, B> key = new Key<>(from, to);
    final ObjectMapping mapping = this.objectMappings.get(key);

    checkState(
      mapping != null,
      String.format("A mapping was not found for %s => %s", from.getCanonicalName(), to.getCanonicalName())
    );

    return mapping.convert(ctx == null ? new ContextImpl() : ctx, from, to, value);
  }

  private final class ContextImpl implements Context {

    private final Map<String, Object> map;

    private ContextImpl() {
      this.map = new HashMap<>();
    }

    private ContextImpl(Context other) {
      this();
      for (String key : other.keys()) {
        this.map.put(key, other.get(key));
      }
    }

    @Override
    public Context copy() {
      return new ContextImpl(this);
    }

    @Override
    public ObjectMapping mappers() {
      return ObjectMapper.this;
    }

    @Override
    public Set<String> keys() {
      return this.map.keySet();
    }

    @Override
    public void set(String key, Object value) {
      this.map.put(key, value);
    }

    @Override
    public Object get(String key) {
      return this.map.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
      return clazz.cast(this.map.get(key));
    }
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
