package io.enkrypt.kafka.mapping;

import io.enkrypt.avro.capture.LogInfoRecord;
import io.enkrypt.avro.common.DataWord;
import org.ethereum.vm.LogInfo;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.ByteBuffer.wrap;

public class LogInfoMapping implements ObjectMapping {

  @Override
  public <A, B> B convert(ObjectMapping mappers, Class<A> from, Class<B> to, A value) {
    checkArgument(LogInfo.class == from);
    checkArgument(LogInfoRecord.class == to);

    final LogInfo logInfo = (LogInfo) value;

    final LogInfoRecord record = LogInfoRecord.newBuilder()
      .setAddress(wrap(logInfo.getAddress()))
      .setData(wrap(logInfo.getData()))
      .setTopics(
        logInfo.getTopics()
          .stream()
          .map(d -> new DataWord(d.getData()))
          .collect(Collectors.toList())
      ).build();

    return to.cast(record);
  }
}
