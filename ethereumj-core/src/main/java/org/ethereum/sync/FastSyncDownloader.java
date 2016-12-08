package org.ethereum.sync;

import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    DbFlushManager dbFlushManager;

    private BigInteger cummDiff = BigInteger.ZERO;

    int counter;
    long t;

    @Autowired
    public FastSyncDownloader(BlockHeaderValidator headerValidator) {
        super(headerValidator);
    }

    public void startImporting(Blockchain blockchain, long endNumber, byte[] baseDiff) {
        cummDiff = cummDiff.add(ByteUtil.bytesToBigInteger(baseDiff));
        SyncQueueImpl syncQueue = new SyncQueueImpl(blockchain, endNumber);
        init(syncQueue, syncPool);
    }

    @Override
    protected synchronized void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!blockWrappers.isEmpty()) {

            for (BlockWrapper blockWrapper : blockWrappers) {
                cummDiff = cummDiff.add(ByteUtil.bytesToBigInteger(blockWrapper.getBlock().getDifficulty()));
                blockStore.saveBlock(blockWrapper.getBlock(), cummDiff, true);
                dbFlushManager.commit();
            }
            counter += blockWrappers.size();

            long c = System.currentTimeMillis();
            if (c - t > 5000) {
                t = c;
                logger.info("FastSync: downloaded " + counter + " blocks so far. Last: " + blockWrappers.get(0).getBlock().getShortDescr());
                blockStore.flush();
            }
        }
    }

    @Override
    protected int getBlockQueueSize() {
        return 0;
    }

    // TODO: receipts loading here

    public int getDownloadedBlocksCount() {
        return counter;
    }

    @Override
    protected void finishDownload() {
        blockStore.flush();
    }
}
