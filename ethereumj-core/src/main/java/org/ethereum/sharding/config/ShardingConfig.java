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
package org.ethereum.sharding.config;

import org.ethereum.config.DefaultConfig;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Repository;
import org.ethereum.db.BlockStore;
import org.ethereum.db.DbFlushManager;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.sharding.manager.ShardingWorldManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Mikhail Kalinin
 * @since 26.07.2018
 */
@Configuration
@Import(DefaultConfig.class)
@EnableTransactionManagement
@ComponentScan(
        basePackages = "org.ethereum",
        excludeFilters = { @ComponentScan.Filter(NoAutoscan.class),
                           @ComponentScan.Filter(value = WorldManager.class, type = FilterType.ASSIGNABLE_TYPE)})
public class ShardingConfig {

    @Autowired
    Blockchain blockchain;

    @Autowired
    BlockStore blockStore;

    @Autowired
    SystemProperties systemProperties;

    @Autowired
    Repository repository;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    DbFlushManager dbFlushManager;

    @Bean
    public WorldManager worldManager() {
        return new ShardingWorldManager(systemProperties, repository,
                ethereumListener, blockchain, blockStore, depositContractConfig(), dbFlushManager);
    }

    @Bean
    public DepositContractConfig depositContractConfig() {
        return DepositContractConfig.fromFile();
    }
}
