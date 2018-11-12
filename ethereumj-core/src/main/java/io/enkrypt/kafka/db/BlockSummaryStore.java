package io.enkrypt.kafka.db;

import io.enkrypt.avro.capture.BlockSummaryRecord;
import org.ethereum.datasource.DbSource;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.ethereum.util.ByteUtil.longToBytes;

public class BlockSummaryStore {

  private final DbSource<byte[]> ds;

  public BlockSummaryStore(DbSource ds) {
    this.ds = ds;
  }

  public void put(long number, BlockSummaryRecord summary) throws IOException {
    this.put(longToBytes(number), summary.toByteBuffer().array());
  }

  public void put(byte[] number, byte[] summary) {
    ds.put(number, summary);
  }

  public BlockSummaryRecord get(long key) throws IOException {
    return this.get(longToBytes(key));
  }

  public BlockSummaryRecord get(byte[] key) throws IOException {
    byte[] bytes = ds.get(key);
    return bytes == null ? null : BlockSummaryRecord.fromByteBuffer(ByteBuffer.wrap(bytes));
  }

}
