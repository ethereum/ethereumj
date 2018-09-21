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
package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.blockchain.EtherUtil;

import java.math.BigInteger;

/**
 * EIPs included in the Constantinople Hard Fork:
 * <ul>
 *     <li>1234 - Constantinople Difficulty Bomb Delay and Block Reward Adjustment (2 ETH)</li>
 *     <li>145  - Bitwise shifting instructions in EVM</li>
 *     <li>1014 - Skinny CREATE2</li>
 *     <li>1052 - EXTCODEHASH opcode</li>
 *     <li>1283 - Net gas metering for SSTORE without dirty maps</li>
 * </ul>
 */
public class ConstantinopleConfig extends ByzantiumConfig {

    private final Constants constants;

    public ConstantinopleConfig(BlockchainConfig parent) {
        super(parent);
        constants = new ConstantsAdapter(super.getConstants()) {
            private final BigInteger BLOCK_REWARD = EtherUtil.convert(2, EtherUtil.Unit.ETHER);

            @Override
            public BigInteger getBLOCK_REWARD() {
                return BLOCK_REWARD;
            }
        };
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    protected int getExplosion(BlockHeader curBlock, BlockHeader parent) {
        int periodCount = (int) (Math.max(0, curBlock.getNumber() - 5_000_000) / getConstants().getEXP_DIFFICULTY_PERIOD());
        return periodCount - 2;
    }

    @Override
    public boolean eip1052() {
        return true;
    }

    @Override
    public boolean eip145() {
        return true;
    }

    @Override
    public boolean eip1283() {
        return true;
    }

    @Override
    public boolean eip1014() {
        return true;
    }
}
