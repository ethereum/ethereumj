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

import org.ethereum.casper.core.CasperPendingStateImpl;
import org.ethereum.casper.manager.CasperWorldManager;
import org.ethereum.casper.mine.CasperBlockMiner;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.PendingState;
import org.ethereum.casper.core.CasperBlockchain;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.mine.BlockMiner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class CasperBeanConfig extends CommonConfig {

    @Override
    @Bean
    public Blockchain blockchain() {
        return new CasperBlockchain(systemProperties());
    }

    @Bean
    @Override
    public WorldManager worldManager() {
        return new CasperWorldManager(systemProperties(), repository(), blockchain());
    }

    @Bean
    @Override
    public PendingState pendingState() {
        return new CasperPendingStateImpl(ethereumListener);
    }

    @Bean
    @Override
    public BlockMiner blockMiner() {
        return new CasperBlockMiner(systemProperties(), (CompositeEthereumListener) ethereumListener,
                blockchain(), pendingState());
    }

    @Bean
    @Override
    public SystemProperties systemProperties() {
        return CasperProperties.getDefault();
    }
}
