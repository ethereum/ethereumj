package org.ethereum.kafka.models;

import java.util.List;
import org.ethereum.db.IndexedBlockStore;

public class BlockInfoList {

  private final List<IndexedBlockStore.BlockInfo> infos;

  public BlockInfoList(List<IndexedBlockStore.BlockInfo> infos) {
    this.infos = infos;
  }

  public List<IndexedBlockStore.BlockInfo> getInfos() {
    return infos;
  }
}