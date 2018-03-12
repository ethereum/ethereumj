/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.validator;

import org.slf4j.Logger;

/**
 * Parent class for E class validators
 */
public abstract class EntityRule<E> extends AbstractValidationRule {

    /**
     * Runs E validation and returns its result
     *
     * @param entity of E class
     */
    abstract public ValidationResult validate(E entity);

    protected ValidationResult fault(String error) {
        return new ValidationResult(false, error);
    }

    public static final ValidationResult Success = new ValidationResult(true, null);

    public boolean validateAndLog(E entity, Logger logger) {
        ValidationResult result = validate(entity);
        if (!result.success && logger.isErrorEnabled()) {
            logger.warn("{} invalid {}", entity.getClass(), result.error);
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
