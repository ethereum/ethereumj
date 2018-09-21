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
import org.ethereum.util.blockchain.EtherUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void testConstantinopleBlock() {
        final int byzStart = 50;
        final int cnstStart = 60;

        GenesisConfig genesisConfig = new GenesisConfig();
        genesisConfig.constantinopleBlock = cnstStart;

        JsonNetConfig config = new JsonNetConfig(genesisConfig);
        assertBlockchainConfigExistsAt(config, cnstStart, ConstantinopleConfig.class);

        BlockchainConfig blockchainConfig = config.getConfigForBlock(cnstStart);
        assertEquals("Default chainId must be '1'", new Integer(1), blockchainConfig.getChainId());
        assertEquals("Reward should be 2 ETH", EtherUtil.convert(2, EtherUtil.Unit.ETHER), blockchainConfig.getConstants().getBLOCK_REWARD());
        assertTrue("EIP-1014 skinny CREATE2 should be activated among others", blockchainConfig.eip1014());

        genesisConfig.chainId = 99;

        config = new JsonNetConfig(genesisConfig);
        blockchainConfig = config.getConfigForBlock(cnstStart);
        assertEquals("chainId should be copied from genesis config", new Integer(99), blockchainConfig.getChainId());

        assertEquals("Default Frontier reward is 5 ETH", EtherUtil.convert(5, EtherUtil.Unit.ETHER),
                config.getConfigForBlock(byzStart).getConstants().getBLOCK_REWARD());
        genesisConfig.byzantiumBlock = byzStart;
        config = new JsonNetConfig(genesisConfig);  // Respawn so we have Byzantium on byzStart instead of Frontier
        assertEquals("Reward should be 3 ETH in Byzantium", EtherUtil.convert(3, EtherUtil.Unit.ETHER),
                config.getConfigForBlock(byzStart).getConstants().getBLOCK_REWARD());
        assertEquals("Reward should be changed to 2 ETH in Constantinople", EtherUtil.convert(2, EtherUtil.Unit.ETHER),
                config.getConfigForBlock(cnstStart).getConstants().getBLOCK_REWARD());
    }

    private <T extends BlockchainConfig> void assertBlockchainConfigExistsAt(BlockchainNetConfig netConfig, long blockNumber, Class<T> configType) {
        BlockchainConfig block = netConfig.getConfigForBlock(blockNumber);
        if (!configType.isAssignableFrom(block.getClass())) {
            fail(block.getClass().getName() + " is not of type " + configType);
        }
    }
}