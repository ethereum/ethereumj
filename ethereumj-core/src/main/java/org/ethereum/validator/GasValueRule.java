package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

/**
 * Checks {@link BlockHeader#gasUsed} against {@link BlockHeader#gasLimit}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasValueRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(header.getGasUsed())) < 0) {
            return fault("header.getGasLimit() < header.getGasUsed()");
        }

        return Success;
    }
}
