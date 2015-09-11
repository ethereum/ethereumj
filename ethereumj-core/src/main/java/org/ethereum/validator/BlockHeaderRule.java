package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

/**
 * Parent class for {@link BlockHeader} validators
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public abstract class BlockHeaderRule extends AbstractValidationRule {

    @Override
    public Class getEntityClass() {
        return BlockHeader.class;
    }

    /**
     * Runs header validation and returns its result
     *
     * @param header block header
     * @return true if validation passed, false otherwise
     */
    abstract public boolean validate(BlockHeader header);

}
