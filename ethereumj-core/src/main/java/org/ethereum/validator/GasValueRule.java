package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

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

        if (header.getGasLimit() < header.getGasUsed()) {
            errors.add(String.format("#%d: header.getGasLimit() < header.getGasUsed()", header.getNumber()));
            return false;
        }

        return true;
    }
}
