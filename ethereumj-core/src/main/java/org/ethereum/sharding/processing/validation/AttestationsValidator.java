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
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.BeaconUtils;
import org.ethereum.sharding.util.Bitfield;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.processing.validation.ValidationResult.InvalidAttestations;
import static org.ethereum.sharding.processing.validation.ValidationResult.Success;
import static org.ethereum.sharding.util.BeaconUtils.scanCommittees;

/**
 * Validates block attestations
 */
public class AttestationsValidator implements BeaconValidator {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    BeaconStore store;
    StateRepository repository;
    Sign sign;
    List<ValidationRule<Data>> rules;

    public AttestationsValidator(BeaconStore store, StateRepository repository, Sign sign) {
        this(store, repository, sign, new ArrayList<>());
        rules.add(ProposerAttestationRule);
        rules.add(CommonAttestationRule);
    }

    private AttestationsValidator(BeaconStore store, StateRepository repository,
                                  Sign sign, List<ValidationRule<Data>> rules) {
        this.store = store;
        this.repository = repository;
        this.sign = sign;
        this.rules = rules;
    }

    /**
     * Basic proposer attestation validation:
     *
     * Attestation from the proposer of the block should be included
     * along with the block in the network message object
     */
    static final ValidationRule<Data> ProposerAttestationRule = (block, data) -> {
        if (block.getSlotNumber() == 0) {
            return Success;
        }

        CrystallizedState crystallized = data.state.getCrystallizedState();

        Committee[][] committees = crystallized.getDynasty().getCommittees();

        int index = (int) data.parent.getSlotNumber() % committees.length;

        if (block.getAttestations().isEmpty()) {
            return InvalidAttestations;
        }

        AttestationRecord proposerAttestation = block.getAttestations().get(0);

        if (proposerAttestation.getSlot() != data.parent.getSlotNumber()) {
            return InvalidAttestations;
        }
        if (!FastByteComparisons.equal(data.parent.getHash(), proposerAttestation.getShardBlockHash())) {
            return InvalidAttestations;
        }

        if (!proposerAttestation.getAttesterBitfield().hasVoted(index)) {
            return InvalidAttestations;
        }

        return Success;
    };

    static final ValidationRule<Data> CommonAttestationRule = (block, data) -> {
        CrystallizedState crystallized = data.state.getCrystallizedState();
        List<AttestationRecord> attestationRecords = block.getAttestations();
        List<byte[]> recentBlockHashes = data.state.getActiveState().getRecentBlockHashes();

        for (AttestationRecord attestation : attestationRecords) {
            // Too early
            if (attestation.getSlot() > data.parent.getSlotNumber()) {
                return InvalidAttestations;
            }

            // Too old
            if (attestation.getSlot() < Math.max(data.parent.getSlotNumber() - CYCLE_LENGTH + 1, 0)) {
                return InvalidAttestations;
            }

            // Incorrect justified
            if (attestation.getJustifiedSlot() > crystallized.getFinality().getLastJustifiedSlot()) {
                return InvalidAttestations;
            }

            Beacon justified = data.store.getByHash(attestation.getJustifiedBlockHash());
            if (justified == null ||
                    data.store.getCanonicalByNumber(justified.getSlotNumber()) != justified) {
                return InvalidAttestations;
            }

            if (justified.getSlotNumber() != attestation.getJustifiedSlot()) {
                return InvalidAttestations;
            }

            // Given an attestation and the block they were included in,
            // the list of hashes that were included in the signature
            long fromSlot = attestation.getSlot() - CYCLE_LENGTH + 1;
            long toSlot = attestation.getSlot() - attestation.getObliqueParentHashes().size();
            long sBack = block.getSlotNumber() - CYCLE_LENGTH * 2;
            List<byte[]> parentHashes = new ArrayList<>();
            for (int i = (int) (fromSlot - sBack); i <= toSlot - sBack; ++i) {
                if (i < 0 || i >= CYCLE_LENGTH * 2) return InvalidAttestations;
                parentHashes.add(recentBlockHashes.get(i));
            }
            parentHashes.addAll(attestation.getObliqueParentHashes());

            int slotOffset = (int) (attestation.getSlot() - crystallized.getDynasty().getStartSlot());
            List<Committee.Index> attestationIndices = scanCommittees(
                    crystallized.getDynasty().getCommittees(), slotOffset, attestation.getShardId());

            // Validate bitfield
            if (attestation.getAttesterBitfield().size() != Bitfield.calcLength(attestationIndices.size())) {
                return InvalidAttestations;
            }

            // Confirm that there were no votes of nonexistent attesters
            int lastBit = attestationIndices.size();
            for (int i = lastBit - 1; i < Bitfield.calcLength(attestationIndices.size()) * Byte.SIZE; ++i) {
                if (attestation.getAttesterBitfield().hasVoted(i)) {
                    return InvalidAttestations;
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

            if (!data.sign.verify(attestation.getAggregateSig(), msgHash, data.sign.aggPubs(pubKeys))) {
                return InvalidAttestations;
            }
        }

        return Success;
    };

    public ValidationResult validateAndLog(Beacon block) {
        Beacon parent = store.getByHash(block.getParentHash());
        assert parent != null;
        BeaconState state = repository.get(parent.getStateHash());
        assert state != null;

        for (ValidationRule<Data> rule : rules) {
            ValidationResult res = rule.apply(block, new Data(parent, state, store, sign));
            if (res != Success) {
                logger.info("Process attestations validation in block {}, status: {}", block.toString(), res);
                return res;
            }
        }

        return Success;
    }

    static class Data {
        Beacon parent;
        BeaconState state;
        Sign sign;
        BeaconStore store;

        public Data(Beacon parent, BeaconState state, BeaconStore store, Sign sign) {
            this.parent = parent;
            this.state = state;
            this.store = store;
            this.sign = sign;
        }
    }
}
