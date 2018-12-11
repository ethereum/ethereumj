package io.enkrypt.kafka.db;

import io.enkrypt.avro.capture.BlockRecord;
import org.ethereum.datasource.DbSource;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.ethereum.util.ByteUtil.longToBytes;

public class BlockRecordStore {

  private final DbSource<byte[]> ds;

  public BlockRecordStore(DbSource ds) {
    this.ds = ds;
  }

  public void put(long number, BlockRecord summary) throws IOException {
    this.put(longToBytes(number), summary.toByteBuffer().array());
  }

  public void put(byte[] number, byte[] summary) {
    ds.put(number, summary);
  }

  public BlockRecord get(long key) throws IOException {
    return this.get(longToBytes(key));
  }

  public BlockRecord get(byte[] key) throws IOException {
    byte[] bytes = ds.get(key);
    return bytes == null ? null : BlockRecord.fromByteBuffer(ByteBuffer.wrap(bytes));
  }

}
