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

/**
 * Beacon chain block proposer.
 *
 * <p>
 *     Is responsible only for creating new block.
 *     Task scheduling is provided by {@link ValidatorService}
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
     * It is assumed that blocks which are distant from canonical chain head by this number or further
     * can't be affected by reorg in the future. Thus, proved mainChainRef should start from that distance.
     */
    long REORG_SAFE_DISTANCE = 32;

    /**
     * Creates new block on top of the beacon chain head.
     *
     * @param slotNumber number of the slot that block does belong to.
     * @param pubKey public key of the validator that the block will be created by
     * @return newly created block
     */
    Beacon createNewBlock(long slotNumber, byte[] pubKey);
}
