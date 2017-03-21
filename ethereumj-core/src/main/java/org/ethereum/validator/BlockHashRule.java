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
package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 *  Checks if the block is from the right fork  
 */
public class BlockHashRule extends BlockHeaderRule {

    private final BlockchainNetConfig blockchainConfig;

    public BlockHashRule(SystemProperties config) {
        blockchainConfig = config.getBlockchainConfig();
    }

    @Override
    public boolean validate(BlockHeader header) {
        errors.clear();

        List<Pair<Long, byte[]>> hashes = blockchainConfig.getConfigForBlock(header.getNumber()).blockHashConstraints();

        for (Pair<Long, byte[]> hash : hashes) {
            if (header.getNumber() == hash.getLeft() &&
                    !FastByteComparisons.equal(header.getHash(), hash.getRight())) {
                errors.add("Block " + header.getNumber() + " hash constraint violated. Expected:" +
                        Hex.toHexString(hash.getRight()) + ", got: " + Hex.toHexString(header.getHash()));
                return false;
            }
        }

        return true;
    }
}
