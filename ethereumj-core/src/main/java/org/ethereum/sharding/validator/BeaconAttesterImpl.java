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

import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.Bitfield;
import org.ethereum.sharding.util.Sign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link BeaconAttester}.
 */
public class BeaconAttesterImpl implements BeaconAttester {

    private static final Logger logger = LoggerFactory.getLogger("attester");

    StateRepository repository;
    BeaconStore store;
    ValidatorConfig config;

    public BeaconAttesterImpl(StateRepository repository, BeaconStore store, ValidatorConfig config) {
        this.repository = repository;
        this.store = store;
        this.config = config;
    }

    @Override
    public AttestationRecord attestBlock(long slotNumber, Committee.Index index, Beacon block, byte[] pubKey) {
        if (block.getAttestations().length == 0) {
            throw new RuntimeException("Block should have at least 1 attestation of its proposer");
        }

        // validate that block doesn't contain our attestation
        for (AttestationRecord attestationRecord : block.getAttestations()) {
            if (Bitfield.hasVoted(attestationRecord.getAttesterBitfield(), index.getValidatorIdx())) {
                throw new RuntimeException("Shouldn't attest block again");
            }
        }

        BeaconState headState = repository.get(store.getCanonicalHead().getHash());
        long lastJustified = headState.getCrystallizedState().getFinality().getLastJustifiedSlot();
        AttestationRecord attestationRecord = new AttestationRecord(
                slotNumber,
                index.getShardId(),
                new byte[0][0], // FIXME: obliqueParentHashes
                new byte[32], // FIXME: shardBlockHash??
                Bitfield.markVote(Bitfield.createEmpty(index.getCommitteeSize()), index.getValidatorIdx()),
                lastJustified,
                store.getCanonicalByNumber(lastJustified) == null ? new byte[32] : store.getCanonicalByNumber(lastJustified).getHash(),
                Sign.aggSigns(new byte[][] {Sign.sign(block.getEncoded(), pubKey)})
        );

        logger.info("Block {} attested by #{} in slot {} ", block, index.getValidatorIdx(), slotNumber);
        return attestationRecord;
    }
}
