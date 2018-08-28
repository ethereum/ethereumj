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

import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * An API to beacon chain block storage.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public interface BeaconStore {

    /** A number of blocks that are kept in memory based cache */
    int BLOCKS_IN_MEM = 256;

    /**
     * Returns a head block of canonical chain.
     * @return head block or null if no block is found
     */
    @Nullable
    Beacon getCanonicalHead();

    /**
     * Returns a certain block that is a part of canonical chain.
     *
     * @param number block number
     * @return the block or null if no block with given number is found in canonical chain
     */
    @Nullable
    Beacon getCanonicalByNumber(long number);

    /**
     * Returns total score for the head block of canonical chain.
     *
     * @return 0 if there is no blocks in canonical chain, otherwise returns total score
     */
    BigInteger getCanonicalHeadScore();

    /**
     * Returns a block with given hash.
     */
    @Nullable
    Beacon getByHash(byte[] hash);

    /**
     * Returns true if block with given hash exists, otherwise returns false.
     */
    boolean exist(byte[] hash);

    /**
     * Returns total score of the chain up to given block inclusively.
     *
     * @param hash block hash
     * @return total chain score up to block or 0 if there is no such block
     */
    BigInteger getChainScore(byte[] hash);

    /**
     * Returns max block number that is presented in the store.
     * @return max number or -1 if store is empty
     */
    long getMaxNumber();

    /**
     * Saves block to the storage.
     *
     * @param block the block
     * @param chainScore total score of the chain that is calculated after given block has been imported
     * @param canonical whether consider the block as a part of canonical chain or not
     *
     * @throws RuntimeException if an attempt of storing canonical block in fork chain occurs
     */
    void save(Beacon block, BigInteger chainScore, boolean canonical);

    /**
     * Reorgs to the chain that given block does belong to.
     * Basically, does two things: first, it marks current canonical chain as a fork,
     * then it marks specified chain as a new canonical chain.
     *
     * @param block block that specifies new canonical chain;
     *              it's not necessary for that block to be a head of the chain,
     *              but it's highly recommended to use head block in reorgs to correctly specify desired chain.
     */
    void reorgTo(Beacon block);

    /**
     * Flushes storage changes to the underlying datasource.
     */
    void flush();
}
