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
import org.ethereum.core.Genesis;
import org.ethereum.core.genesis.CasperStateInit;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.GasCost;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

public class CasperEpochSwitchTest extends CasperBase {

    class CasperEasyConfig extends BaseNetConfig {
        class CasperGasCost extends Eip150HFConfig.GasCostEip150HF {
            public int getEXP_BYTE_GAS()        {     return 10;     }      // before spurious dragon hard fork
        }

        private final GasCost NEW_GAS_COST = new CasperEasyConfig.CasperGasCost();

        private class CasperConfig extends ByzantiumConfig {
            private final Constants constants;
            CasperConfig(BlockchainConfig parent) {

                super(parent);
                constants = new ConstantsAdapter(super.getConstants()) {
                    private final BigInteger BLOCK_REWARD = new BigInteger("1000000000000000000"); // 1 ETH

                    private final BigInteger MINIMUM_DIFFICULTY = BigInteger.ONE;

                    @Override
                    public BigInteger getBLOCK_REWARD() {
                        return BLOCK_REWARD;
                    }

                    @Override
                    public BigInteger getMINIMUM_DIFFICULTY() {
                        return MINIMUM_DIFFICULTY;
                    }
                };
            }

            @Override
            public GasCost getGasCost() {
                return NEW_GAS_COST;
            }

            @Override
            public boolean eip161() {
                return false;
            }

            @Override
            public Constants getConstants() {
                return constants;
            }
        }

        public CasperEasyConfig() {
            add(0, new CasperConfig(new FrontierConfig()));
        }
    }

    @Override
    BlockchainNetConfig config() {
        return new CasperEasyConfig();
    }

    @Test
    public void epochStartTest() {
        // Init with Genesis
        final Genesis genesis = Genesis.getInstance(systemProperties);
        final Genesis modifiedGenesis = new Genesis(
                genesis.getParentHash(),
                genesis.getUnclesHash(),
                genesis.getCoinbase(),
                genesis.getLogBloom(),
                ByteUtil.longToBytes(1),
                genesis.getNumber(),
                ByteUtil.byteArrayToLong(genesis.getGasLimit()),
                genesis.getGasUsed(),
                genesis.getTimestamp(),
                genesis.getExtraData(),
                genesis.getMixHash(),
                genesis.getNonce()
        );
        modifiedGenesis.setPremine(genesis.getPremine());

        CasperStateInit casperStateInit = new CasperStateInit(modifiedGenesis, repository, blockchain, systemProperties);
        casperStateInit.initDB();

        casper.setInitTxs(casperStateInit.makeInitTxes().getValue());

        BigInteger zeroEpoch = (BigInteger) casper.constCall("get_current_epoch")[0];
        assertEquals(0, zeroEpoch.longValue());

        for (int i = 0; i < 50; ++i) {
            Block block = bc.createBlock();
        }

        BigInteger firstEpoch = (BigInteger) casper.constCall("get_current_epoch")[0];
        assertEquals(1, firstEpoch.longValue());

        for (int i = 0; i < 50; ++i) {
            Block block = bc.createBlock();
        }

        // Epochs switches and they are finalized and justified because there no deposits yet [insta_finalize]
        BigInteger secondEpoch = (BigInteger) casper.constCall("get_current_epoch")[0];
        assertEquals(2, secondEpoch.longValue());

        BigInteger lastFinalized = (BigInteger) casper.constCall("get_last_finalized_epoch")[0];
        assertEquals(1, lastFinalized.longValue());

        BigInteger lastJustified = (BigInteger) casper.constCall("get_last_justified_epoch")[0];
        assertEquals(1, lastJustified.longValue());
    }
}
