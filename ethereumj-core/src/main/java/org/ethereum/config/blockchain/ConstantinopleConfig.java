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

/**
 * EIPs included in the Constantinople Hard Fork:
 * <ul>
 *     <li>145  - Bitwise shifting instructions in EVM</li>
 *     <li>1014 - Skinny CREATE2</li>
 *     <li>1052 - EXTCODEHASH opcode</li>
 *     <li>1087 - Net gas metering for SSTORE operations</li>
 * </ul>
 */
public class ConstantinopleConfig extends ByzantiumConfig {

    public ConstantinopleConfig(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public boolean eip1052() {
        return true;
    }

    @Override
    public boolean eip145() {
        return true;
    }
}
