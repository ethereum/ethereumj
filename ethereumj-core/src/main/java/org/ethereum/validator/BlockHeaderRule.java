package org.ethereum.validator;

import org.ethereum.core.BlockHeader;

import java.util.List;

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

//    /**
//     * Run header validation with print friendly errors
//     * @return returns empty list on success or non empty otherwise
//     */
//    abstract public List<String> getValidationErrors(BlockHeader header);

}
