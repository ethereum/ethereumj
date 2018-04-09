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
package org.ethereum.casper.config.net;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.config.blockchain.ByzantiumConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.vm.GasCost;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class CasperTestNetConfig extends BaseNetConfig {

    public static final int EPOCH_LENGTH = 50;
    public static final int WITHDRAWAL_DELAY = 5;
    public static final int DYNASTY_LOGOUT_DELAY = 5;
    public static final double BASE_INTEREST_FACTOR = 0.1;
    public static final double BASE_PENALTY_FACTOR = 0.0001;
    public static final int MIN_DEPOSIT_ETH = 1500;

    public final static ECKey NULL_SIGN_SENDER = ECKey.fromPrivate(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

    public static final CasperTestNetConfig INSTANCE = new CasperTestNetConfig();

    static class CasperGasCost extends Eip150HFConfig.GasCostEip150HF {
        public int getEXP_BYTE_GAS()        {     return 10;     }      // before spurious dragon hard fork
    }

    private static final GasCost NEW_GAS_COST = new CasperGasCost();

    private class CasperConfig extends ByzantiumConfig {
        private final Constants constants;
        public CasperConfig(BlockchainConfig parent) {

            super(parent);
            constants = new ConstantsAdapter(super.getConstants()) {
                private final BigInteger BLOCK_REWARD = new BigInteger("1000000000000000000"); // 1 ETH

                private final BigInteger MINIMUM_DIFFICULTY = BigInteger.valueOf(8192);

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

    public CasperTestNetConfig() {
        add(0, new CasperConfig(new FrontierConfig()));
    }
}
