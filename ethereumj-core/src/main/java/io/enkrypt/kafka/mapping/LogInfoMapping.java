package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.LogRecord;
import io.enkrypt.avro.common.Data20;
import io.enkrypt.avro.common.Data32;
import org.ethereum.vm.LogInfo;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class LogInfoMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(Context ctx, Class<A> from, Class<B> to, A value) {
    checkArgument(LogInfo.class == from);
    checkArgument(LogRecord.class == to);

    final LogInfo logInfo = (LogInfo) value;

    final LogRecord record = LogRecord.newBuilder()
      .setAddress(new Data20(logInfo.getAddress().clone()))
      .setData(wrap(logInfo.getData().clone()))
      .setTopics(
        logInfo.getTopics()
          .stream()
          .map(d -> new Data32(d.getData().clone()))
          .collect(Collectors.toList())
      ).build();

    return to.cast(record);
  }
}
