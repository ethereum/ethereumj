package org.ethereum.samples;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.IndexedBlockStore;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.07.2016.
 */
public class CheckFork {
    public static void main(String[] args) throws Exception {
        SystemProperties.getDefault().overrideParams("database.dir", "");
        KeyValueDataSource index = CommonConfig.getDefault().keyValueDataSource();
        index.setName("index");
        index.init();
        KeyValueDataSource blockDS = CommonConfig.getDefault().keyValueDataSource();
        blockDS.setName("block");
        blockDS.init();
        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(index, blockDS);

        for (int i = 1_919_990; i < 1_921_000; i++) {
            Block chainBlock = indexedBlockStore.getChainBlockByNumber(i);
            List<Block> blocks = indexedBlockStore.getBlocksByNumber(i);
            String s = chainBlock.getShortDescr() + " (";
            for (Block block : blocks) {
                if (!block.isEqual(chainBlock)) {
                    s += block.getShortDescr() + " ";
                }
            }
            System.out.println(s);
        }
    }
}
