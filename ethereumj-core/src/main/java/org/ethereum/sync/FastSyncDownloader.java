package org.ethereum.sync;

import org.ethereum.core.BlockWrapper;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Lazy
public class FastSyncDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    int counter;
    long t;

    @Autowired
    public FastSyncDownloader(BlockHeaderValidator headerValidator) {
        super(headerValidator);
    }

    public void startImporting(byte[] fromHash) {
        SyncQueueReverseImpl syncQueueReverse = new SyncQueueReverseImpl(fromHash);
        init(syncQueueReverse, syncPool);
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (blockWrappers.isEmpty()) return;

        for (BlockWrapper blockWrapper : blockWrappers) {
            blockStore.saveBlock(blockWrapper.getBlock(), BigInteger.ZERO, true);
        }
        counter += blockWrappers.size();

        long c = System.currentTimeMillis();
        if (c - t > 5000) {
            t = c;
            logger.info("FastSync: downloaded " + counter + " blocks so far. Last: " + blockWrappers.get(0).getBlock().getShortDescr());
            blockStore.flush();
        }
    }

    // TODO: receipts loading here

    public int getDownloadedBlocksCount() {
        return counter;
    }

    @Override
    protected void downloadComplete() {
        blockStore.flush();
        blockStore.updateAllTotDifficulties();
    }
}
