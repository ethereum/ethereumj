package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.processing.state.Dynasty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.MIN_DYNASTY_LENGTH;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class DynastyTransition implements StateTransition<Dynasty> {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    StateTransition<ValidatorSet> validatorSetTransition;
    CommitteeFactory committeeFactory = new ShufflingCommitteeFactory();

    public DynastyTransition(StateTransition<ValidatorSet> validatorSetTransition) {
        this.validatorSetTransition = validatorSetTransition;
    }

    @Override
    public Dynasty applyBlock(Beacon block, Dynasty to) {
        if (block.getSlotNumber() - to.getStartSlot() < MIN_DYNASTY_LENGTH)
            return to;

        logger.info("Calculate new dynasty, slot: {}, prev slot: {}", block.getSlotNumber(), to.getStartSlot());

        // validator set transition
        ValidatorSet validatorSet = validatorSetTransition.applyBlock(block, to.getValidatorSet());

        // committee transition
        int startShard = to.getCommitteesEndShard() + 1;
        int[] validators = validatorSet.getActiveIndices();
        Committee[][] committees = committeeFactory.create(block.getHash(), validators, startShard);

        return to.withNumberIncrement(1L)
                .withStartSlot(block.getSlotNumber() - block.getSlotNumber() % CYCLE_LENGTH)
                .withValidatorSet(validatorSet)
                .withCommittees(committees);
    }
}
