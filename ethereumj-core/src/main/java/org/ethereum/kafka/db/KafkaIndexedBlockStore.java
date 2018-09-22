package org.ethereum.kafka.db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.ethereum.core.Block;
import org.ethereum.datasource.Source;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.kafka.Kafka;
import org.ethereum.kafka.models.BlockInfoList;
import org.ethereum.util.ByteUtil;

public class KafkaIndexedBlockStore extends IndexedBlockStore {

  private Kafka kafka;

  public void init(Source<byte[], byte[]> index, Source<byte[], byte[]> blocks, Kafka kafka) {
    init(index, blocks);
    this.kafka = kafka;
  }

  protected void addInternalBlock(Block block, BigInteger totalDifficulty, boolean mainChain) {
    List<BlockInfo> blockInfos = block.getNumber() >= index.size() ? null : index.get((int) block.getNumber());
    blockInfos = blockInfos == null ? new ArrayList<>() : blockInfos;

    BlockInfo blockInfo = new BlockInfo();
    blockInfo.setTotalDifficulty(totalDifficulty);
    blockInfo.setHash(block.getHash());
    blockInfo.setMainChain(mainChain); // FIXME:maybe here I should force reset main chain for all uncles on that level

    putBlockInfo(blockInfos, blockInfo);

    kafka.sendSync(Kafka.Producer.BLOCKS_INFO, block.getNumber(), new BlockInfoList(blockInfos));
    kafka.sendSync(Kafka.Producer.BLOCKS, ByteUtil.toHexString(block.getHash()), block);

    index.set((int) block.getNumber(), blockInfos);
    blocks.put(block.getHash(), block);
  }

  protected synchronized void setBlockInfoForLevel(long level, List<BlockInfo> infos) {
    kafka.sendSync(Kafka.Producer.BLOCKS_INFO, level, new BlockInfoList(infos));
    index.set((int) level, infos);
  }
}
