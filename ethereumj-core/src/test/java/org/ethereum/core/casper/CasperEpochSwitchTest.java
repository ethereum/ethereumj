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
package org.ethereum.core.casper;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.config.blockchain.ByzantiumConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.core.Block;
import org.ethereum.vm.GasCost;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

public class CasperEpochSwitchTest extends CasperBase {

    @Override
    BlockchainNetConfig config() {
        return CasperValidatorTest.CASPER_EASY_CONFIG;
    }

    @Test
    public void epochStartTest() throws Exception {
        // Init with light Genesis
        Resource casperGenesis = new ClassPathResource("/genesis/casper-test.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        loadBlockchain();

        BigInteger zeroEpoch = (BigInteger) casper.constCall("current_epoch")[0];
        assertEquals(0, zeroEpoch.longValue());

        for (int i = 0; i < 50; ++i) {
            Block block = bc.createBlock();
        }

        BigInteger firstEpoch = (BigInteger) casper.constCall("current_epoch")[0];
        assertEquals(1, firstEpoch.longValue());

        for (int i = 0; i < 50; ++i) {
            Block block = bc.createBlock();
        }

        // Epochs switches and they are finalized and justified because there no deposits yet [insta_finalize]
        BigInteger secondEpoch = (BigInteger) casper.constCall("current_epoch")[0];
        assertEquals(2, secondEpoch.longValue());

        BigInteger lastFinalized = (BigInteger) casper.constCall("last_finalized_epoch")[0];
        assertEquals(1, lastFinalized.longValue());

        BigInteger lastJustified = (BigInteger) casper.constCall("last_justified_epoch")[0];
        assertEquals(1, lastJustified.longValue());
    }
}
