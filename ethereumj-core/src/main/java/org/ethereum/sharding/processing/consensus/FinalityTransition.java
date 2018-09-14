package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.Finality;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class FinalityTransition implements StateTransition<Finality> {
    @Override
    public Finality applyBlock(Beacon block, Finality to) {
        return to;
    }
}
