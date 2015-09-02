package org.ethereum.validator;

import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;

import static org.ethereum.config.Constants.MIN_GAS_LIMIT;

/**
 * Checks {@link BlockHeader#gasLimit} against {@link Constants#MIN_GAS_LIMIT}. <br>
 *
 * This check is NOT run in Frontier
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasLimitRule extends BlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        if (header.getGasLimit() < MIN_GAS_LIMIT) {
            errors.add("header.getGasLimit() < MIN_GAS_LIMIT");
            return false;
        }

        return true;
    }
}
