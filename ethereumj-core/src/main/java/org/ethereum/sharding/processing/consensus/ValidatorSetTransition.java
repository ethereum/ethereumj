package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.sharding.service.ValidatorRepository;

/**
 * @author Mikhail Kalinin
 * @since 12.09.2018
 */
public class ValidatorSetTransition implements StateTransition<ValidatorSet> {

    ValidatorRepository validatorRepository;

    public ValidatorSetTransition(ValidatorRepository validatorRepository) {
        this.validatorRepository = validatorRepository;
    }

    @Override
    public ValidatorSet applyBlock(Beacon block, ValidatorSet to) {
        return to;
    }
}
