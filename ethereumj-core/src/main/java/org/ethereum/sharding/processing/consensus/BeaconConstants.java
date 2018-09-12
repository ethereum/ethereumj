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
package org.ethereum.sharding.processing.consensus;

import org.ethereum.util.blockchain.EtherUtil;

import java.math.BigInteger;

import static org.ethereum.util.blockchain.EtherUtil.convert;

/**
 * A global constants related to beacon chain consensus.
 *
 * @author Mikhail Kalinin
 * @since 06.09.2018
 */
public interface BeaconConstants {

    /**
     *  Number of slots in each cycle
     */
    long CYCLE_LENGTH = 64;

    /**
     * Number of shards
     */
    int SHARD_COUNT = 1024;

    /**
     * Validator registration deposit in wei
     */
    BigInteger DEPOSIT_WEI = convert(32, EtherUtil.Unit.ETHER);
}
