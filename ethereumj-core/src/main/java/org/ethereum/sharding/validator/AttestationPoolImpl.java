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

import org.ethereum.sharding.crypto.Sign;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.pubsub.BeaconAttestationIncluded;
import org.ethereum.sharding.pubsub.BeaconBlockAttested;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.pubsub.StateRecalc;
import org.ethereum.sharding.util.Bitfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;

/**
 * Default implementation of {@link AttestationPool}.
 */
public class AttestationPoolImpl implements AttestationPool {

    private static final Logger logger = LoggerFactory.getLogger("attester");

    // Single attestations
    private final Map<Long, Set<AttestationRecord>> attestations = new HashMap<>();

    BeaconStore store;
    Sign sign;
    Publisher publisher;

    public AttestationPoolImpl(BeaconStore store, Sign sign, Publisher publisher) {
        this.store = store;
        this.sign = sign;
        this.publisher = publisher;
        init();
    }

    private void init() {
        publisher.subscribe(BeaconBlockAttested.class, (data) -> {
            addSingleAttestation(data.getAttestationRecord());
        });

        publisher.subscribe(BeaconAttestationIncluded.class, event -> {
            purgeAttestations(event.getAttestationRecord());
        });

        publisher.subscribe(StateRecalc.class, event -> {
            removeOldSlots(event.getSlot());
        });
    }

    @Override
    public List<AttestationRecord> getAttestations(Long currentSlot, Beacon lastJustified) {
        List<AttestationRecord> res = new ArrayList<>();
        // we should always have attestation for ourselves, so not a big deal
        int shardId = 0;
        byte[] canonicalHash = store.getCanonicalHead().getHash();
        // publish a (signed) attestation, [current_slot, h1, h2, ...h64] where h1, h2, ...h64 are the hashes
        // of the ancestors of the head up to 64 slots (if a chain has missing slots between heights a and b,
        // then use the hash of the block at height a for heights a + 1 ... b - 1 and the current_slot is
        // the current slot number
        for (long i = currentSlot; i > Math.max(0, currentSlot - CYCLE_LENGTH - 1); --i) {
            Set<AttestationRecord> slotAttestations = attestations.get(i) == null ? new HashSet<>() : attestations.get(i);
            if (!slotAttestations.isEmpty()) {
                shardId = slotAttestations.iterator().next().getShardId();
            }
            if (store.getCanonicalByNumber(i) != null) {
                canonicalHash = store.getCanonicalByNumber(i).getHash();
            }

            AttestationRecord mergedAttestation = new AttestationRecord(
                i,
                shardId,
                Collections.emptyList(),
                canonicalHash,
                Bitfield.orBitfield(slotAttestations.stream().map(AttestationRecord::getAttesterBitfield).collect(Collectors.toList())),
                lastJustified.getSlotNumber(),
                lastJustified.getHash(),
                sign.aggSigns(slotAttestations.stream().map(AttestationRecord::getAggregateSig).collect(Collectors.toList()))
            );
            res.add(mergedAttestation);
        }

        return res;
    }

    @Override
    public void addSingleAttestation(AttestationRecord attestationRecord) {
        if (attestationRecord.getAttesterBitfield().calcVotes() != 1) {
            throw new RuntimeException("Accepts only unmerged attestations");
        }

        if (attestations.containsKey(attestationRecord.getSlot())) {
            attestations.get(attestationRecord.getSlot()).add(attestationRecord);
        } else {
            attestations.put(attestationRecord.getSlot(), new HashSet<AttestationRecord>() {{
                add(attestationRecord);
            }});
        }
    }

    @Override
    public void purgeAttestations(AttestationRecord attestationRecord) {
        Set<AttestationRecord> slotAttestations = attestations.get(attestationRecord.getSlot());
        for (AttestationRecord record : slotAttestations) {
            if (Bitfield.orBitfield(attestationRecord.getAttesterBitfield(), record.getAttesterBitfield()) ==
                    attestationRecord.getAttesterBitfield()) {
                slotAttestations.remove(record);
                if (slotAttestations.isEmpty()) {
                    attestations.remove(record.getSlot());
                }
            }
        }
    }

    @Override
    public void removeOldSlots(long startSlot) {
        List<Long> toRemove = attestations.keySet().stream().filter((x) -> x < startSlot).collect(Collectors.toList());
        toRemove.forEach(attestations::remove);
    }
}
