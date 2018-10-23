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
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.Committee;

import java.util.List;

/**
 * Beacon chain block attester
 */
public interface BeaconAttester {

    /**
     * Attests block
     *
     * @param in           Input data
     * @param pubKey       Public key of the attestation validator
     * @return Attestation record
     */
    AttestationRecord attestBlock(Input in, byte[] pubKey);

    List<AttestationRecord> getAttestations(Long currentSlot, Beacon lastJustified);

    void addSingleAttestation(AttestationRecord attestationRecord);

    void purgeAttestations(AttestationRecord attestationRecord);

    /**
     * @param startSlot minimum slot number to preserve.
     */
    void removeOldSlots(long startSlot);

    class Input {
        long slotNumber;
        Committee.Index index;
        Beacon block;
        BeaconState state;

        public Input(long slotNumber, Committee.Index index, Beacon block, BeaconState state) {
            this.slotNumber = slotNumber;
            this.index = index;
            this.block = block;
            this.state = state;
        }

        public Input(long slotNumber, Committee.Index index, ValidatorService.ChainHead head) {
            this.slotNumber = slotNumber;
            this.index = index;
            this.block = head.block;
            this.state = head.state;
        }
    }
}
