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
package org.ethereum.sharding.processing.validation;

import org.ethereum.sharding.crypto.Sign;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.processing.state.CrystallizedState;
import org.ethereum.sharding.util.BeaconUtils;
import org.ethereum.sharding.util.Bitfield;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.processing.validation.ValidationResult.Invalid;
import static org.ethereum.sharding.processing.validation.ValidationResult.Success;
import static org.ethereum.sharding.util.BeaconUtils.scanCommittees;

/**
 * Validates block attestations
 */
public class AttestationsValidator {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    BeaconStore store;
    Sign sign;
    List<ValidationRule<Data>> rules;

    public AttestationsValidator(BeaconStore store, Sign sign) {
        this.store = store;
        this.sign = sign;

        rules = new ArrayList<>();
        rules.add((block, data) -> {
            CrystallizedState crystallized = data.state.getCrystallizedState();
            List<AttestationRecord> attestationRecords = block.getAttestations();
            List<byte[]> recentBlockHashes = data.state.getActiveState().getRecentBlockHashes();

            for (AttestationRecord attestation : attestationRecords) {
                // Too early
                if (attestation.getSlot() > data.parent.getSlotNumber()) {
                    return Invalid;
                }

                // Too old
                if (attestation.getSlot() < Math.max(data.parent.getSlotNumber() - CYCLE_LENGTH + 1, 0)) {
                    return Invalid;
                }

                // Incorrect justified
                if (attestation.getJustifiedSlot() > crystallized.getFinality().getLastJustifiedSlot()) {
                    return Invalid;
                }

                Beacon justified = store.getByHash(attestation.getJustifiedBlockHash());
                if (justified == null ||
                        store.getCanonicalByNumber(justified.getSlotNumber()) != justified) {
                    return Invalid;
                }

                if (justified.getSlotNumber() != attestation.getJustifiedSlot()) {
                    return Invalid;
                }

                // Given an attestation and the block they were included in,
                // the list of hashes that were included in the signature
                long fromSlot = attestation.getSlot() - CYCLE_LENGTH + 1;
                long toSlot = attestation.getSlot() - attestation.getObliqueParentHashes().size();
                long sBack = block.getSlotNumber() - CYCLE_LENGTH * 2;
                List<byte[]> parentHashes = new ArrayList<>();
                for (int i = (int) (fromSlot - sBack); i <= toSlot - sBack; ++i) {
                    if (i < 0 || i >= CYCLE_LENGTH * 2) return Invalid;
                    parentHashes.add(recentBlockHashes.get(i));
                }
                parentHashes.addAll(attestation.getObliqueParentHashes());

                int slotOffset = (int) (attestation.getSlot() - crystallized.getDynasty().getStartSlot());
                List<Committee.Index> attestationIndices = scanCommittees(
                        crystallized.getDynasty().getCommittees(), slotOffset, attestation.getShardId());

                // Validate bitfield
                if (attestation.getAttesterBitfield().size() != Bitfield.calcLength(attestationIndices.size())) {
                    return Invalid;
                }

                // Confirm that there were no votes of nonexistent attesters
                int lastBit = attestationIndices.size();
                for (int i = lastBit - 1; i < Bitfield.calcLength(attestationIndices.size()) * Byte.SIZE; ++i) {
                    if (attestation.getAttesterBitfield().hasVoted(i)) {
                        return Invalid;
                    }
                }

                // Validate aggregate signature
                List<BigInteger> pubKeys = new ArrayList<>();
                for (Committee.Index index : attestationIndices) {
                    if (attestation.getAttesterBitfield().hasVoted(index.getValidatorIdx())) {
                        byte[] key = crystallized.getDynasty().getValidatorSet().get(index.getValidatorIdx()).getPubKey();
                        pubKeys.add(ByteUtil.bytesToBigInteger(key));
                    }
                }

                byte[] msgHash = BeaconUtils.calcMessageHash(attestation.getSlot(), parentHashes,
                        attestation.getShardId(), attestation.getShardBlockHash(), attestation.getJustifiedSlot());

                if (!sign.verify(attestation.getAggregateSig(), msgHash, sign.aggPubs(pubKeys))) {
                    return Invalid;
                }
            }

            return Success;
        });
    }

    public ValidationResult validateAndLog(Beacon block, Beacon parent, BeaconState state) {
        for (ValidationRule<Data> rule : rules) {
            ValidationResult res = rule.apply(block, new Data(parent, state));
            if (res != Success) {
                logger.info("Process attestations validation in block {}, status: {}", block.toString(), res);
                return res;
            }
        }

        return Success;
    }

    class Data {
        Beacon parent;
        BeaconState state;

        public Data(Beacon parent, BeaconState state) {
            this.parent = parent;
            this.state = state;
        }
    }
}
