package org.ethereum.validator;

import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds errors list to share between all rules
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public abstract class AbstractValidationRule implements ValidationRule {

    protected List<String> errors = new LinkedList<>();

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public void logErrors(Logger logger) {
        for (String msg : errors) {
            logger.error("{} invalid: {}", getEntityClass().getSimpleName(), msg);
        }
    }

    abstract public Class getEntityClass();
}
