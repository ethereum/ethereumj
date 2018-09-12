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
package org.ethereum.sharding.processing.state;

import javax.annotation.Nullable;

/**
 * An API to the beacon state repository.
 *
 * <p>
 *     The repository is a kv storage with a state hash treated as a key.
 *
 * @author Mikhail Kalinin
 * @since 15.08.2018
 */
public interface StateRepository {

    /**
     * Inserts new state into repository.
     */
    void insert(BeaconState state);

    /**
     * Returns beacon state with specific hash.
     */
    @Nullable
    BeaconState get(byte[] hash);

    /**
     * Creates valid empty state.
     */
    BeaconState getEmpty();

    /**
     * Flushes changes to underlying sources.
     */
    void commit();
}
