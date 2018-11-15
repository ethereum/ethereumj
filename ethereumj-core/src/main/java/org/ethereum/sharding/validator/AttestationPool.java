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

import java.util.List;

/**
 * Attestation pool accumulates single attestations for unjustified part of
 * beacon chain, keeping attestations in its pool, so beacon proposer
 * knows which attestations to include in its block plus we know which
 * attestations are not included in any block and should be distributed broadly.
 */
public interface AttestationPool {

    /**
     * List of attestations which should be included in new proposal
     * @param currentSlot       Slot for proposal
     * @param lastJustified     Last justified chain
     * @return  list of attestations for slots before current, merged by slot
     */
    List<AttestationRecord> getAttestations(Long currentSlot, Beacon lastJustified);

    /**
     * Adds new attestation to the pool
     * @param attestationRecord     Attestation
     */
    void addSingleAttestation(AttestationRecord attestationRecord);

    /**
     * Purges exact attestation which is already included in state
     * @param attestationRecord     Attestation
     */
    void purgeAttestations(AttestationRecord attestationRecord);

    /**
     * Removes old slots, before last justified
     * @param startSlot minimum slot number to preserve.
     */
    void removeOldSlots(long startSlot);
}
