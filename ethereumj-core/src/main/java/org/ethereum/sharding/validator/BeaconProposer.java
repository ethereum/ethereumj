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
import org.ethereum.sharding.processing.state.Committee;

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
     * Creates new block on top of the beacon chain head.
     *
     * @param in inputs for the new block
     * @param pubKey public key of the validator that the block will be created by
     * @return newly created block
     */
    Beacon createNewBlock(Input in, byte[] pubKey);

    class Input {
        long slotNumber;
        Committee.Index index;
        Beacon parent;
        BeaconState state;
        byte[] mainChainRef;

        public Input(long slotNumber, Committee.Index index, Beacon parent, BeaconState state, byte[] mainChainRef) {
            this.slotNumber = slotNumber;
            this.index = index;
            this.parent = parent;
            this.state = state;
            this.mainChainRef = mainChainRef;
        }

        public Input(long slotNumber, Committee.Index index, ValidatorService.ChainHead head, byte[] mainChainRef) {
            this.slotNumber = slotNumber;
            this.index = index;
            this.parent = head.block;
            this.state = head.state;
            this.mainChainRef = mainChainRef;
        }
    }
}
