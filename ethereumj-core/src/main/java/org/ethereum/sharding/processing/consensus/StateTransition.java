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

import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.domain.Beacon;

/**
 * A heart of beacon chain block processing.
 *
 * <p>
 *     Produces new beacon chain state by taking current state and applying a block to it.
 *
 * @see BeaconState
 * @see Beacon
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public interface StateTransition {

    /**
     * Produces new beacon chain state.
     *
     * @param block block that is the source of transition
     * @param to a state to make a transition from
     * @return new beacon state
     */
    BeaconState applyBlock(Beacon block, BeaconState to);
}
