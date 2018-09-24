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

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Source;
import org.ethereum.sharding.domain.Validator;

/**
 * An interface to a validator set.
 *
 * <p>
 *     Actually, validator set has a list structure with size and item indexes.
 *     Term <i>set</i> is used to be aligned with the spec.
 *
 * <p>
 *     Major additions to the regular list functionality is {@code #getHash} method
 *     which returns a hash of the whole set. Hash calculation is implementation dependent.
 *
 * @author Mikhail Kalinin
 * @since 04.09.2018
 */
public interface ValidatorSet extends Source<Integer, Validator> {

    /**
     * A hash of empty validator set
     */
    byte[] EMPTY_HASH = HashUtil.EMPTY_TRIE_HASH;

    /**
     * Returns validator with specified index.
     *
     * @throws IndexOutOfBoundsException if validator with given index does not exist
     */
    Validator get(Integer index);

    /**
     * Replaces old validator entry with a new one.
     *
     * @param index an index of entry that is replacing
     * @param validator new validator structure
     * @throws IndexOutOfBoundsException if validator with given index does not exist
     */
    void put(Integer index, Validator validator);

    /**
     * Delete operation is not intrinsic to validator set.
     */
    default void delete(Integer key) {}

    /**
     * Adds given validator to the end of the <strike>list</strike> set.
     *
     * @return index that is assigned to the new validator
     */
    int add(Validator validator);

    /**
     * Returns a size of the <strike>list</strike> set.
     */
    int size();

    /**
     * Returns a hash of validator set.
     * The hash must uniquely identify each sample of validator set.
     */
    byte[] getHash();

    /**
     * Returns a sample of validator set that corresponds to specified hash.
     */
    ValidatorSet getSnapshotTo(byte[] hash);

    /**
     * Returns indices array of active validators.
     */
    int[] getActiveIndices();
}
