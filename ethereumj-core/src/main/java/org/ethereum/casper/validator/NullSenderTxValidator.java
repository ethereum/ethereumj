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
package org.ethereum.casper.validator;

import java.util.function.Function;
import org.ethereum.core.Transaction;
import org.ethereum.validator.AbstractValidationRule;

/**
 * Customizable validator for checking NULL_SENDER tx for acceptance
 */
public class NullSenderTxValidator extends AbstractValidationRule {

    private Function<Transaction, Boolean> validator;

    public NullSenderTxValidator(Function<Transaction, Boolean> validator) {
        this.validator = validator;
    }

    @Override
    public Class getEntityClass() {
        return Transaction.class;
    }

    /**
     * Runs transaction validation and returns its result
     *
     * @param transaction
     */
    public Boolean validate(Transaction transaction) {
        return validator.apply(transaction);
    }
}
