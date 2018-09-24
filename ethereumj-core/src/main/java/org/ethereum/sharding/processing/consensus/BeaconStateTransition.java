package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.CrystallizedState;
import org.ethereum.sharding.processing.state.Dynasty;
import org.ethereum.sharding.processing.state.Finality;
import org.ethereum.sharding.service.ValidatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class BeaconStateTransition implements StateTransition<BeaconState> {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    StateTransition<Dynasty> dynastyTransition;
    StateTransition<Finality> finalityTransition;

    public BeaconStateTransition(ValidatorRepository validatorRepository) {
        this.dynastyTransition = new DynastyTransition(new ValidatorSetTransition(validatorRepository));
        this.finalityTransition = new FinalityTransition();
    }

    public BeaconStateTransition(StateTransition<Dynasty> dynastyTransition,
                                 StateTransition<Finality> finalityTransition) {
        this.dynastyTransition = dynastyTransition;
        this.finalityTransition = finalityTransition;
    }

    @Override
    public BeaconState applyBlock(Beacon block, BeaconState to) {

        CrystallizedState crystallized = to.getCrystallizedState();

        if (block.getSlotNumber() - crystallized.getLastStateRecalc() >= CYCLE_LENGTH) {
            logger.info("Calculate new crystallized state, slot: {}", block.getSlotNumber());

            Finality finality = finalityTransition.applyBlock(block, crystallized.getFinality());
            Dynasty dynasty = dynastyTransition.applyBlock(block, crystallized.getDynasty());

            crystallized = crystallized
                    .withDynasty(dynasty)
                    .withLastStateRecalcIncrement(CYCLE_LENGTH)
                    .withFinality(finality);
        }

        return new BeaconState(crystallized);
    }
}
