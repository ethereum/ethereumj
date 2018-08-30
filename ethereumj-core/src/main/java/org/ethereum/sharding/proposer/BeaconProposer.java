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
package org.ethereum.sharding.proposer;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;

/**
 * Beacon chain block proposer.
 *
 * <p>
 *     Is responsible only for creating new block.
 *     Task scheduling is provided by {@link ProposerService}
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public interface BeaconProposer {

    /**
     * Slot duration for the beacon chain
     */
    long SLOT_DURATION = 8 * 1000; // 8 seconds

    /**
     * Creates new block on top of the beacon chain head.
     *
     * @param slotNumber number of the slot that block does belong to.
     * @return newly created block
     */
    Beacon createNewBlock(long slotNumber);

    /**
     * Calculates minimal timestamp for specified slot number.
     *
     * @param slotNumber slot number
     * @return timestamp in milliseconds
     */
    long getTimestamp(long slotNumber);

    /**
     * Calculates a number of slot that given moment of time does fit to.
     * Uses {@link BeaconGenesis#timestamp} and {@link #SLOT_DURATION}
     *
     * @param timestamp timestamp in milliseconds
     * @return slot number
     */
    long getSlotNumber(long timestamp);
}
