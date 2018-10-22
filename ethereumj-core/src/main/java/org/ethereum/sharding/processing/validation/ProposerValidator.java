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

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.processing.state.CrystallizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.processing.validation.ValidationResult.Invalid;
import static org.ethereum.sharding.processing.validation.ValidationResult.Success;

/**
 * Basic proposer attestation validation:
 *
 * Attestation from the proposer of the block should be included
 * along with the block in the network message object
 */
public class ProposerValidator {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    BeaconStore store;
    List<ValidationRule<Data>> rules;

    public ProposerValidator(BeaconStore store) {
        this.store = store;

        rules = new ArrayList<>();
        rules.add((block, data) -> {
            if (block.getSlotNumber() == 0) {
                return Success;
            }

            CrystallizedState crystallized = data.state.getCrystallizedState();

            Committee[][] committees = crystallized.getDynasty().getCommittees();

            int index = (int) data.parent.getSlotNumber() % committees.length;

            if (block.getAttestations().isEmpty()) {
                return Invalid;
            }

            AttestationRecord proposerAttestation = block.getAttestations().get(0);

            if (proposerAttestation.getSlot() != data.parent.getSlotNumber()) {
                return Invalid;
            }

            if (!proposerAttestation.getAttesterBitfield().hasVoted(index)) {
                return Invalid;
            }

            return Success;
        });
    }

    public ValidationResult validateAndLog(Beacon block, Beacon parent, BeaconState state) {
        for (ValidationRule<Data> rule : rules) {
            ValidationResult res = rule.apply(block, new Data(parent, state));
            if (res != Success) {
                logger.info("Process proposer validation of block {}, status: {}", block.toString(), res);
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
