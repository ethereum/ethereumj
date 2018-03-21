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
package org.ethereum.config.net;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.blockchain.AbstractConfig;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class BaseNetConfigTest {
    @Test(expected = RuntimeException.class)
    public void addedBlockShouldHaveIncrementedBlockNumber() throws Exception {
        BlockchainConfig blockchainConfig = new TestBlockchainConfig();

        BaseNetConfig config = new BaseNetConfig();
        config.add(0, blockchainConfig);
        config.add(1, blockchainConfig);
        config.add(0, blockchainConfig);
    }

    @Test
    public void toStringShouldCaterForNulls() throws Exception {
        BaseNetConfig config = new BaseNetConfig();
        assertEquals("BaseNetConfig{blockNumbers=[], configs=[], count=0}", config.toString());

        BlockchainConfig blockchainConfig = new TestBlockchainConfig() {
            @Override
            public String toString() {
                return "TestBlockchainConfig";
            }
        };
        config.add(0, blockchainConfig);
        assertEquals("BaseNetConfig{blockNumbers=[0], configs=[TestBlockchainConfig], count=1}", config.toString());

        config.add(1, blockchainConfig);
        assertEquals("BaseNetConfig{blockNumbers=[0, 1], configs=[TestBlockchainConfig, TestBlockchainConfig], count=2}", config.toString());
    }

    private static class TestBlockchainConfig extends AbstractConfig {
        @Override
        public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
            return BigInteger.ZERO;
        }

        @Override
        public long getTransactionCost(Transaction tx) {
            return 0;
        }
    }
}