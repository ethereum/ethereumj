package org.ethereum.validator;

import org.ethereum.core.BlockHeader;
import org.slf4j.Logger;

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
     */
    abstract public ValidationResult validate(BlockHeader header);

    protected ValidationResult fault(String error) {
        return new ValidationResult(false, error);
    }

    public static final ValidationResult Success = new ValidationResult(true, null);

    public boolean validateAndLog(BlockHeader header, Logger logger) {
        ValidationResult result = validate(header);
        if (!result.success && logger.isErrorEnabled()) {
            logger.warn("{} invalid {}", getEntityClass(), result.error);
        }
        return result.success;
    }

    /**
     * Validation result is either success or fault
     */
    public static final class ValidationResult {

        public final boolean success;

        public final String error;

        public ValidationResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
    }
}
