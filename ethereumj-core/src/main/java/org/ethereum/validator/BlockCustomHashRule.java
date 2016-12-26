/*
 * Copyright 2015, 2016 Ether.Camp Inc. (US)
 * This file is part of Ethereum Harmony.
 *
 * Ethereum Harmony is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ethereum Harmony is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ethereum Harmony.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public class BlockCustomHashRule extends BlockHeaderRule {

    public final byte[] blockHash;

    public BlockCustomHashRule(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public boolean validate(BlockHeader header) {
        errors.clear();

        if (!FastByteComparisons.equal(header.getHash(), blockHash)) {
            errors.add("Block " + header.getNumber() + " hash constraint violated. Expected:" +
                    Hex.toHexString(blockHash) + ", got: " + Hex.toHexString(header.getHash()));
            return false;
        }
        return true;
    }
}
