package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.processing.state.Dynasty;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class DynastyTransition implements StateTransition<Dynasty> {

    CommitteeFactory committeeFactory = new ShufflingCommitteeFactory();

    @Override
    public Dynasty applyBlock(Beacon block, Dynasty to) {
        if (block.getSlotNumber() - to.getStartSlot() < BeaconConstants.MIN_DYNASTY_LENGTH)
            return to;

        // committee transition
        int startShard = to.getCommitteesEndShard() + 1;
        int[] validators = to.getValidatorSet().getActiveIndices();
        Committee[][] committees = committeeFactory.create(block.getHash(), validators, startShard);

        return to.withNumberIncrement(1L)
                .withCommittees(committees);
    }
}
