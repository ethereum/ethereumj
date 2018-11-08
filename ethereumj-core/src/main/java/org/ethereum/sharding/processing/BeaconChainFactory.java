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
package org.ethereum.sharding.processing;

import org.ethereum.core.Block;
import org.ethereum.db.DbFlushManager;
import org.ethereum.sharding.crypto.Sign;
import org.ethereum.sharding.processing.consensus.BeaconStateTransition;
import org.ethereum.sharding.processing.consensus.GenesisTransition;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.processing.validation.AttestationsValidator;
import org.ethereum.sharding.processing.validation.BeaconValidator;
import org.ethereum.sharding.processing.validation.StateValidator;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.registration.ValidatorRepository;
import org.ethereum.sharding.validator.BeaconAttester;

/**
 * A factory that creates {@link BeaconChain} instance.
 *
 * <p>
 *     Instantiates strategy implementations that beacon chain depends on,
 *     after that creates beacon chain itself.
 *
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public class BeaconChainFactory {

    private static StateTransition<BeaconState> stateTransition;

    public static BeaconChain create(DbFlushManager beaconDbFlusher, BeaconStore store,
                                     StateRepository repository, StateTransition<BeaconState> genesisStateTransition,
                                     StateTransition<BeaconState> stateTransitionFunction, Sign sign) {

        BeaconValidator beaconValidator = new BeaconValidator(store);
        StateValidator stateValidator = new StateValidator();
        AttestationsValidator attestationsValidator = new AttestationsValidator(store, sign);

        return new BeaconChainImpl(beaconDbFlusher, store, stateTransitionFunction, repository,
                beaconValidator, stateValidator, attestationsValidator, genesisStateTransition);
    }

    public static BeaconChain create(DbFlushManager beaconDbFlusher, BeaconStore store, StateRepository repository,
                                     ValidatorRepository validatorRepository, Block bestBlock,
                                     BeaconAttester beaconAttester, Publisher publisher, Sign sign) {

        StateTransition<BeaconState> genesisStateTransition = new GenesisTransition(validatorRepository)
                .withMainChainRef(bestBlock.getHash());

        return create(beaconDbFlusher, store, repository, genesisStateTransition,
                stateTransition(validatorRepository, beaconAttester, publisher), sign);
    }

    public static StateTransition<BeaconState> stateTransition(ValidatorRepository validatorRepository,
                                                               BeaconAttester beaconAttester,
                                                               Publisher publisher) {
        if (stateTransition == null)
            stateTransition = new BeaconStateTransition(validatorRepository, beaconAttester, publisher);
        return stateTransition;
    }
}
