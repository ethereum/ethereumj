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
package org.ethereum.sharding.processing.consensus;

import com.google.common.base.Functions;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.service.ValidatorRepository;
import org.spongycastle.util.encoders.Hex;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mikhail Kalinin
 * @since 04.09.2018
 */
public class GenesisTransition implements StateTransition {

    ValidatorSet validatorSet;
    ValidatorRepository validatorRepository;
    ValidatorSetTransition validatorSetTransition = new ValidatorSetInitiator();
    byte[] mainChainRef;

    public GenesisTransition(ValidatorSet validatorSet, ValidatorRepository validatorRepository) {
        this.validatorSet = validatorSet;
        this.validatorRepository = validatorRepository;
    }

    public GenesisTransition withMainChainRef(byte[] mainChainRef) {
        this.mainChainRef = mainChainRef;
        return this;
    }

    @Override
    public BeaconState applyBlock(Beacon block, BeaconState to) {
        assert block instanceof BeaconGenesis;

        BeaconGenesis genesis = (BeaconGenesis) block;
        if (mainChainRef == null) {
            mainChainRef = genesis.getMainChainRef();
        }

        byte[] validatorSetHash = validatorSetTransition.applyBlock(block, validatorSet);

        return new BeaconState(validatorSetHash, genesis.getRandaoReveal());
    }

    class ValidatorSetInitiator implements ValidatorSetTransition {

        @Override
        public byte[] applyBlock(Beacon block, ValidatorSet to) {
            BeaconGenesis genesis = (BeaconGenesis) block;

            Map<String, Validator> registered = validatorRepository.query(mainChainRef)
                    .stream().collect(Collectors.toMap(v -> Hex.toHexString(v.getPubKey()), Functions.identity()));

            genesis.getInitialValidators().forEach(pubKey -> {
                Validator v = registered.get(pubKey);
                if (v != null) validatorSet.add(v);
            });

            return validatorSet.getHash();
        }
    }
}
