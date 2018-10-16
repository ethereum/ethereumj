package io.enkrypt.kafka.db;

import org.ethereum.core.BlockSummary;
import org.ethereum.datasource.rocksdb.RocksDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.util.ByteUtil.longToBytes;

public class BlockSummaryStore {

  private static final Logger logger = LoggerFactory.getLogger("db");

  private final RocksDbDataSource ds;

  public BlockSummaryStore(RocksDbDataSource ds) {
    this.ds = ds;
  }

  public void init() {
    ds.setName("block-summaries");
    ds.init();
  }

  public void put(long number, BlockSummary summary) {
    this.put(longToBytes(number), summary.getEncoded());
  }

  public void put(byte[] number, byte[] summary) {
    ds.put(number, summary);
  }

  public BlockSummary get(long number) {
    return this.get(longToBytes(number));
  }

  public BlockSummary get(byte[] number) {
    byte[] bytes = ds.get(number);
    return bytes == null ? null : new BlockSummary(bytes);
  }

}
