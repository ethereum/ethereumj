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
package org.ethereum.config.blockchain;

import org.apache.commons.lang3.StringUtils;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

class BlockHeaderBuilder {
    private byte[] EMPTY_ARRAY = new byte[0];

    private byte[] parentHash;
    private long blockNumber;
    private BigInteger difficulty = BigInteger.ZERO;
    private long timestamp = 2L;
    private byte[] unclesHash = EMPTY_ARRAY;

    BlockHeaderBuilder(byte[] parentHash, long blockNumber, String difficulty) {
        this(parentHash, blockNumber, parse(difficulty));
    }

    BlockHeaderBuilder(byte[] parentHash, long blockNumber, int difficulty) {
        this(parentHash, blockNumber, BigInteger.valueOf(difficulty));
    }

    BlockHeaderBuilder(byte[] parentHash, long blockNumber, BigInteger difficulty) {
        this.parentHash = parentHash;
        this.blockNumber = blockNumber;
        this.difficulty = difficulty;
    }

    BlockHeaderBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    BlockHeaderBuilder withUncles(byte[] unclesHash) {
        this.unclesHash = unclesHash;
        return this;
    }

    BlockHeader build() {
        return new BlockHeader(parentHash, unclesHash, EMPTY_ARRAY, EMPTY_ARRAY,
                difficulty.toByteArray(), blockNumber, EMPTY_ARRAY, 1L, timestamp, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
    }

    public static BigInteger parse(String val) {
        return new BigInteger(StringUtils.replace(val, ",", ""));
    }

}
