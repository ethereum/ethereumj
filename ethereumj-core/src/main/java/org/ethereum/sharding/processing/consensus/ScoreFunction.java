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

import java.math.BigInteger;

/**
 * A function that takes beacon chain head block and state that block produces and
 * returns a score of the chain that given block is a head of.
 *
 * <p>
 *     Basically, this function represents a fork choice rule.
 *
 * <p>
 *     When score is calculated it's stored as a score of the chain that block does belong to,
 *     and next it is compared to the score of canonical chain,
 *     if calculated score is greater than canonical score then beacon chain reorgs to the new block.
 *
 *     Check {@link org.ethereum.sharding.processing.db.BeaconStore#reorgTo(Beacon)} for details.
 *
 * @see Beacon
 * @see BeaconState
 *
 * @author Mikhail Kalinin
 * @since 15.08.2018
 */
public interface ScoreFunction {

    /**
     * Calculates chain score, accepts block and state.
     * The state is a result of applying {@link StateTransition} function to the block.
     */
    BigInteger apply(Beacon block, BeaconState state);
}
