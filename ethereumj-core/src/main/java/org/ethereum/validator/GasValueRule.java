package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.math.BigInteger;
import java.util.List;

/**
 * Checks {@link BlockHeader#gasUsed} against {@link BlockHeader#gasLimit}
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasValueRule extends BlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(header.getGasUsed())) < 0) {
            errors.add("header.getGasLimit() < header.getGasUsed()");
            return false;
        }

        return true;
    }
}
