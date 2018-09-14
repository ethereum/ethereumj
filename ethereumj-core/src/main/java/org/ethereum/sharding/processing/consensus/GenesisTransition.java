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
import org.ethereum.sharding.processing.state.Crosslink;
import org.ethereum.sharding.processing.state.CrystallizedState;
import org.ethereum.sharding.processing.state.Dynasty;
import org.ethereum.sharding.service.ValidatorRepository;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.DEPOSIT_WEI;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.SHARD_COUNT;

/**
 * @author Mikhail Kalinin
 * @since 04.09.2018
 */
public class GenesisTransition implements StateTransition<BeaconState> {

    ValidatorRepository validatorRepository;
    StateTransition<ValidatorSet> validatorSetTransition = new ValidatorSetInitiator();
    byte[] mainChainRef;

    public GenesisTransition(ValidatorRepository validatorRepository) {
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

        ValidatorSet validatorSet = validatorSetTransition.applyBlock(block,
                to.getCrystallizedState().getDynasty().getValidatorSet());

        Dynasty dynasty = to.getCrystallizedState().getDynasty()
                .withValidatorSet(validatorSet)
                .withNumber(1L)
                .withCrosslinkingStartShard(0L)
                .withTotalDepositsIncrement(DEPOSIT_WEI.multiply(BigInteger.valueOf(validatorSet.size())));

        CrystallizedState crystallizedState = to.getCrystallizedState()
                .withDynasty(dynasty)
                .withLastStateRecalc(0L)
                .withCrosslinks(Crosslink.empty(SHARD_COUNT));

        return new BeaconState(crystallizedState);
    }

    class ValidatorSetInitiator implements StateTransition<ValidatorSet> {

        @Override
        public ValidatorSet applyBlock(Beacon block, ValidatorSet set) {
            BeaconGenesis genesis = (BeaconGenesis) block;

            Map<String, Validator> registered = validatorRepository.query(mainChainRef)
                    .stream().collect(Collectors.toMap(v -> Hex.toHexString(v.getPubKey()), Functions.identity()));

            genesis.getInitialValidators().forEach(pubKey -> {
                Validator v = registered.get(pubKey);
                if (v != null) set.add(v);
            });

            return set;
        }
    }
}
