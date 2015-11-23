package org.ethereum.validator;

import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

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

        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(MIN_GAS_LIMIT)) < 0) {
            errors.add("header.getGasLimit() < MIN_GAS_LIMIT");
            return false;
        }

        return true;
    }
}
