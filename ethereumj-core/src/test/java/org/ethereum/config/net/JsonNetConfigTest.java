/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.config.net;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.blockchain.*;
import org.ethereum.core.genesis.GenesisConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonNetConfigTest {
    @Test
    public void testCreationBasedOnGenesis() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.eip155Block = 10;
        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 0, FrontierConfig.class);
        assertBlockchainConfigExistsAt(config, 10, Eip150HFConfig.class);
    }

    @Test
    public void testCreationBasedOnDaoForkAndEip150Blocks_noHardFork() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.daoForkBlock = 10;
        genesisConfig.eip150Block = 20;
        genesisConfig.daoForkSupport = false;
        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 0, FrontierConfig.class);
        assertBlockchainConfigExistsAt(config, 10, DaoNoHFConfig.class);
        assertBlockchainConfigExistsAt(config, 20, Eip150HFConfig.class);
    }

    @Test
    public void testCreationBasedOnDaoHardFork() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.daoForkBlock = 10;
        genesisConfig.daoForkSupport = true;
        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 0, FrontierConfig.class);
        assertBlockchainConfigExistsAt(config, 10, DaoHFConfig.class);
    }

    @Test
    public void testEip158WithoutEip155CreatesEip160HFConfig() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.eip158Block = 10;

        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 10, Eip160HFConfig.class);
    }

    @Test
    public void testEip155WithoutEip158CreatesEip160HFConfig() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.eip155Block = 10;

        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 10, Eip160HFConfig.class);
    }

    @Test
    public void testChainIdIsCorrectlySetOnEip160HFConfig() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.eip155Block = 10;

        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        BlockchainConfig eip160 = config.getConfigForBlock(10);
        assertEquals("Default chainId must be '1'", new Integer(1), eip160.getChainId());

        genesisConfig.chainId = 99;

        config = new JsonNetConfig(genesisConfig);
        eip160 = config.getConfigForBlock(10);
        assertEquals("chainId should be copied from genesis config", new Integer(99), eip160.getChainId());
    }

    @Test
    public void testEip155MustMatchEip158IfBothExist() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.eip155Block = 10;
        genesisConfig.eip158Block = 10;
        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 10, Eip160HFConfig.class);

        try {
            genesisConfig.eip158Block = 13;
            new JsonNetConfig(genesisConfig);
            fail("Must fail. EIP155 and EIP158 must have same blocks");
        } catch (RuntimeException e) {
            assertEquals("Unable to build config with different blocks for EIP155 (10) and EIP158 (13)", e.getMessage());
        }
    }

    @Test
    public void testByzantiumBlock() {
        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.byzantiumBlock = 50;

        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, 50, ByzantiumConfig.class);

        BlockchainConfig eip160 = config.getConfigForBlock(50);
        assertEquals("Default chainId must be '1'", new Integer(1), eip160.getChainId());

        genesisConfig.chainId = 99;

        config = new JsonNetConfig(genesisConfig);
        eip160 = config.getConfigForBlock(50);
        assertEquals("chainId should be copied from genesis config", new Integer(99), eip160.getChainId());
    }

    private <T extends BlockchainConfig> void assertBlockchainConfigExistsAt(BlockchainNetConfig netConfig, long blockNumber, Class<T> configType) {
        BlockchainConfig block = netConfig.getConfigForBlock(blockNumber);
        if (!configType.isAssignableFrom(block.getClass())) {
            fail(block.getClass().getName() + " is not of type " + configType);
        }
    }
}