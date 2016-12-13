package org.ethereum.sync;

import org.ethereum.core.*;
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.BlockHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
@Component
@Scope("prototype")
public class BlockBodiesDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    @Autowired @Qualifier("headerSource")
    DataSourceArray<BlockHeader> headerStore;

    @Autowired
    DbFlushManager dbFlushManager;

    long t;

    SyncQueueIfc syncQueue;
    int curBlockIdx = 1;
    BigInteger curTotalDiff;

    Thread headersThread;

    @Autowired
    public BlockBodiesDownloader(BlockHeaderValidator headerValidator) {
        super(headerValidator);
    }

    public void startImporting() {
        Block genesis = blockStore.getChainBlockByNumber(0);
        syncQueue = new SyncQueueImpl(Collections.singletonList(genesis));
        curTotalDiff = genesis.getDifficultyBI();

        headersThread = new Thread("FastsyncHeadersFetchThread") {
            @Override
            public void run() {
                headerLoop();
            }
        };
        headersThread.start();

        setHeadersDownload(false);

        init(syncQueue, syncPool);
    }

    private void headerLoop() {
        while (curBlockIdx < headerStore.size() && !Thread.currentThread().isInterrupted()) {
            List<BlockHeaderWrapper> wrappers = new ArrayList<>();
            for (int i = 0; i < 10000 - syncQueue.getHeadersCount() && curBlockIdx < headerStore.size(); i++) {
                BlockHeader header = headerStore.get(curBlockIdx++);
                wrappers.add(new BlockHeaderWrapper(header, new byte[0]));
            }
            syncQueue.addHeaders(wrappers);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        headersDownloadComplete = true;
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!blockWrappers.isEmpty()) {

            for (BlockWrapper blockWrapper : blockWrappers) {
                curTotalDiff = curTotalDiff.add(blockWrapper.getBlock().getDifficultyBI());
                blockStore.saveBlock(blockWrapper.getBlock(), curTotalDiff, true);
            }
            dbFlushManager.commit();

            long c = System.currentTimeMillis();
            if (c - t > 5000) {
                t = c;
                logger.info("FastSync: downloaded blocks. Last: " + blockWrappers.get(blockWrappers.size() - 1).getBlock().getShortDescr());
            }
        }
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueSize() {
        return 0;
    }

    // TODO: receipts loading here

    @Override
    public void stop() {
        headersThread.interrupt();
        super.stop();
    }

    @Override
    protected void finishDownload() {
        stop();
    }
}
