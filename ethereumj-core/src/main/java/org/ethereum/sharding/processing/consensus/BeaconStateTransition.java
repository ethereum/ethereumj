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

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.ActiveState;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.CrystallizedState;
import org.ethereum.sharding.processing.state.Dynasty;
import org.ethereum.sharding.processing.state.Finality;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.registration.ValidatorRepository;
import org.ethereum.sharding.validator.BeaconAttester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.pubsub.Events.onBeaconAttestationIncluded;
import static org.ethereum.sharding.pubsub.Events.onStateRecalc;
import static org.ethereum.sharding.util.BeaconUtils.cycleStartSlot;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class BeaconStateTransition implements StateTransition<BeaconState> {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    StateTransition<Dynasty> dynastyTransition;
    StateTransition<Finality> finalityTransition;
    BeaconAttester beaconAttester;
    Publisher publisher;

    public BeaconStateTransition(ValidatorRepository validatorRepository, BeaconAttester beaconAttester,
                                 Publisher publisher) {
        this.dynastyTransition = new DynastyTransition(new ValidatorSetTransition(validatorRepository));
        this.finalityTransition = new FinalityTransition();
        this.beaconAttester = beaconAttester;
        this.publisher = publisher;
    }

    public BeaconStateTransition(StateTransition<Dynasty> dynastyTransition,
                                 StateTransition<Finality> finalityTransition) {
        this.dynastyTransition = dynastyTransition;
        this.finalityTransition = finalityTransition;
    }

    @Override
    public BeaconState applyBlock(Beacon block, BeaconState to) {

        CrystallizedState crystallized = to.getCrystallizedState();
        ActiveState activeState = to.getActiveState();

        block.getAttestations().forEach(at -> publisher.publish(onBeaconAttestationIncluded(at)));
        activeState = activeState.addPendingAttestations(block.getAttestations());

        if (block.getSlotNumber() - crystallized.getLastStateRecalc() >= CYCLE_LENGTH) {
            logger.info("Calculate new crystallized state, slot: {}, prev slot: {}",
                    block.getSlotNumber(), crystallized.getLastStateRecalc());

            Finality finality = finalityTransition.applyBlock(block, crystallized.getFinality());
            Dynasty dynasty = dynastyTransition.applyBlock(block, crystallized.getDynasty());

            crystallized = crystallized
                    .withDynasty(dynasty)
                    .withLastStateRecalc(cycleStartSlot(block))
                    .withFinality(finality);
            if (publisher != null) {
                publisher.publish(onStateRecalc(crystallized.getLastStateRecalc()));
            }

            // remove attestations older than last_state_recalc
            activeState = activeState.removeAttestationsPriorTo(crystallized.getLastStateRecalc());
        }

        return new BeaconState(crystallized, activeState);
    }
}
