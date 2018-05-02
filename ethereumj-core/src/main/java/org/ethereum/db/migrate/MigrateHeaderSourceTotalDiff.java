/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db.migrate;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Blockchain;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Serializers;
import org.ethereum.db.BlockStore;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.HeaderStore;
import org.ethereum.sync.FastSyncManager;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @deprecated
 * TODO: Remove after a few versions (current: 1.7.3) or with DB version update
 * TODO: Make {@link FastSyncManager#removeHeadersDb(Logger)} private after removing
 * Also remove CommonConfig.headerSource with it as no more used
 *
 * - Repairs Headers DB after FastSync with skipHistory to be usable
 *    a) Updates incorrect total difficulty
 *    b) Migrates headers without index to usable scheme with index
 * - Removes headers DB otherwise as it's not needed
 */
@Deprecated
public class MigrateHeaderSourceTotalDiff implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private ApplicationContext ctx;

    private BlockStore blockStore;

    private Blockchain blockchain;

    private SystemProperties systemProperties;

    public MigrateHeaderSourceTotalDiff(ApplicationContext ctx, BlockStore blockStore,
                                        Blockchain blockchain, SystemProperties systemProperties) {
        this.ctx = ctx;
        this.blockStore = blockStore;
        this.blockchain = blockchain;
        this.systemProperties = systemProperties;
    }

    @Override
    public void run() {
        // checking whether we should do any kind of migration:
        if (!systemProperties.isFastSyncEnabled()) {
            return;
        }

        FastSyncManager fastSyncManager = ctx.getBean(FastSyncManager.class);
        if (fastSyncManager.isInProgress()|| blockStore.getBestBlock().getNumber() == 0) { // Fast sync is not over
            return;
        }

        logger.info("Fast Sync was used. Checking if migration required.");
        boolean dbRemoved = fastSyncManager.removeHeadersDb(logger);
        if (dbRemoved) {
            logger.info("Migration finished.");
            return;
        }
        if (blockStore.getBestBlock().getNumber() > 0 && blockStore.getChainBlockByNumber(1) == null) {
            // Maybe migration of headerStore and totalDifficulty is required?
            HeaderStore headerStore = ctx.getBean(HeaderStore.class);
            if (headerStore.getHeaderByNumber(1) != null) {
                logger.info("No migration required.");
                return;
            }

            logger.info("Migration required. Updating total difficulty.");
            logger.info("=== Don't stop or exit from application, migration could not be resumed ===");
            long firstFullBlockNum = blockStore.getMaxNumber();
            while (blockStore.getChainBlockByNumber(firstFullBlockNum - 1) != null) {
                --firstFullBlockNum;
            }
            Block firstFullBlock = blockStore.getChainBlockByNumber(firstFullBlockNum);
            DbSource<byte[]> headerDbSource = (DbSource<byte[]>) ctx.getBean("headerSource");
            ObjectDataSource<BlockHeader> objectDataSource = new ObjectDataSource<>(headerDbSource, Serializers.BlockHeaderSerializer, 0);
            DataSourceArray<BlockHeader> headerSource = new DataSourceArray<>(objectDataSource);
            BigInteger totalDifficulty = blockStore.getChainBlockByNumber(0).getDifficultyBI();
            for (int i = 1; i < firstFullBlockNum; ++i) {
                totalDifficulty = totalDifficulty.add(headerSource.get(i).getDifficultyBI());
            }
            blockStore.saveBlock(firstFullBlock, totalDifficulty.add(firstFullBlock.getDifficultyBI()), true);
            ((BlockchainImpl) blockchain).updateBlockTotDifficulties(firstFullBlockNum + 1);
            logger.info("Total difficulty updated");
            logger.info("Migrating headerStore");
            int maxHeaderNumber = headerSource.size() - 1;
            DbFlushManager flushManager = ctx.getBean(DbFlushManager.class);
            for (int i = 1; i < headerSource.size(); ++i) {
                BlockHeader curHeader = headerSource.get(i);
                headerStore.saveHeader(curHeader);
                headerSource.set(i, null);
                if (i % 10000 == 0) {
                    logger.info("#{} of {} headers moved. Flushing...", i, maxHeaderNumber);
                    flushManager.commit();
                    flushManager.flush();
                }
            }
            flushManager.commit();
            flushManager.flush();
            logger.info("headerStore migration finished. No more migrations required");
        } else {
            logger.info("No migration required.");
        }
    }
}
