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

import org.ethereum.sharding.processing.state.Committee;

/**
 * An interface of committee factory.
 *
 * <p>
 *     Used to produce new shards and committees array.
 *
 * <p>
 *     in: {@code [validators]} <br/>
 *     out: {@code [slot: [shardId, [validators]]]}
 *
 *
 * @see Committee
 *
 * @author Mikhail Kalinin
 * @since 14.09.2018
 */
public interface CommitteeFactory {

    /**
     * Creates new committees set.
     *
     * @param seed seed for random shuffling
     * @param validators array of active validator numbers
     * @param startShard shard id that first committee in resulting array will be assigned to
     *
     * @return shards and committee array for each slot of cycle
     *
     * @see BeaconConstants#CYCLE_LENGTH
     */
    Committee[][] create(byte[] seed, int[] validators, int startShard);
}
