package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.Dynasty;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class DynastyTransition implements StateTransition<Dynasty> {

    @Override
    public Dynasty applyBlock(Beacon block, Dynasty to) {
        if (block.getSlotNumber() - to.getStartSlot() < BeaconConstants.MIN_DYNASTY_LENGTH)
            return to;

        return to.withNumberIncrement(1L)
                .withCrosslinkingStartShardIncrement(1L);
    }
}
