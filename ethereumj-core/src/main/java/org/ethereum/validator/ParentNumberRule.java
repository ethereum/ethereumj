package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

/**
 * Checks if {@link BlockHeader#number} == {@link BlockHeader#number} + 1 of parent's block
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ParentNumberRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        if (header.getNumber() != (parent.getNumber() + 1)) {
            errors.add("block number is not parentBlock number + 1");
            return false;
        }

        return true;
    }
}
