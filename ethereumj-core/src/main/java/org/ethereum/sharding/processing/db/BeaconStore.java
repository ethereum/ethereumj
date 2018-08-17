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
package org.ethereum.sharding.processing.db;

import org.ethereum.sharding.domain.Beacon;

import java.math.BigInteger;

/**
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public interface BeaconStore {

    int BLOCKS_IN_MEM = 256;

    Beacon getCanonicalHead();

    BigInteger getCanonicalHeadScore();

    Beacon getByHash(byte[] hash);

    boolean exist(byte[] hash);

    BigInteger getChainScore(byte[] hash);

    long getMaxNumber();

    void save(Beacon block, BigInteger chainScore, boolean canonical);

    void reorgTo(Beacon block);
}
