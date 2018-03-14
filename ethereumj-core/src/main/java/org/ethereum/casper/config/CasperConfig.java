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
package org.ethereum.casper.config;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.Source;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.PruneManager;
import org.ethereum.db.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(
        basePackages = "org.ethereum",
        excludeFilters = {@ComponentScan.Filter(NoAutoscan.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {CommonConfig.class, DefaultConfig.class})},
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {CasperBeanConfig.class})}
)
@Import(CasperBeanConfig.class)
public class CasperConfig {
    private static Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    ApplicationContext appCtx;

    @Autowired
    CasperBeanConfig beanConfig;

    @Autowired
    SystemProperties config;

    public CasperConfig() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
    }

    @Bean
    public BlockStore blockStore(){
        beanConfig.fastSyncCleanUp();
        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        Source<byte[], byte[]> block = beanConfig.cachedDbSource("block");
        Source<byte[], byte[]> index = beanConfig.cachedDbSource("index");
        indexedBlockStore.init(index, block);

        return indexedBlockStore;
    }

    @Bean(name = "finalizedBlocks")
    public Source<byte[], byte[]> finalizedBlockStore() {
        return beanConfig.cachedDbSource("finalized");
    }

    @Bean
    public TransactionStore transactionStore() {
        beanConfig.fastSyncCleanUp();
        return new TransactionStore(beanConfig.cachedDbSource("transactions"));
    }

    @Bean
    public PruneManager pruneManager() {
        if (config.databasePruneDepth() >= 0) {
            return new PruneManager((IndexedBlockStore) blockStore(), beanConfig.stateSource().getJournalSource(),
                    config.databasePruneDepth());
        } else {
            return new PruneManager(null, null, -1); // dummy
        }
    }
}
