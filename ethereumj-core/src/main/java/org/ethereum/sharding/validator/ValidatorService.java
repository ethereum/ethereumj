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
package org.ethereum.sharding.validator;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.BeaconState;

/**
 * Service that is responsible for scheduling attestation and proposal tasks for the beacon chain validator.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 *
 * @see BeaconProposer
 */
public interface ValidatorService {

    /**
     * It is assumed that blocks which are distant from canonical chain head by this number or further
     * can't be affected by reorg in the future. Thus, proved mainChainRef should start from that distance.
     */
    long REORG_SAFE_DISTANCE = 32;

    /**
     * Initializes service.
     */
    default void init(ChainHead head, byte[]... pubKeys) {}

    /**
     * Submits a task to propose block with given slot number.
     * Thread safe.
     */
    default void propose(long slotNumber, int validatorIdx) {}

    /**
     * Submits a task to make an attestation in a given slot number.
     * Thread safe.
     */
    default void attest(long slotNumber, int validatorIdx) {}

    /**
     * Handy aggregator
     */
    class ChainHead {
        Beacon block;
        BeaconState state;

        public ChainHead(Beacon block, BeaconState state) {
            this.block = block;
            this.state = state;
        }
    }
}
