package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

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
        BigInteger headerGasLimit = new BigInteger(1, header.getGasLimit());
        BigInteger parentGasLimit = new BigInteger(1, parent.getGasLimit());

        if (headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR - 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) < 0 ||
            headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR + 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) > 0) {

            errors.add(String.format(
                    "#%d: gas limit exceeds parentBlock.getGasLimit() (+-) GAS_LIMIT_BOUND_DIVISOR",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
