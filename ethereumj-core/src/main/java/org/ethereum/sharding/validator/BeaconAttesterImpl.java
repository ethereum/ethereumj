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
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.Bitfield;
import org.ethereum.sharding.crypto.Sign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Default implementation of {@link BeaconAttester}.
 */
public class BeaconAttesterImpl implements BeaconAttester {

    private static final Logger logger = LoggerFactory.getLogger("attester");

    StateRepository repository;
    BeaconStore store;
    ValidatorConfig config;
    Sign sign;

    public BeaconAttesterImpl(StateRepository repository, BeaconStore store, ValidatorConfig config,
                              Sign sign) {
        this.repository = repository;
        this.store = store;
        this.config = config;
        this.sign = sign;
    }

    @Override
    public AttestationRecord attestBlock(Input in, byte[] pubKey) {
        long lastJustified = in.state.getCrystallizedState().getFinality().getLastJustifiedSlot();
        AttestationRecord attestationRecord = new AttestationRecord(
                in.slotNumber,
                in.index.getShardId(),
                Collections.emptyList(),
                in.block.getHash(),
                Bitfield.markVote(Bitfield.createEmpty(in.index.getCommitteeSize()), in.index.getValidatorIdx()),
                lastJustified,
                store.getCanonicalByNumber(lastJustified) == null ? new byte[32] : store.getCanonicalByNumber(lastJustified).getHash(),
                sign.aggSigns(new Sign.Signature[]{sign.sign(in.block.getEncoded(), pubKey)})
        );

        logger.info("Block {} attested by #{} in slot {} ", in.block, in.index.getValidatorIdx(), in.slotNumber);
        return attestationRecord;
    }
}
