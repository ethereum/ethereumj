package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import static org.ethereum.config.Constants.GAS_LIMIT_BOUND_DIVISOR;

/**
 * Checks if {@link BlockHeader#gasLimit} matches gas limit bounds. <br>
 *
 * This check is NOT run in Frontier
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ParentGasLimitRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        if (header.getGasLimit() < parent.getGasLimit() * (GAS_LIMIT_BOUND_DIVISOR - 1) / GAS_LIMIT_BOUND_DIVISOR ||
            header.getGasLimit() > parent.getGasLimit() * (GAS_LIMIT_BOUND_DIVISOR + 1) / GAS_LIMIT_BOUND_DIVISOR) {

            errors.add("gas limit exceeds parentBlock.getGasLimit() (+-) GAS_LIMIT_BOUND_DIVISOR");
            return false;
        }

        return true;
    }
}
