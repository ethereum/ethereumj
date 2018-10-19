package io.enkrypt.kafka.db;

import org.ethereum.core.BlockSummary;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.rocksdb.RocksDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.util.ByteUtil.longToBytes;

public class BlockSummaryStore {

  private static final Logger logger = LoggerFactory.getLogger("db");

  private final DbSource<byte[]> ds;

  public BlockSummaryStore(DbSource ds) {
    this.ds = ds;
  }

  public void put(long number, BlockSummary summary) {
    this.put(longToBytes(number), summary.getEncoded());
  }

  public void put(byte[] number, byte[] summary) {
    ds.put(number, summary);
  }

  public BlockSummary get(long key) {
    return this.get(longToBytes(key));
  }

  public BlockSummary get(byte[] key) {
    byte[] bytes = ds.get(key);
    return bytes == null ? null : new BlockSummary(bytes);
  }

}
